/**
 * 
 */
package org.r3p.cache.hybrid.dao;

import java.util.Date;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 * The TestDaoImpl class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
@Repository("testDao")
public class TestDaoImpl implements TestDao {


	@Cacheable(value="testBeanFindCache", key="#id")
	public TestBean findById(int id) {
		slowQuery(2000L);
		return new TestBean(id, "Test Bean 1", new Date());
	}
	
	@Cacheable(value="testBeanFindCache", key="#name")
	public TestBean findByName(String name) {
		slowQuery(2000L);
		return new TestBean(1, name, new Date());
	}

	private void slowQuery(long seconds){
	    try {
            Thread.sleep(seconds);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
	}

}
