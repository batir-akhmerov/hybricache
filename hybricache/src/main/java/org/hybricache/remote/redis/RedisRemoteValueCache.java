/**
 * 
 */
package org.hybricache.remote.redis;

import org.hybricache.HybriCacheConfiguration;
import org.hybricache.remote.RemoteValueWrapper;
import org.springframework.data.redis.core.ValueOperations;

/**
 * The RedisRemoteValueCache class
 *
 * @author Batir Akhmerov
 * Created on Feb 1, 2017
 */
public class RedisRemoteValueCache<K, V> extends AbstractRedisRemoteCache<ValueOperations<K, RemoteValueWrapper<V>>, K, V>  {
	
	public RedisRemoteValueCache(HybriCacheConfiguration conf) {
		super(conf);
	}
	
	public ValueOperations<K, RemoteValueWrapper<V>> getCacheTarget() {
		return this.redisTemplate.opsForValue();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> clazz) {
		RemoteValueWrapper<V> wrapper = (RemoteValueWrapper<V>) getCacheTarget().get((K) key); 
		return (T) wrapper.get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public ValueWrapper get(Object key) {
		return getCacheTarget().get((K) key);
	}
	
	

	@Override
	@SuppressWarnings("unchecked")
	public void put(Object key, Object value) {
		getCacheTarget().set((K)key, new RemoteValueWrapper<V>((V)value));
	}

	@Override
	@SuppressWarnings("unchecked")
	public ValueWrapper putIfAbsent(Object key, Object value) {
		RemoteValueWrapper<V> wrapper = new RemoteValueWrapper<V>((V)value);
		getCacheTarget().setIfAbsent((K)key, wrapper);
		return wrapper;
	}

	
}
