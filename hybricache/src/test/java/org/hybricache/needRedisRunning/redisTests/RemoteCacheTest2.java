/**
 * 
 */
package org.hybricache.needRedisRunning.redisTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.hybricache.HybriCacheConfiguration;
import org.hybricache.needRedisRunning.BaseRemoteTest;
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
@ContextConfiguration(classes = {RemoteCacheAppConfig1.class})
public class RemoteCacheTest2 extends BaseRemoteTest {
	
	@Test
	public void test() {
		String cacheName = "redisCache";
		
		List<HybriCacheConfiguration> confList = this.cacheManager.getHybriCacheConfigurationList();
		assertFalse(CollectionUtils.isEmpty(confList));
		
		HybriCacheConfiguration remoteConf = confList.get(1);
		assertEquals(cacheName, remoteConf.getCacheName());
		
		initRedis(remoteConf);
		
		String[][] testValues = new String[][]{
			new String[]{"1", "Jan"},
			new String[]{"2", "Feb"},
			new String[]{"3", "Mar"},
		};
		
		testCacheValues(cacheName, testValues);
		
		testRedisTemplateCacheValues(testValues);
		
	}
	
}
