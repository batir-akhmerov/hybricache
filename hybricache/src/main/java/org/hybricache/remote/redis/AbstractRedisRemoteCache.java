/**
 * 
 */
package org.hybricache.remote.redis;

import java.util.concurrent.Callable;

import org.hybricache.HybriCacheConfiguration;
import org.hybricache.remote.RemoteCache;
import org.hybricache.remote.RemoteValueWrapper;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * The AbstractRedisRemoteCache class
 *
 * @author Batir Akhmerov
 * Created on Feb 1, 2017
 */
public abstract class AbstractRedisRemoteCache<C, K, V> implements RemoteCache<C> {
	
	protected JedisConnectionFactory jedisConnectionFactory;
	protected RedisTemplate<K, RemoteValueWrapper<V>> redisTemplate;
	protected HybriCacheConfiguration config;
	
	public AbstractRedisRemoteCache(HybriCacheConfiguration conf) {
		this.config = conf;
		
		this.jedisConnectionFactory = new JedisConnectionFactory();
		if (conf.getDatabaseIndex() != null) {
			this.jedisConnectionFactory.setDatabase(conf.getDatabaseIndex());
		}
		this.jedisConnectionFactory.setHostName(conf.getRemoteSeverHost());
		this.jedisConnectionFactory.setPort(conf.getRemoteServerPort());
		this.jedisConnectionFactory.setUsePool(true);
		this.jedisConnectionFactory.afterPropertiesSet();
		
		this.redisTemplate = new RedisTemplate<>();
		this.redisTemplate.setConnectionFactory(this.jedisConnectionFactory);
		this.redisTemplate.setKeySerializer(new StringRedisSerializer());
		this.redisTemplate.afterPropertiesSet();
	}
	

	@Override
	public void clear() {
		this.jedisConnectionFactory.getConnection().flushAll();
		
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public void evict(Object key) {
		this.redisTemplate.delete((K) key);
		
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
		return getConfig().getCacheName();
	}

	@Override
	public Object getNativeCache() {
		return getCacheTarget();
	}


	public HybriCacheConfiguration getConfig() {
		return this.config;
	}

	public void setConfig(HybriCacheConfiguration config) {
		this.config = config;
	}


}
