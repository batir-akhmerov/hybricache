/**
 * 
 */
package org.hybricache;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Callable;

import org.hybricache.key.HybriKey;
import org.hybricache.key.HybriKeyCache;
import org.hybricache.remote.RemoteCache;
import org.hybricache.remote.RemoteCacheFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;

import net.sf.ehcache.Ehcache;

/**
 * The HybriKeyCache class
 *
 * @author Batir Akhmerov
 * Created on Jan 27, 2017
 */
public class HybriCache extends BaseCache implements Cache{
	
	private HybriKeyCache hybriKeyCache;
	
	
	public HybriCache(Ehcache ehCacheNative, HybriCacheConfiguration hybriCacheConfig, 
			RemoteCacheFactory remoteCacheFactory, HybriKeyCache hybriKeyCache) {
		super(ehCacheNative, hybriCacheConfig, remoteCacheFactory);
		initHybriKeyCache(hybriKeyCache);
	}
	
	public HybriCache(EhCacheCache ehCache, HybriCacheConfiguration hybriCacheConfig, 
			RemoteCacheFactory remoteCacheFactory, HybriKeyCache hybriKeyCache) {
		super(ehCache, hybriCacheConfig, remoteCacheFactory);		
		initHybriKeyCache(hybriKeyCache);
	}

	public void initHybriKeyCache(HybriKeyCache hybriKeyCache) {
		String cacheName = getHybriCacheConfig().getCacheName();
		if (isCacheHybrid() && !HybriCacheManager.CACHE_KEY.equals(cacheName) && !HybriCacheManager.CACHE_COMMON.equals(cacheName)) {
			assertNotNull(hybriKeyCache);
			this.hybriKeyCache = hybriKeyCache;
		}
	}
	
	@Override
	public void clear() {
		getEhCache().clear();	
		getRemoteCache().clear();
		if (isCacheHybrid()) {
			getHybriKeyCache().clear();
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
		
		// handle hybri cache
		HybriKeyCache keyCache = getHybriKeyCache();
		HybriKey keyLocal = keyCache.getKeyLocal(key);
		if (keyCache.isKeyRecent(keyLocal)) { 
			// return a local cached value if the key is still recent
			return getEhCache().get(key);
		}
		
		HybriKey keyRemote = keyCache.getKeyRemote(key);
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
			updateHybriKeyCache(key, keyCache, keyRemote, keyLocal);
		}
		else {
			// cache value has been removed from remote cache
			keyCache.removeBothHybriKeys(key);
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
		// handle hybri cache
		getEhCache().put(key, value);
		getRemoteCache().put(key, value);
		
		// save hybri keys
		HybriKeyCache keyCache = getHybriKeyCache();
		HybriKey keyRemote = keyCache.getKeyRemote(key);
		HybriKey keyLocal = keyCache.getKeyLocal(key);
		updateHybriKeyCache(key, keyCache, keyRemote, keyLocal);
	}
	
	protected void updateHybriKeyCache(Object key, HybriKeyCache keyCache, HybriKey keyRemote, HybriKey keyLocal){
		if (keyRemote.isUndefined()) {
			keyRemote.nextRevision();
			keyCache.putKeyRemote(key, keyRemote);
		}		
		keyLocal.setRevision(keyRemote.getRevision());
		keyLocal.setAccessTime();
		keyCache.putKeyLocal(key, keyLocal);
	}
	
	/**
	 * TODO: implement hybri logic here
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
