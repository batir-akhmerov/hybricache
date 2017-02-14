/**
 * 
 */
package org.r3p.cache.hybrid.dao;

/**
 * The TestDao class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
public interface TestDao {

	TestBean findById(int id);
	
	TestBean findByName(String name);
	
}
