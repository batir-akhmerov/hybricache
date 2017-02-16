/**
 * 
 */
package org.hybricache.remote;

import org.hybricache.HybriCacheConfiguration;

/**
 * The RemoteCacheFactory class
 *
 * @author Batir Akhmerov
 * Created on Feb 1, 2017
 */
public interface RemoteCacheFactory {
	
	@SuppressWarnings("rawtypes")
	public RemoteCache getInstance(HybriCacheConfiguration conf);

}
