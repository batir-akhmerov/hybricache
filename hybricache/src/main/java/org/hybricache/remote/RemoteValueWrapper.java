/**
 * 
 */
package org.r3p.cache.hybrid.remote;

import java.io.Serializable;

import org.springframework.cache.Cache.ValueWrapper;

/**
 * The RemoteValueWrapper class
 *
 * @author Batir Akhmerov
 * Created on Feb 1, 2017
 */
public class RemoteValueWrapper<T> implements ValueWrapper, Serializable {
	
	private T value;
	
	public RemoteValueWrapper(T value) {
		this.value = value;
	}
	@Override
	public Object get() {
		return getValue();
	}
	
	public T getValue() {
		return this.value;
	}
	public void setValue(T value) {
		this.value = value;
	}

}
