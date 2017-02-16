/**
 * 
 */
package org.hybricache.testDao;

import java.io.Serializable;
import java.util.Date;

/**
 * The TestBean class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017
 */
public class TestBean  implements Serializable {

	private int id;
	private String name;
	private Date date;
	
	public TestBean() {
		
	}
	
	public TestBean(int id, String name, Date date) {
		this.id = id;
		this.name = name;
		this.date = date;
	}
	
	
	
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDate() {
		return this.date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "{id:" + this.id + ", name: '" + this.name + "', date: " + this.date + " }";
	}


}
