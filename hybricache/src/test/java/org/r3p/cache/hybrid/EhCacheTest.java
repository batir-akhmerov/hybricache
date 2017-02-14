/**
 * 
 */
package org.r3p.cache.hybrid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The BaseTest class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})
public class EhCacheTest {

	@Resource HybridCacheManager cacheManager;
	
	@Test
	public void test() {
		
		//ApplicationContext context = new AnnotationConfigApplicationContext();
		//HybridCacheManager hybridCacheManager = (HybridCacheManager) context.getBean("cacheManager");
	
		Cache cache = cacheManager.getCache("localCache");
	
		cache.put("1", "Jan");
		cache.put("2", "Feb");
		cache.put("3", "Mar");
		
		
		validateCacheValue(cache, "1", "Jan");
		validateCacheValue(cache, "2", "Feb");
		validateCacheValue(cache, "3", "Mar");
	
		assertTrue(getCacheValue(cache, "99") == null);
		
		//Configuration conf = hybridCacheManager.getConfiguration();
		
		cache = cacheManager.getCache("runtimeCache");
		cache.put("11", "Monday");
		validateCacheValue(cache, "11", "Monday");
		
		//((ConfigurableApplicationContext)context).close();
	}
	
	protected void validateCacheValue(Cache cache, String key, String expectedValue){
		String cachedValue = getCacheValue(cache, key);
		assertEquals(expectedValue, cachedValue);
	}
	
	protected String getCacheValue(Cache cache, String key){
		ValueWrapper vw = cache.get(key);
		String cachedValue = (vw == null ? null : vw.get().toString());
		System.out.println(cachedValue);
		return cachedValue;
	}

}
