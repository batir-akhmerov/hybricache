/**
 * 
 */
package org.r3p.cache.hybrid.remote;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.r3p.cache.hybrid.HybridCache;
import org.r3p.cache.hybrid.HybridCacheConfiguration;
import org.r3p.cache.hybrid.HybridCacheManager;
import org.r3p.cache.hybrid.HybridCacheConfiguration.CacheType;
import org.r3p.cache.hybrid.remote.redis.RedisRemoteValueCache;
import org.r3p.cache.hybrid.TestAppConfig;
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
	public HybridCacheManager cacheManager() {
		String host = this.env.getProperty("remote.cache.server.host");
		String port = this.env.getProperty("remote.cache.server.port");
		Integer intPort = null;
		if (port != null) {
			intPort = Integer.parseInt(port);
		}		
		HybridCacheManager cacheManager =  new HybridCacheManager( new EhCacheCacheManager(ehCacheCacheManager().getObject()), host,  intPort);
		cacheManager.setHybridCacheConfigurationList(hybridCacheConfigurationList());
		cacheManager.setDefaultHybridCacheConfiguration(defaultHybridCacheConfiguration());
		
		 RemoteCache remoteCache = EasyMock.createMock(RedisRemoteValueCache.class);
		 ((HybridCache)cacheManager.getCache("remoteCache")).setRemoteCache(remoteCache);
		
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
	public List<HybridCacheConfiguration> hybridCacheConfigurationList() {
		List<HybridCacheConfiguration> list = new ArrayList<>();
		list.add( new HybridCacheConfiguration(
				"appCache",
				CacheType.LOCAL,
				0
			)
		);
		list.add( new HybridCacheConfiguration(
				"remoteCache",
				CacheType.REMOTE,
				0
			)
		);
		return list;
		
	}
	
}