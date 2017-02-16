/**
 * 
 */
package org.hybricache.needRedisRunning.ehCacheTests;

import javax.annotation.Resource;

import org.hybricache.HybriCacheManager;
import org.hybricache.needRedisRunning.BaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The BaseTest class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = {TestAppConfig.class})
public class EhCacheTest  extends BaseTest {

	@Resource HybriCacheManager cacheManager;
	
	@Test
	public void test() {
		String cacheName = "localCache";
		
		String[][] testValues = new String[][]{
			new String[]{"1", "Jan"},
			new String[]{"2", "Feb"},
			new String[]{"3", "Mar"},
		};
		
		testCacheValues(cacheName, testValues);
	}
	
}
