/**
 * 
 */
package org.r3p.cache.hybrid;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.r3p.cache.hybrid.HybridCacheConfiguration.CacheMode;
import org.r3p.cache.hybrid.HybridCacheConfiguration.CacheType;
import org.r3p.cache.hybrid.key.HybridKeyCache;
import org.r3p.cache.hybrid.remote.RemoteCacheFactory;
import org.r3p.cache.hybrid.remote.redis.RedisRemoteCacheFactory;
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
 * The HybridCacheManager class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
public class HybridCacheManager extends AbstractTransactionSupportingCacheManager {

	public static final CacheConfiguration DEF_EHCACHE_CONFIG =  new CacheConfiguration("defaultEhCache", 0)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.eternal(false)
			.timeToLiveSeconds(60)
			.timeToIdleSeconds(30)
			.diskExpiryThreadIntervalSeconds(0)
			.persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP));
	
	public static final HybridCacheConfiguration DEF_HYBRID_CACHE_CONFIG = new HybridCacheConfiguration("defaultEhCache", CacheType.HYBRID, 1000);
	
	public static final RemoteCacheFactory DEF_REMOTE_CACHE_FACTORY = new RedisRemoteCacheFactory();
	
	public static final String CACHE_KEY = "hybridKey";
	public static final String CACHE_COMMON = "hybridCommon";
	
	protected Map<String, Cache> caches = new HashMap<>();
	
	protected HybridCacheConfiguration defaultHybridCacheConfiguration;
	protected List<HybridCacheConfiguration> hybridCacheConfigurationList;
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
	
	
	public HybridCacheManager(EhCacheCacheManager  localCacheManager, String remoteSeverHost, Integer remoteServerPort) {
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
		
		HybridCacheConfiguration systemHybridCacheConfig = new HybridCacheConfiguration(null, CacheType.HYBRID, 1000);
		systemHybridCacheConfig.setDatabaseIndex(getNextRemoteDatabaseIndex());
		setRemoteServer(systemHybridCacheConfig);
		registerEhCache(CACHE_KEY, systemHybridCacheConfig, null);
		
		systemHybridCacheConfig.setDatabaseIndex(getNextRemoteDatabaseIndex());
		registerEhCache(CACHE_COMMON, systemHybridCacheConfig, null);
		
		String[] names = this.localCacheManager.getCacheManager().getCacheNames();
		Collection<Cache> cacheSet = new LinkedHashSet<>(names.length);
		for (String cacheName : names) {
			Ehcache ehcache = this.localCacheManager.getCacheManager().getEhcache(cacheName);
			HybridCacheConfiguration hybridCacheConfig = findOrAddHybridCacheConfig(cacheName, ehcache.getCacheConfiguration());
			
			HybridCache hybridCache = new HybridCache(ehcache, hybridCacheConfig, getRemoteCacheFactory(), createHybridKeyCacheIfNeeded(hybridCacheConfig));
			this.caches.put(cacheName, hybridCache);
			cacheSet.add(hybridCache);
		}
		
		// load caches declared in hybridCacheConfigurationList but missing in ehcache.xml
		for (HybridCacheConfiguration hybridCacheConfig: this.hybridCacheConfigurationList) {
			String cacheName = hybridCacheConfig.getCacheName();
			if (this.caches.containsKey(hybridCacheConfig.getCacheName())) {
				continue;
			}
			setRemoteServer(hybridCacheConfig);
			hybridCacheConfig = registerEhCache(cacheName, hybridCacheConfig, null);
			Ehcache ehcache = this.localCacheManager.getCacheManager().getEhcache(cacheName);
			HybridCache hybridCache = new HybridCache(ehcache, hybridCacheConfig, getRemoteCacheFactory(), createHybridKeyCacheIfNeeded(hybridCacheConfig));
			this.caches.put(cacheName, hybridCache);
			cacheSet.add(hybridCache);
			
		}
		return cacheSet;
	}
	
	@Override
	protected Cache getMissingCache(String name) {
		return createHybridCache_Common(name);
	}
	
	protected HybridKeyCache createHybridCache_Key(String cacheName) {
		return (HybridKeyCache) createHybridCache(CACHE_KEY + "_" + cacheName, CACHE_KEY, true, true);
	}

	protected Cache createHybridCache_Common(String cacheName) {
		return createHybridCache(cacheName, CACHE_COMMON, true, false);
	}
	
	protected Cache createHybridCache(String cacheName, String parentCacheName, boolean tryToCreate, boolean isKeyCache) {
		// Check the EhCache cache again (in case the cache was added at runtime)
		CacheManager ehCacheManager =  this.localCacheManager.getCacheManager(); 		
		Ehcache ehcache = ehCacheManager.getEhcache(cacheName);
		if (ehcache != null) {
			HybridCacheConfiguration hybridConfig = findConfigByName(cacheName);
			Cache hybridCache = null;
			if (isKeyCache) {
				hybridCache = new HybridKeyCache(ehcache, hybridConfig, getRemoteCacheFactory());
			}
			else {
				hybridCache = new HybridCache(ehcache, hybridConfig, getRemoteCacheFactory(), createHybridKeyCacheIfNeeded(hybridConfig));
			}
			this.caches.put(cacheName, hybridCache);
			return hybridCache;
		}
		else if (!tryToCreate) {
			throw new IllegalStateException(String.format("Cannot find nor create missing cache [{%s}]!", cacheName));
		}
		
		// register a new ehCache
		HybridCacheConfiguration commonHybridCacheConfig = findConfigByName(parentCacheName);		
		HybridCacheConfiguration hashHybridCacheConfig = commonHybridCacheConfig.clone();
		// its remote config is going to have same CACHE_COMMON's databaseIndex but will act as hashCache with hashKey (CACHE_COMMON + "_" + cacheName)
		hashHybridCacheConfig.setCacheMode(CacheMode.HASH);
		hashHybridCacheConfig.setCacheName(cacheName);
		hashHybridCacheConfig.setHashCacheName(parentCacheName + "_" + cacheName);
		
		registerEhCache(cacheName, hashHybridCacheConfig, null);
		
		return createHybridCache(cacheName, parentCacheName, false, isKeyCache);
	}
	
	protected HybridKeyCache createHybridKeyCacheIfNeeded(HybridCacheConfiguration hybridConfig){
		if (hybridConfig.getCacheType() != CacheType.HYBRID) {
			return null;
		}
		return createHybridCache_Key(hybridConfig.getCacheName());
	}
	
	protected HybridCacheConfiguration registerEhCache(String cacheName, HybridCacheConfiguration newHybridCacheConfig, net.sf.ehcache.config.CacheConfiguration ehCacheConfig) {		
		CacheManager ehCacheManager =  this.localCacheManager.getCacheManager();
		
		// create ehCache cache
		if (ehCacheConfig == null) {
			ehCacheConfig = getDefaultEhCacheConfig().clone();
		}
		ehCacheConfig.setName(cacheName);
				
		net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache( ehCacheConfig );
		
		ehCacheManager.addCache(cache);
		
		if (newHybridCacheConfig != null) {
			return addHybridCacheConfig(cacheName, newHybridCacheConfig, ehCacheConfig);
		}
		
		return findOrAddHybridCacheConfig(cacheName, ehCacheConfig);
	}
	
	protected HybridCacheConfiguration addHybridCacheConfig(String cacheName, HybridCacheConfiguration hybridCacheConfig, net.sf.ehcache.config.CacheConfiguration ehCacheConfig) {
		assertNotNull(hybridCacheConfig);
		//assertNull(findConfigByName(cacheName));
		
		hybridCacheConfig = hybridCacheConfig.clone();
		hybridCacheConfig.setCacheName(cacheName);
		if (ehCacheConfig != null) {
			hybridCacheConfig.setEhCacheConfiguration(ehCacheConfig);
		}
		
		int existingConfigIndex = findConfigIndex(cacheName);
		if (existingConfigIndex == -1) {
			getCacheConfigListSafely().add(hybridCacheConfig);
		}
		else {
			getCacheConfigListSafely().set(existingConfigIndex, hybridCacheConfig);
		}
		
		return hybridCacheConfig;
	}
		
		
	
	protected HybridCacheConfiguration findOrAddHybridCacheConfig(String cacheName, net.sf.ehcache.config.CacheConfiguration ehCacheConfig) {
		HybridCacheConfiguration hybridCacheConfig = findConfigByName(cacheName);
		if (hybridCacheConfig == null) {
			hybridCacheConfig = DEF_HYBRID_CACHE_CONFIG.clone();
			hybridCacheConfig.setCacheName(cacheName);
			hybridCacheConfig.setDatabaseIndex(getNextRemoteDatabaseIndex());
			getCacheConfigListSafely().add(hybridCacheConfig);		
		}
		
		if (ehCacheConfig != null) {
			hybridCacheConfig.setEhCacheConfiguration(ehCacheConfig);
		}
		
		setRemoteServer(hybridCacheConfig);
		
		return hybridCacheConfig;
	}
	
	protected void setRemoteServer(HybridCacheConfiguration conf) {
		conf.setRemoteSeverHost(getRemoteSeverHost());
		conf.setRemoteServerPort(getRemoteServerPort());
	}
	
	protected HybridCacheConfiguration findConfigByName(String cacheName) {
		assertFalse("CacheName cannot be blank!", StringUtils.isEmpty(cacheName));
		List<HybridCacheConfiguration> list = getCacheConfigListSafely();
		for (HybridCacheConfiguration config: list) {
			if (cacheName.equals(config.getCacheName())) {
				return config;
			}
		}
		return null;
	}
	
	protected int findConfigIndex(String cacheName) {
		assertFalse("CacheName cannot be blank!", StringUtils.isEmpty(cacheName));
		List<HybridCacheConfiguration> list = getCacheConfigListSafely();
		for (int i = 0, size = list.size(); i < size; i++) {
			HybridCacheConfiguration config = list.get(i);
			if (cacheName.equals(config.getCacheName())) {
				return i;
			}
		}
		return -1;
	}
	
	protected List<HybridCacheConfiguration> getCacheConfigListSafely(){
		if (this.hybridCacheConfigurationList == null) {
			this.hybridCacheConfigurationList = new ArrayList<>();
		}
		return this.hybridCacheConfigurationList;
	}
	
	protected net.sf.ehcache.config.CacheConfiguration getDefaultEhCacheConfig() {
		if (this.defaultHybridCacheConfiguration != null && this.defaultHybridCacheConfiguration.getEhCacheConfiguration() != null) {
			return this.defaultHybridCacheConfiguration.getEhCacheConfiguration();
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
	 * @return the defaultHybridCacheConfiguration
	 */
	public HybridCacheConfiguration getDefaultHybridCacheConfiguration() {
		return this.defaultHybridCacheConfiguration;
	}

	/**
	 *
	 * @param defaultHybridCacheConfiguration the defaultHybridCacheConfiguration to set
	 */
	public void setDefaultHybridCacheConfiguration(HybridCacheConfiguration defaultHybridCacheConfiguration) {
		this.defaultHybridCacheConfiguration = defaultHybridCacheConfiguration;
	}

	/**
	 *
	 * @return the hybridCacheConfigurationList
	 */
	public List<HybridCacheConfiguration> getHybridCacheConfigurationList() {
		return this.hybridCacheConfigurationList;
	}

	/**
	 *
	 * @param hybridCacheConfigurationList the hybridCacheConfigurationList to set
	 */
	public void setHybridCacheConfigurationList(List<HybridCacheConfiguration> hybridCacheConfigurationList) {
		this.hybridCacheConfigurationList = hybridCacheConfigurationList;
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
