/**
 * 
 */
package org.r3p.cache.hybrid;

import org.r3p.cache.hybrid.HybridCacheConfiguration.CacheType;
import org.r3p.cache.hybrid.key.HybridKeyCache;
import org.r3p.cache.hybrid.remote.RemoteCache;
import org.r3p.cache.hybrid.remote.RemoteCacheFactory;
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
	protected HybridCacheConfiguration hybridCacheConfig;
	@SuppressWarnings("rawtypes")
	protected RemoteCache remoteCache;
	
	private HybridKeyCache hybridKeyCache;
	
	
	public BaseCache(Ehcache ehCacheNative, HybridCacheConfiguration hybridCacheConfig, RemoteCacheFactory remoteCacheFactory) {
		this(new EhCacheCache(ehCacheNative), hybridCacheConfig, remoteCacheFactory.getInstance(hybridCacheConfig));
	}
	
	public BaseCache(EhCacheCache ehCache, HybridCacheConfiguration hybridCacheConfig, RemoteCacheFactory remoteCacheFactory) {
		this(ehCache, hybridCacheConfig, remoteCacheFactory.getInstance(hybridCacheConfig));
	}
	
	@SuppressWarnings("rawtypes")
	public BaseCache(EhCacheCache ehCache, HybridCacheConfiguration hybridCacheConfig, RemoteCache remoteCache) {
		this.ehCache = ehCache;
		this.hybridCacheConfig = hybridCacheConfig;
		this.remoteCache = remoteCache;		
	}

	
	

	public boolean isCacheRemote() {
		return this.hybridCacheConfig.getCacheType() == CacheType.REMOTE;
	}
	public boolean isCacheHybrid() {
		return this.hybridCacheConfig.getCacheType() == CacheType.HYBRID;
	}
	public boolean isCacheLocal() {
		return this.hybridCacheConfig.getCacheType() == CacheType.LOCAL;
	}
	
	
	
	public HybridCacheConfiguration getHybridCacheConfig() {
		return this.hybridCacheConfig;
	}

	
	public void setHybridCacheConfig(HybridCacheConfiguration hybridCacheConfig) {
		this.hybridCacheConfig = hybridCacheConfig;
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


	public HybridKeyCache getHybridKeyCache() {
		return this.hybridKeyCache;
	}


	public void setHybridKeyCache(HybridKeyCache hybridKeyCache) {
		this.hybridKeyCache = hybridKeyCache;
	}

	

}
