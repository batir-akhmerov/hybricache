/**
 * 
 */
package org.hybricache.needRedisRunning.ehCacheTests;

import org.hybricache.testDao.TestDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * The EhCacheDaoAppTest class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
public class EhCacheDaoAppTest {
	
	private static final Logger log = LoggerFactory.getLogger(EhCacheDaoAppTest.class);

	//@Test
	public void test() {

	    ApplicationContext context = new AnnotationConfigApplicationContext(TestAppConfig.class);
	    TestDao obj = (TestDao) context.getBean("testDao");

	    log.debug("Result : {}", obj.findByName("foo"));
	    log.debug("Result : {}", obj.findByName("foo"));
	    log.debug("Result : {}", obj.findByName("foo"));
	    
	    ((ConfigurableApplicationContext)context).close();

	}
	
}
