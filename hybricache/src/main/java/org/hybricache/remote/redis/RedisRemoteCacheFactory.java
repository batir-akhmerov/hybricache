/**
 * 
 */
package org.r3p.cache.hybrid.remote.redis;

import org.r3p.cache.hybrid.HybridCacheConfiguration;
import org.r3p.cache.hybrid.HybridCacheConfiguration.CacheMode;
import org.r3p.cache.hybrid.remote.RemoteCache;
import org.r3p.cache.hybrid.remote.RemoteCacheFactory;

/**
 * The RedisRemoteCacheFactory class
 *
 * @author Batir Akhmerov
 * Created on Feb 1, 2017
 */
public class RedisRemoteCacheFactory implements RemoteCacheFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public RemoteCache getInstance(HybridCacheConfiguration conf) {
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
