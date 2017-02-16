/**
 * 
 */
package org.hybricache.remote.redis;

import org.hybricache.HybriCacheConfiguration;
import org.hybricache.HybriCacheConfiguration.CacheMode;
import org.hybricache.remote.RemoteCache;
import org.hybricache.remote.RemoteCacheFactory;

/**
 * The RedisRemoteCacheFactory class
 *
 * @author Batir Akhmerov
 * Created on Feb 1, 2017
 */
public class RedisRemoteCacheFactory implements RemoteCacheFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public RemoteCache getInstance(HybriCacheConfiguration conf) {
		RemoteCache remoteCache = null;
		if (conf.getCacheMode() == CacheMode.HASH) {
			remoteCache = new RedisRemoteHashCache(conf);
		}
		else {
			remoteCache = new RedisRemoteValueCache(conf);
		}
		try {
			remoteCache.clear();
		}
		catch(Exception ex) {
			throw new RuntimeException("Cannot establish Redis connection!", ex);
		}
		return remoteCache;
	}

}
