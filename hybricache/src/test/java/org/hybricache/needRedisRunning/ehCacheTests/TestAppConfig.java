/**
 * 
 */
package org.hybricache.needRedisRunning.ehCacheTests;

import java.util.List;

import javax.inject.Inject;

import org.hybricache.HybriCacheConfiguration;
import org.hybricache.HybriCacheManager;
import org.hybricache.HybriCacheConfiguration.CacheType;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;


/**
 * The HybriCacheAppConfig1 class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
@Configuration
@ComponentScan({ "org.r3p.cache.*" })
@PropertySource("classpath:application.properties")
public class TestAppConfig {
	
	@Inject protected Environment env;
	
	
	@Bean
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
		return cacheManager;
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheCacheManager() {
		EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
		cmfb.setConfigLocation(new ClassPathResource("ehcache.xml"));
		cmfb.setShared(true);
		return cmfb;
	}
	
	@Bean
	public HybriCacheConfiguration defaultHybriCacheConfiguration() {
		int maxEntriesLocalHeap = 0;
		
		return new HybriCacheConfiguration(
			CacheType.HYBRID,
			1000,
			new CacheConfiguration("defaultCache", maxEntriesLocalHeap)
				.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
				.eternal(false)
				.timeToLiveSeconds(60)
				.timeToIdleSeconds(30)
				.diskExpiryThreadIntervalSeconds(0)
				.persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP))
		);
		
	}
	
	
	@Bean
	public List<HybriCacheConfiguration> hybriCacheConfigurationList() {
		return null;
	}
	
}