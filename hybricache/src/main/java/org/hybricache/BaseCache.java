/**
 * 
 */
package org.hybricache;

import org.hybricache.HybriCacheConfiguration.CacheType;
import org.hybricache.key.HybriKeyCache;
import org.hybricache.remote.RemoteCache;
import org.hybricache.remote.RemoteCacheFactory;
import org.springframework.cache.ehcache.EhCacheCache;

import net.sf.ehcache.Ehcache;

/**
 * The BaseCache class
 *
 * @author Batir Akhmerov
 * Created on Feb 4, 2017
 */ 
public class BaseCache {
	
	protected EhCacheCache ehCache;
	protected HybriCacheConfiguration hybriCacheConfig;
	@SuppressWarnings("rawtypes")
	protected RemoteCache remoteCache;
	
	private HybriKeyCache hybriKeyCache;
	
	
	public BaseCache(Ehcache ehCacheNative, HybriCacheConfiguration hybriCacheConfig, RemoteCacheFactory remoteCacheFactory) {
		this(new EhCacheCache(ehCacheNative), hybriCacheConfig, remoteCacheFactory.getInstance(hybriCacheConfig));
	}
	
	public BaseCache(EhCacheCache ehCache, HybriCacheConfiguration hybriCacheConfig, RemoteCacheFactory remoteCacheFactory) {
		this(ehCache, hybriCacheConfig, remoteCacheFactory.getInstance(hybriCacheConfig));
	}
	
	@SuppressWarnings("rawtypes")
	public BaseCache(EhCacheCache ehCache, HybriCacheConfiguration hybriCacheConfig, RemoteCache remoteCache) {
		this.ehCache = ehCache;
		this.hybriCacheConfig = hybriCacheConfig;
		this.remoteCache = remoteCache;		
	}

	

	public boolean isCacheRemote() {
		return this.hybriCacheConfig.getCacheType() == CacheType.REMOTE;
	}
	public boolean isCacheHybrid() {
		return this.hybriCacheConfig.getCacheType() == CacheType.HYBRID;
	}
	public boolean isCacheLocal() {
		return this.hybriCacheConfig.getCacheType() == CacheType.LOCAL;
	}
	
	
	
	public HybriCacheConfiguration getHybriCacheConfig() {
		return this.hybriCacheConfig;
	}

	
	public void setHybriCacheConfig(HybriCacheConfiguration hybriCacheConfig) {
		this.hybriCacheConfig = hybriCacheConfig;
	}

	@SuppressWarnings("rawtypes")
	public RemoteCache getRemoteCache() {
		return this.remoteCache;
	}

	@SuppressWarnings("rawtypes")
	public void setRemoteCache(RemoteCache remoteCache) {
		this.remoteCache = remoteCache;
	}

	public EhCacheCache getEhCache() {
		return this.ehCache;
	}

	public void setEhCache(EhCacheCache ehCache) {
		this.ehCache = ehCache;
	}


	public HybriKeyCache getHybriKeyCache() {
		return this.hybriKeyCache;
	}


	public void setHybriKeyCache(HybriKeyCache hybriKeyCache) {
		this.hybriKeyCache = hybriKeyCache;
	}

	

}
