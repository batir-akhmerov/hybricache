/**
 * 
 */
package org.r3p.cache.hybrid.remote.redis;

import static org.junit.Assert.assertFalse;

import org.r3p.cache.hybrid.HybridCacheConfiguration;
import org.r3p.cache.hybrid.remote.RemoteValueWrapper;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.util.StringUtils;

/**
 * The RedisRemoteHashCache class
 *
 * @author Batir Akhmerov
 * Created on Feb 1, 2017
 */
public class RedisRemoteHashCache<K, V> extends AbstractRedisRemoteCache<HashOperations<K, K, RemoteValueWrapper<V>>, K, V>  {
	
	private String hashCacheName;
	
	public RedisRemoteHashCache(HybridCacheConfiguration conf) {
		super(conf);
		assertFalse(StringUtils.isEmpty(conf.getHashCacheName()));
		this.hashCacheName = conf.getHashCacheName();
	}
	
	public HashOperations<K, K, RemoteValueWrapper<V>> getCacheTarget() {
		return this.redisTemplate.opsForHash();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> clazz) {
		RemoteValueWrapper<V> wrapper = (RemoteValueWrapper<V>) getCacheTarget().get(getHashCacheName(), key); 
		return (T) wrapper.get();
	}

	@Override
	public ValueWrapper get(Object key) {
		return getCacheTarget().get(getHashCacheName(), key);
	}
	
	

	@Override
	@SuppressWarnings("unchecked")
	public void put(Object key, Object value) {
		getCacheTarget().put(getHashCacheName(), (K)key, new RemoteValueWrapper<V>((V)value));
	}

	@Override
	@SuppressWarnings("unchecked")
	public ValueWrapper putIfAbsent(Object key, Object value) {
		RemoteValueWrapper<V> wrapper = new RemoteValueWrapper<V>((V)value);
		getCacheTarget().putIfAbsent(getHashCacheName(), (K)key, wrapper);
		return wrapper;
	}

	@SuppressWarnings("unchecked")
	public K getHashCacheName() {
		return (K) this.hashCacheName;
	}

	public void setHashCacheName(K hashCacheName) {
		this.hashCacheName = String.valueOf(hashCacheName);
	}
}
