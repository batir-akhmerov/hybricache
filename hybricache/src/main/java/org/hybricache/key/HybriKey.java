/**
 * 
 */
package org.hybricache.key;

import java.io.Serializable;

/**
 * The HybriKey class
 *
 * @author Batir Akhmerov
 * Created on Feb 3, 2017
 */
public class HybriKey implements Serializable {
	public static final long UNDEFINED_KEY = 0;
	public static final long UNDEFINED_TIME = 0;
	
	private long revision = UNDEFINED_KEY;
	private long lastAccessedTime = UNDEFINED_TIME;
	
	public HybriKey() {
	}
	public HybriKey(long revision) {
		this.revision = revision;
		if (!isUndefined()) {
			setAccessTime();
		} 
	}
	
	public boolean isUndefined() {
		return getRevision() == UNDEFINED_KEY;
	}
	
	public long nextRevision() {
		return ++this.revision;
	}
	public void setAccessTime() {
		this.lastAccessedTime = System.currentTimeMillis();
	}
	
	public long getRevision() {
		return this.revision;
	}
	public void setRevision(long revision) {
		this.revision = revision;
	}
	public long getLastAccessedTime() {
		return this.lastAccessedTime;
	}
	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

}
