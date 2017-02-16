/**
 * 
 */
package org.r3p.cache.hybrid.remote;

import org.r3p.cache.hybrid.HybridCacheConfiguration;

/**
 * The RemoteCacheFactory class
 *
 * @author Batir Akhmerov
 * Created on Feb 1, 2017
 */
public interface RemoteCacheFactory {
	
	@SuppressWarnings("rawtypes")
	public RemoteCache getInstance(HybridCacheConfiguration conf);

}
