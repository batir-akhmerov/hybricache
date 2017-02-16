/**
 * 
 */
package org.hybricache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.hybricache.HybriCacheConfiguration.CacheMode;
import org.hybricache.HybriCacheConfiguration.CacheType;
import org.hybricache.key.HybriKeyCache;
import org.hybricache.remote.RemoteCacheFactory;
import org.hybricache.remote.redis.RedisRemoteCacheFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.springframework.util.StringUtils;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * The HybriCacheManager class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
public class HybriCacheManager extends AbstractTransactionSupportingCacheManager {

	public static final CacheConfiguration DEF_EHCACHE_CONFIG =  new CacheConfiguration("defaultEhCache", 0)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.eternal(false)
			.timeToLiveSeconds(60)
			.timeToIdleSeconds(30)
			.diskExpiryThreadIntervalSeconds(0)
			.persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP));
	
	public static final HybriCacheConfiguration DEF_HYBRID_CACHE_CONFIG = new HybriCacheConfiguration("defaultEhCache", CacheType.HYBRID, 1000);
	
	public static final RemoteCacheFactory DEF_REMOTE_CACHE_FACTORY = new RedisRemoteCacheFactory();
	
	public static final String CACHE_KEY = "hybriKey";
	public static final String CACHE_COMMON = "hybriCommon";
	
	protected Map<String, Cache> caches = new HashMap<>();
	
	protected HybriCacheConfiguration defaultHybriCacheConfiguration;
	protected List<HybriCacheConfiguration> hybriCacheConfigurationList;
	protected RemoteCacheFactory remoteCacheFactory;
	
	protected EhCacheCacheManager  localCacheManager;
	private String remoteSeverHost;
	private Integer remoteServerPort;
	
	//private int remoteDatabaseMaxSize;
	private int remoteDatabaseSize; 
	
	protected int getNextRemoteDatabaseIndex() {
		return this.remoteDatabaseSize++;
	}
	protected void resetRemoteDatabaseSize() {
		this.remoteDatabaseSize = 0;
	}
	
	
	public HybriCacheManager(EhCacheCacheManager  localCacheManager, String remoteSeverHost, Integer remoteServerPort) {
		this.localCacheManager = localCacheManager;
		this.remoteSeverHost = remoteSeverHost;
		this.remoteServerPort = remoteServerPort;
	}
	
	public Configuration getConfiguration() {
		return this.localCacheManager.getCacheManager().getConfiguration();
	}
	
	
	@Override
	protected Collection<Cache> loadCaches() {
		Status status = this.localCacheManager.getCacheManager().getStatus();
		if (!Status.STATUS_ALIVE.equals(status)) {
			throw new IllegalStateException(
					"An 'alive' EhCache CacheManager is required - current cache is " + status.toString());
		}
		
		resetRemoteDatabaseSize();
		
		HybriCacheConfiguration systemHybriCacheConfig = new HybriCacheConfiguration(null, CacheType.HYBRID, 1000);
		systemHybriCacheConfig.setDatabaseIndex(getNextRemoteDatabaseIndex());
		setRemoteServer(systemHybriCacheConfig);
		registerEhCache(CACHE_KEY, systemHybriCacheConfig, null);
		
		systemHybriCacheConfig.setDatabaseIndex(getNextRemoteDatabaseIndex());
		registerEhCache(CACHE_COMMON, systemHybriCacheConfig, null);
		
		String[] names = this.localCacheManager.getCacheManager().getCacheNames();
		Collection<Cache> cacheSet = new LinkedHashSet<>(names.length);
		for (String cacheName : names) {
			Ehcache ehcache = this.localCacheManager.getCacheManager().getEhcache(cacheName);
			HybriCacheConfiguration hybriCacheConfig = findOrAddHybriCacheConfig(cacheName, ehcache.getCacheConfiguration());
			
			HybriCache hybriCache = new HybriCache(ehcache, hybriCacheConfig, getRemoteCacheFactory(), createHybriKeyCacheIfNeeded(hybriCacheConfig));
			this.caches.put(cacheName, hybriCache);
			cacheSet.add(hybriCache);
		}
		
		// load caches declared in hybriCacheConfigurationList but missing in ehcache.xml
		for (HybriCacheConfiguration hybriCacheConfig: this.hybriCacheConfigurationList) {
			String cacheName = hybriCacheConfig.getCacheName();
			if (this.caches.containsKey(hybriCacheConfig.getCacheName())) {
				continue;
			}
			setRemoteServer(hybriCacheConfig);
			hybriCacheConfig = registerEhCache(cacheName, hybriCacheConfig, null);
			Ehcache ehcache = this.localCacheManager.getCacheManager().getEhcache(cacheName);
			HybriCache hybriCache = new HybriCache(ehcache, hybriCacheConfig, getRemoteCacheFactory(), createHybriKeyCacheIfNeeded(hybriCacheConfig));
			this.caches.put(cacheName, hybriCache);
			cacheSet.add(hybriCache);
			
		}
		return cacheSet;
	}
	
	@Override
	protected Cache getMissingCache(String name) {
		return createHybriCache_Common(name);
	}
	
	protected HybriKeyCache createHybriCache_Key(String cacheName) {
		return (HybriKeyCache) createHybriCache(CACHE_KEY + "_" + cacheName, CACHE_KEY, true, true);
	}

	protected Cache createHybriCache_Common(String cacheName) {
		return createHybriCache(cacheName, CACHE_COMMON, true, false);
	}
	
	protected Cache createHybriCache(String cacheName, String parentCacheName, boolean tryToCreate, boolean isKeyCache) {
		// Check the EhCache cache again (in case the cache was added at runtime)
		CacheManager ehCacheManager =  this.localCacheManager.getCacheManager(); 		
		Ehcache ehcache = ehCacheManager.getEhcache(cacheName);
		if (ehcache != null) {
			HybriCacheConfiguration hybriConfig = findConfigByName(cacheName);
			Cache hybriCache = null;
			if (isKeyCache) {
				hybriCache = new HybriKeyCache(ehcache, hybriConfig, getRemoteCacheFactory());
			}
			else {
				hybriCache = new HybriCache(ehcache, hybriConfig, getRemoteCacheFactory(), createHybriKeyCacheIfNeeded(hybriConfig));
			}
			this.caches.put(cacheName, hybriCache);
			return hybriCache;
		}
		else if (!tryToCreate) {
			throw new IllegalStateException(String.format("Cannot find nor create missing cache [{%s}]!", cacheName));
		}
		
		// register a new ehCache
		HybriCacheConfiguration commonHybriCacheConfig = findConfigByName(parentCacheName);		
		HybriCacheConfiguration hashHybriCacheConfig = commonHybriCacheConfig.clone();
		// its remote config is going to have same CACHE_COMMON's databaseIndex but will act as hashCache with hashKey (CACHE_COMMON + "_" + cacheName)
		hashHybriCacheConfig.setCacheMode(CacheMode.HASH);
		hashHybriCacheConfig.setCacheName(cacheName);
		//hashHybriCacheConfig.setHashCacheName(parentCacheName + "_" + cacheName);
		
		registerEhCache(cacheName, hashHybriCacheConfig, null);
		
		return createHybriCache(cacheName, parentCacheName, false, isKeyCache);
	}
	
	protected HybriKeyCache createHybriKeyCacheIfNeeded(HybriCacheConfiguration hybriConfig){
		String cacheName = hybriConfig.getCacheName();
		if (hybriConfig.getCacheType() != CacheType.HYBRID || CACHE_KEY.equals(cacheName) || CACHE_COMMON.equals(cacheName)) {
			return null;
		}
		return createHybriCache_Key(hybriConfig.getCacheName());
	}
	
	protected HybriCacheConfiguration registerEhCache(String cacheName, HybriCacheConfiguration newHybriCacheConfig, net.sf.ehcache.config.CacheConfiguration ehCacheConfig) {		
		CacheManager ehCacheManager =  this.localCacheManager.getCacheManager();
		
		// create ehCache cache
		if (ehCacheConfig == null) {
			ehCacheConfig = getDefaultEhCacheConfig().clone();
		}
		ehCacheConfig.setName(cacheName);
				
		net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache( ehCacheConfig );
		
		ehCacheManager.addCache(cache);
		
		if (newHybriCacheConfig != null) {
			return addHybriCacheConfig(cacheName, newHybriCacheConfig, ehCacheConfig);
		}
		
		return findOrAddHybriCacheConfig(cacheName, ehCacheConfig);
	}
	
	protected HybriCacheConfiguration addHybriCacheConfig(String cacheName, HybriCacheConfiguration hybriCacheConfig, net.sf.ehcache.config.CacheConfiguration ehCacheConfig) {
		assertNotNull(hybriCacheConfig);
		//assertNull(findConfigByName(cacheName));
		
		hybriCacheConfig = hybriCacheConfig.clone();
		hybriCacheConfig.setCacheName(cacheName);
		if (ehCacheConfig != null) {
			hybriCacheConfig.setEhCacheConfiguration(ehCacheConfig);
		}
		
		int existingConfigIndex = findConfigIndex(cacheName);
		if (existingConfigIndex == -1) {
			getCacheConfigListSafely().add(hybriCacheConfig);
		}
		else {
			getCacheConfigListSafely().set(existingConfigIndex, hybriCacheConfig);
		}
		
		return hybriCacheConfig;
	}
		
		
	
	protected HybriCacheConfiguration findOrAddHybriCacheConfig(String cacheName, net.sf.ehcache.config.CacheConfiguration ehCacheConfig) {
		HybriCacheConfiguration hybriCacheConfig = findConfigByName(cacheName);
		if (hybriCacheConfig == null) {
			hybriCacheConfig = DEF_HYBRID_CACHE_CONFIG.clone();
			hybriCacheConfig.setCacheName(cacheName);
			hybriCacheConfig.setDatabaseIndex(getNextRemoteDatabaseIndex());
			getCacheConfigListSafely().add(hybriCacheConfig);		
		}
		
		if (ehCacheConfig != null) {
			hybriCacheConfig.setEhCacheConfiguration(ehCacheConfig);
		}
		
		setRemoteServer(hybriCacheConfig);
		
		return hybriCacheConfig;
	}
	
	protected void setRemoteServer(HybriCacheConfiguration conf) {
		conf.setRemoteSeverHost(getRemoteSeverHost());
		conf.setRemoteServerPort(getRemoteServerPort());
	}
	
	protected HybriCacheConfiguration findConfigByName(String cacheName) {
		assertFalse("CacheName cannot be blank!", StringUtils.isEmpty(cacheName));
		List<HybriCacheConfiguration> list = getCacheConfigListSafely();
		for (HybriCacheConfiguration config: list) {
			if (cacheName.equals(config.getCacheName())) {
				return config;
			}
		}
		return null;
	}
	
	protected int findConfigIndex(String cacheName) {
		assertFalse("CacheName cannot be blank!", StringUtils.isEmpty(cacheName));
		List<HybriCacheConfiguration> list = getCacheConfigListSafely();
		for (int i = 0, size = list.size(); i < size; i++) {
			HybriCacheConfiguration config = list.get(i);
			if (cacheName.equals(config.getCacheName())) {
				return i;
			}
		}
		return -1;
	}
	
	protected List<HybriCacheConfiguration> getCacheConfigListSafely(){
		if (this.hybriCacheConfigurationList == null) {
			this.hybriCacheConfigurationList = new ArrayList<>();
		}
		return this.hybriCacheConfigurationList;
	}
	
	protected net.sf.ehcache.config.CacheConfiguration getDefaultEhCacheConfig() {
		if (this.defaultHybriCacheConfiguration != null && this.defaultHybriCacheConfiguration.getEhCacheConfiguration() != null) {
			return this.defaultHybriCacheConfiguration.getEhCacheConfiguration();
		}
		return DEF_EHCACHE_CONFIG;
	}


	public EhCacheCacheManager getLocalCacheManager() {
		return this.localCacheManager;
	}


	public void setLocalCacheManager(EhCacheCacheManager localCacheManager) {
		this.localCacheManager = localCacheManager;
	}

	/**
	 *
	 * @return the remoteSeverHost
	 */
	public String getRemoteSeverHost() {
		return this.remoteSeverHost;
	}

	/**
	 *
	 * @param remoteSeverHost the remoteSeverHost to set
	 */
	public void setRemoteSeverHost(String remoteSeverHost) {
		this.remoteSeverHost = remoteSeverHost;
	}

	/**
	 *
	 * @return the remoteServerPort
	 */
	public Integer getRemoteServerPort() {
		return this.remoteServerPort;
	}

	/**
	 *
	 * @param remoteServerPort the remoteServerPort to set
	 */
	public void setRemoteServerPort(Integer remoteServerPort) {
		this.remoteServerPort = remoteServerPort;
	}

	/**
	 *
	 * @return the defaultHybriCacheConfiguration
	 */
	public HybriCacheConfiguration getDefaultHybriCacheConfiguration() {
		return this.defaultHybriCacheConfiguration;
	}

	/**
	 *
	 * @param defaultHybriCacheConfiguration the defaultHybriCacheConfiguration to set
	 */
	public void setDefaultHybriCacheConfiguration(HybriCacheConfiguration defaultHybriCacheConfiguration) {
		this.defaultHybriCacheConfiguration = defaultHybriCacheConfiguration;
	}

	/**
	 *
	 * @return the hybriCacheConfigurationList
	 */
	public List<HybriCacheConfiguration> getHybriCacheConfigurationList() {
		return this.hybriCacheConfigurationList;
	}

	/**
	 *
	 * @param hybriCacheConfigurationList the hybriCacheConfigurationList to set
	 */
	public void setHybriCacheConfigurationList(List<HybriCacheConfiguration> hybriCacheConfigurationList) {
		this.hybriCacheConfigurationList = hybriCacheConfigurationList;
	}

	/**
	 *
	 * @return the remoteDatabaseSize
	 */
	public int getRemoteDatabaseSize() {
		return this.remoteDatabaseSize;
	}

	/**
	 *
	 * @param remoteDatabaseSize the remoteDatabaseSize to set
	 */
	public void setRemoteDatabaseSize(int remoteDatabaseSize) {
		this.remoteDatabaseSize = remoteDatabaseSize;
	}
	
	
	public RemoteCacheFactory getRemoteCacheFactory() {
		if (this.remoteCacheFactory == null) {
			this.remoteCacheFactory = DEF_REMOTE_CACHE_FACTORY;
		}
		return this.remoteCacheFactory;
	}
	public void setRemoteCacheFactory(RemoteCacheFactory remoteCacheFactory) {
		this.remoteCacheFactory = remoteCacheFactory;
	}

	

}
