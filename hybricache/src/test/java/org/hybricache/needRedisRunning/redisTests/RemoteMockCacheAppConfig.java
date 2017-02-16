/**
 * 
 */
package org.hybricache.needRedisRunning.redisTests;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.hybricache.HybriCache;
import org.hybricache.HybriCacheConfiguration;
import org.hybricache.HybriCacheManager;
import org.hybricache.HybriCacheConfiguration.CacheType;
import org.hybricache.needRedisRunning.ehCacheTests.TestAppConfig;
import org.hybricache.remote.RemoteCache;
import org.hybricache.remote.redis.RedisRemoteValueCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;


/**
 * The LocalCacheAppConfig1 class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
@Configuration
@ComponentScan({ "org.r3p.cache.*" })
@PropertySource("classpath:application.properties")
public class RemoteMockCacheAppConfig extends TestAppConfig {

	@Bean
	@SuppressWarnings("rawtypes")
	public HybriCacheManager cacheManager() {
		String host = this.env.getProperty("remote.cache.server.host");
		String port = this.env.getProperty("remote.cache.server.port");
		Integer intPort = null;
		if (port != null) {
			intPort = Integer.parseInt(port);
		}		
		HybriCacheManager cacheManager =  new HybriCacheManager( new EhCacheCacheManager(ehCacheCacheManager().getObject()), host,  intPort);
		cacheManager.setHybriCacheConfigurationList(hybriCacheConfigurationList());
		cacheManager.setDefaultHybriCacheConfiguration(defaultHybriCacheConfiguration());
		
		 
		RemoteCache remoteCache = EasyMock.createMock(RedisRemoteValueCache.class);
		((HybriCache)cacheManager.getCache("remoteCache")).setRemoteCache(remoteCache);
		
		return cacheManager;
	}
	
	@Bean
	public EhCacheManagerFactoryBean ehCacheCacheManager() {
		EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
		cmfb.setConfigLocation(new ClassPathResource("ehcacheSimple.xml"));
		cmfb.setShared(true);
		return cmfb;
	}
	
	
	@Bean
	public List<HybriCacheConfiguration> hybriCacheConfigurationList() {
		List<HybriCacheConfiguration> list = new ArrayList<>();
		list.add( new HybriCacheConfiguration(
				"appCache",
				CacheType.LOCAL,
				0
			)
		);
		list.add( new HybriCacheConfiguration(
				"remoteCache",
				CacheType.REMOTE,
				0
			)
		);
		return list;
		
	}
	
}