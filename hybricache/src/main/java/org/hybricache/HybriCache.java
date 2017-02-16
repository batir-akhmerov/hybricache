/**
 * 
 */
package org.r3p.cache.hybrid;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Callable;

import org.r3p.cache.hybrid.HybridCacheConfiguration.CacheType;
import org.r3p.cache.hybrid.key.HybridKey;
import org.r3p.cache.hybrid.key.HybridKeyCache;
import org.r3p.cache.hybrid.remote.RemoteCache;
import org.r3p.cache.hybrid.remote.RemoteCacheFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;

import net.sf.ehcache.Ehcache;

/**
 * The HybridKeyCache class
 *
 * @author Batir Akhmerov
 * Created on Jan 27, 2017
 */
public class HybridCache extends BaseCache implements Cache{
	
	private HybridKeyCache hybridKeyCache;
	
	
	public HybridCache(Ehcache ehCacheNative, HybridCacheConfiguration hybridCacheConfig, 
			RemoteCacheFactory remoteCacheFactory, HybridKeyCache hybridKeyCache) {
		super(ehCacheNative, hybridCacheConfig, remoteCacheFactory);		
		if (isCacheHybrid()) {
			assertNotNull(hybridKeyCache);
			this.hybridKeyCache = hybridKeyCache;
		}
	}
	
	public HybridCache(EhCacheCache ehCache, HybridCacheConfiguration hybridCacheConfig, 
			RemoteCacheFactory remoteCacheFactory, HybridKeyCache hybridKeyCache) {
		super(ehCache, hybridCacheConfig, remoteCacheFactory);		
		if (isCacheHybrid()) {
			assertNotNull(hybridKeyCache);
			this.hybridKeyCache = hybridKeyCache;
		}
	}

	
	@Override
	public void clear() {
		getEhCache().clear();	
		getRemoteCache().clear();
		if (isCacheHybrid()) {
			getHybridKeyCache().clear();
		}
	}

	@Override
	public void evict(Object key) {
		if (isCacheRemote()) {
			getRemoteCache().evict(key);
			return;
		}
		else if (isCacheLocal()) {
			getEhCache().evict(key);
			return;
		}
		getEhCache().evict(key);
		getRemoteCache().evict(key);
	}
	
	@Override
	public ValueWrapper get(Object key) {
		if (isCacheRemote()) {
			return getRemoteCache().get(key);
		}
		else if (isCacheLocal()) {
			return getEhCache().get(key);
		}
		
		// handle hybrid cache
		HybridKeyCache keyCache = getHybridKeyCache();
		HybridKey keyLocal = keyCache.getKeyLocal(key);
		if (keyCache.isKeyRecent(keyLocal)) { 
			// return a local cached value if the key is still recent
			return getEhCache().get(key);
		}
		
		HybridKey keyRemote = keyCache.getKeyRemote(key);
		if (keyCache.isKeyValid(keyLocal, keyRemote)) {
			// return a local cached value if the key is valid and update its last accessed time
			keyLocal.setAccessTime();
			keyCache.putKeyLocal(key, keyLocal);
			return getEhCache().get(key);
		}
		
		// key is invalid, get remote cached value
		ValueWrapper remoteValueWrapper = getRemoteCache().get(key);
		if (remoteValueWrapper != null) {
			// update local cache value and key
			getEhCache().put(key, remoteValueWrapper.get());
			updateHybridKeyCache(key, keyCache, keyRemote, keyLocal);
		}
		else {
			// cache value has been removed from remote cache
			keyCache.removeBothHybridKeys(key);
			getEhCache().evict(key);
		}
		return remoteValueWrapper;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> type) {
		ValueWrapper remoteValue = get(key);
		if (remoteValue != null) {
			return (T) remoteValue.get();
		}
		return null;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Callable<T> valueLoader) {
		ValueWrapper wrapper = get(key);
		if (wrapper != null && wrapper.get() != null) {
			return (T) wrapper.get();
		}
		T value = null;
		try {
			value = valueLoader.call();
		}
		catch (Throwable ex) {
			throw new ValueRetrievalException(key, valueLoader, ex);
		}
		put(key, value);
		return value;
	}

	@Override
	public String getName() {
		return getEhCache().getName();
	}

	@Override
	public Object getNativeCache() {
		return getEhCache().getNativeCache();
	}

	@Override
	public void put(Object key, Object value) {
		if (isCacheRemote()) {
			getRemoteCache().put(key, value);
			return;
		}
		else if (isCacheLocal()) {
			getEhCache().put(key, value);
			return;
		}
		// handle hybrid cache
		getEhCache().put(key, value);
		getRemoteCache().put(key, value);
		
		// save hybrid keys
		HybridKeyCache keyCache = getHybridKeyCache();
		HybridKey keyRemote = keyCache.getKeyRemote(key);
		HybridKey keyLocal = keyCache.getKeyLocal(key);
		updateHybridKeyCache(key, keyCache, keyRemote, keyLocal);
	}
	
	protected void updateHybridKeyCache(Object key, HybridKeyCache keyCache, HybridKey keyRemote, HybridKey keyLocal){
		if (keyRemote.isUndefined()) {
			keyRemote.nextRevision();
			keyCache.putKeyRemote(key, keyRemote);
		}		
		keyLocal.setRevision(keyRemote.getRevision());
		keyLocal.setAccessTime();
		keyCache.putKeyLocal(key, keyLocal);
	}
	
	/**
	 * TODO: implement hybrid logic here
	 */
	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		if (isCacheRemote()) {
			return getRemoteCache().putIfAbsent(key, value);
		}
		else if (isCacheLocal()) {
			return getEhCache().putIfAbsent(key, value);
		}
		return getRemoteCache().putIfAbsent(key, value);
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
