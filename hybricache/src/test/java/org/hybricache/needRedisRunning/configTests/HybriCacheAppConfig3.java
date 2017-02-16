/**
 * 
 */
package org.hybricache.needRedisRunning.configTests;

import java.util.ArrayList;
import java.util.List;

import org.hybricache.HybriCacheConfiguration;
import org.hybricache.HybriCacheConfiguration.CacheType;
import org.hybricache.needRedisRunning.ehCacheTests.TestAppConfig;
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
public class HybriCacheAppConfig3 extends TestAppConfig {
	

	
	
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