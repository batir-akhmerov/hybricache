/**
 * 
 */
package org.r3p.cache.hybrid.remote;

import org.springframework.cache.Cache;

/**
 * The RemoteCache class
 *
 * @author Batir Akhmerov
 * Created on Feb 1, 2017
 */
public interface RemoteCache<C> extends Cache {
	
	public C getCacheTarget();

}
