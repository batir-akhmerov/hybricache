/**
 * 
 */
package org.r3p.cache.hybrid.key;

import java.util.concurrent.Callable;

import org.r3p.cache.hybrid.BaseCache;
import org.r3p.cache.hybrid.HybridCacheConfiguration;
import org.r3p.cache.hybrid.remote.RemoteCacheFactory;
import org.springframework.cache.Cache;

import net.sf.ehcache.Ehcache;

/**
 * The HybridKeyCache class
 *
 * @author Batir Akhmerov
 * Created on Jan 27, 2017
 */
public class HybridKeyCache extends BaseCache implements Cache {
	
	protected int keyTrustPeriod;
	
		
	public HybridKeyCache(Ehcache ehCacheNative, HybridCacheConfiguration hybridCacheConfig, RemoteCacheFactory remoteCacheFactory) {
		super(ehCacheNative, hybridCacheConfig, remoteCacheFactory);
		this.keyTrustPeriod = getHybridCacheConfig().getKeyTrustPeriod();
	}
		
	public void removeBothHybridKeys(Object key){
		getEhCache().evict(key);
		getRemoteCache().evict(key);
	}
	
	public HybridKey getKeyRemote(Object key) {
		HybridKey hybridKey = getRemoteCache().get(key, HybridKey.class);
		if (hybridKey == null) {
			hybridKey = new HybridKey();
		}
		return hybridKey;
	}
	public void putKeyRemote(Object key, HybridKey hybridKey) {
		getRemoteCache().put(key, hybridKey);
	}
	
	public HybridKey getKeyLocal(Object key) {
		HybridKey hybridKey = getEhCache().get(key, HybridKey.class);
		if (hybridKey == null) {
			hybridKey = new HybridKey();
		}
		return hybridKey;
	}
	public void putKeyLocal(Object key, HybridKey hybridKey) {
		getEhCache().put(key, hybridKey);
	}
	
	
	public boolean isKeyRecent(HybridKey keyLocal) {
		long currentTime = System.currentTimeMillis();
		boolean isRecent = keyLocal != null && !keyLocal.isUndefined() 
				&& (currentTime - keyLocal.getLastAccessedTime()) < this.keyTrustPeriod;
		
		System.out.println("TIME DIFF: " + (currentTime - keyLocal.getLastAccessedTime()));
		return isRecent;
	}
	public boolean isKeyValid(HybridKey keyLocal, HybridKey keyRemote) {
		return keyLocal != null && keyRemote != null
				&& !keyLocal.isUndefined() && !keyRemote.isUndefined()
				&& keyLocal.getRevision() == keyRemote.getRevision();
	}
	
	
	
	
	
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getNativeCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueWrapper get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Object key, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(Object key, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void evict(Object key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public int getKeyTrustPeriod() {
		return this.keyTrustPeriod;
	}

	public void setKeyTrustPeriod(int keyTrustPeriod) {
		this.keyTrustPeriod = keyTrustPeriod;
	}
	
	
	
	
	

}
