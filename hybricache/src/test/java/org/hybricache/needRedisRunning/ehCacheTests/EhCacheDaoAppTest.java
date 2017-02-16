/**
 * 
 */
package org.r3p.cache.hybrid;

import org.junit.Test;
import org.r3p.cache.hybrid.dao.TestDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * The EhCacheDaoApp class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
public class EhCacheDaoApp {
	
	private static final Logger log = LoggerFactory.getLogger(EhCacheDaoApp.class);

	@Test
	public void test() {

	    ApplicationContext context = new AnnotationConfigApplicationContext(TestAppConfig.class);
	    TestDao obj = (TestDao) context.getBean("testDao");

	    log.debug("Result : {}", obj.findByName("foo"));
	    log.debug("Result : {}", obj.findByName("foo"));
	    log.debug("Result : {}", obj.findByName("foo"));
	    
	    ((ConfigurableApplicationContext)context).close();

	}
	
}
