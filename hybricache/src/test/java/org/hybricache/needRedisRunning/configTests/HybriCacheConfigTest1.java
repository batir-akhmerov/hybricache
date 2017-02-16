/**
 * 
 */
package org.hybricache.needRedisRunning.configTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.hybricache.HybriCacheConfiguration;
import org.hybricache.HybriCacheManager;
import org.hybricache.needRedisRunning.BaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@ContextConfiguration(classes = {HybriCacheAppConfig1.class})
public class HybriCacheConfigTest1 extends BaseTest {
	
	@Test
	public void test() {
		
		List<HybriCacheConfiguration> confList = this.cacheManager.getHybriCacheConfigurationList();
		
		assertFalse(CollectionUtils.isEmpty(confList));
		assertEquals(4, confList.size());
		
		assertEquals(HybriCacheManager.CACHE_KEY, confList.get(0).getCacheName());
		assertEquals(HybriCacheManager.CACHE_COMMON, confList.get(1).getCacheName());
		assertEquals("appCache", confList.get(2).getCacheName());
		
		test2Caches("appCache", "runtimeCache");
			
		confList = this.cacheManager.getHybriCacheConfigurationList();
		
		assertFalse(CollectionUtils.isEmpty(confList));
		assertEquals(6, confList.size());
		assertEquals("runtimeCache", confList.get(4).getCacheName());
		
	}
	
}
