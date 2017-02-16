/**
 * 
 */
package org.r3p.cache.hybrid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.r3p.cache.hybrid.remote.RemoteValueWrapper;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * The BaseTest class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
public class BaseRemoteTest extends BaseTest {
	
	protected JedisConnectionFactory jedisConnectionFactory;
	protected RedisTemplate<String, RemoteValueWrapper<Object>> redisTemplate;
	protected HybridCacheConfiguration config;
	
	public RedisTemplate<String, RemoteValueWrapper<Object>> initRedis(HybridCacheConfiguration conf) {
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
		
		return this.redisTemplate;
	}
	
	protected void testRedisTemplateCacheValues(String[][] val) {
		ValueOperations<String, RemoteValueWrapper<Object>> cache = getCacheValueTarget();
		
		for (String[] obj: val) {
			RemoteValueWrapper<Object> wrapper = cache.get(obj[0]);
			assertNotNull(wrapper);
			assertEquals(obj[1], wrapper.get());
		}
	}
	
	public HashOperations<String, String, RemoteValueWrapper<Object>> getCacheHashTarget() {
		return this.redisTemplate.opsForHash();
	}
	
	public ValueOperations<String, RemoteValueWrapper<Object>> getCacheValueTarget() {
		return this.redisTemplate.opsForValue();
	}

}
