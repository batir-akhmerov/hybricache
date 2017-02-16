/**
 * 
 */
package org.r3p.cache.hybrid;

import java.util.List;

import javax.inject.Inject;

import org.r3p.cache.hybrid.HybridCacheConfiguration.CacheType;
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
 * The HybridCacheAppConfig1 class
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
	public HybridCacheConfiguration defaultHybridCacheConfiguration() {
		int maxEntriesLocalHeap = 0;
		
		return new HybridCacheConfiguration(
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
	public List<HybridCacheConfiguration> hybridCacheConfigurationList() {
		return null;
	}
	
	/*
	@Bean
	public List<HybridCacheConfiguration> hybridCacheConfigurationList() {
		int maxEntriesLocalHeap = 0;
		List<HybridCacheConfiguration> list = new ArrayList<>();
		
		list.add( new HybridCacheConfiguration(
				"testBeanFindCache",
				CacheType.HYBRID,
				1000
			)
		);
		
		list.add( new HybridCacheConfiguration(
				CacheType.LOCAL,
				0,
				new CacheConfiguration("localCache", maxEntriesLocalHeap)
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
	*/
	
}