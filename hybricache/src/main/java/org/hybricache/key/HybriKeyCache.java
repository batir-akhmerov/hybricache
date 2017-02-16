/**
 * 
 */
package org.hybricache.key;

import java.util.concurrent.Callable;

import org.hybricache.BaseCache;
import org.hybricache.HybriCacheConfiguration;
import org.hybricache.remote.RemoteCacheFactory;
import org.springframework.cache.Cache;

import net.sf.ehcache.Ehcache;

/**
 * The HybriKeyCache class
 *
 * @author Batir Akhmerov
 * Created on Jan 27, 2017
 */
public class HybriKeyCache extends BaseCache implements Cache {
	
	protected int keyTrustPeriod;
	
		
	public HybriKeyCache(Ehcache ehCacheNative, HybriCacheConfiguration hybriCacheConfig, RemoteCacheFactory remoteCacheFactory) {
		super(ehCacheNative, hybriCacheConfig, remoteCacheFactory);
		this.keyTrustPeriod = getHybriCacheConfig().getKeyTrustPeriod();
	}
		
	public void removeBothHybriKeys(Object key){
		getEhCache().evict(key);
		getRemoteCache().evict(key);
	}
	
	public HybriKey getKeyRemote(Object key) {
		HybriKey hybriKey = getRemoteCache().get(key, HybriKey.class);
		if (hybriKey == null) {
			hybriKey = new HybriKey();
		}
		return hybriKey;
	}
	public void putKeyRemote(Object key, HybriKey hybriKey) {
		getRemoteCache().put(key, hybriKey);
	}
	
	public HybriKey getKeyLocal(Object key) {
		HybriKey hybriKey = getEhCache().get(key, HybriKey.class);
		if (hybriKey == null) {
			hybriKey = new HybriKey();
		}
		return hybriKey;
	}
	public void putKeyLocal(Object key, HybriKey hybriKey) {
		getEhCache().put(key, hybriKey);
	}
	
	
	public boolean isKeyRecent(HybriKey keyLocal) {
		long currentTime = System.currentTimeMillis();
		boolean isRecent = keyLocal != null && !keyLocal.isUndefined() 
				&& (currentTime - keyLocal.getLastAccessedTime()) < this.keyTrustPeriod;
		
		System.out.println("TIME DIFF: " + (currentTime - keyLocal.getLastAccessedTime()));
		return isRecent;
	}
	public boolean isKeyValid(HybriKey keyLocal, HybriKey keyRemote) {
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
