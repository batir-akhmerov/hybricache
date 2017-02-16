/**
 * 
 */
package org.hybricache.needRedisRunning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.hybricache.HybriCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * The BaseTest class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
public class BaseTest {

	@Resource 
	protected HybriCacheManager cacheManager;
	
	protected void test2Caches(String cacheName, String runtimeCacheName) {
		Cache cache = this.cacheManager.getCache(cacheName);
		
		testCacheValues(cacheName, new String[][]{
			new String[]{"1", "Jan"},
			new String[]{"2", "Feb"},
			new String[]{"3", "Mar"},
		});
		
		assertTrue(getCacheValue(cache, "99") == null);
		
		testCacheValues(runtimeCacheName, new String[][]{
			new String[]{"11", "Monday"}
		});
		
	}
	
	protected void testCacheValues(String cacheName, String[][] val) {
		Cache cache = this.cacheManager.getCache(cacheName);
		
		for (String[] obj: val) {
			cache.put(obj[0], obj[1]);
			validateCacheValue(cache, obj[0], obj[1]);
		}
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
	
	protected void testPerformance(String cacheName, String key, int numberOfCacheHits) {
		Cache cache = this.cacheManager.getCache(cacheName);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < numberOfCacheHits; i++) {
			getCacheValue(cache, key);
		}
		long endTime = System.currentTimeMillis();
		System.out.println(String.format(
				"\n\n\n Cache [%s] got hit %d times. Execution took %d milliseconds\n\n\n", 
				cacheName, numberOfCacheHits, endTime - startTime)
		);
	}

}
