/**
 * 
 */
package org.r3p.cache.hybrid.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.r3p.cache.hybrid.BaseTest;
import org.r3p.cache.hybrid.HybridCacheConfiguration;
import org.r3p.cache.hybrid.HybridCacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

/**
 * The BaseTest class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = {HybridCacheAppConfig2.class})
public class HybridCacheConfigTest2 extends BaseTest {
	
	@Test
	public void test() {
		
		List<HybridCacheConfiguration> confList = this.cacheManager.getHybridCacheConfigurationList();
		
		assertFalse(CollectionUtils.isEmpty(confList));
		assertEquals(3, confList.size());
		assertEquals("appCache", confList.get(0).getCacheName());
		assertEquals(HybridCacheManager.CACHE_KEY, confList.get(1).getCacheName());
		assertEquals(HybridCacheManager.CACHE_COMMON, confList.get(2).getCacheName());
			
		test2Caches("appCache", "runtimeCache");
		
		confList = this.cacheManager.getHybridCacheConfigurationList();
		
		assertFalse(CollectionUtils.isEmpty(confList));
		assertEquals(4, confList.size());
		assertEquals("runtimeCache", confList.get(3).getCacheName());
		
	}
	
}
