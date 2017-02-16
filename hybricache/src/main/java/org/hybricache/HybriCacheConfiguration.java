/**
 * 
 */
package org.r3p.cache.hybrid;

import net.sf.ehcache.config.CacheConfiguration;

/**
 * The HybridCacheConfiguration class
 *
 * @author Batir Akhmerov
 * Created on Jan 27, 2017
 */
public class HybridCacheConfiguration {
	
	public enum CacheType{HYBRID, LOCAL, REMOTE};
	public enum CacheMode{VALUE, HASH};
	public static final int KEY_TRUST_PERIOD_ALWAYS = 0;
	public static final int KEY_TRUST_PERIOD_1SEC = 1000;
	
	private String cacheName;
	private String hashCacheName;
	private CacheConfiguration ehCacheConfiguration;
	private CacheType cacheType = CacheType.HYBRID;
	private int keyTrustPeriod = KEY_TRUST_PERIOD_ALWAYS;
	private String remoteSeverHost;
	private Integer remoteServerPort;
	private Integer databaseIndex;
	private CacheMode cacheMode = CacheMode.VALUE; 
	
	public HybridCacheConfiguration() {
		
	}
	
	public HybridCacheConfiguration(String cacheName, CacheType cacheType, int keyTrustPeriod) {
		this(cacheName, cacheType, keyTrustPeriod, null);
	}
	
	public HybridCacheConfiguration(CacheType cacheType, int keyTrustPeriod, CacheConfiguration ehCacheConfiguration) {
		this(ehCacheConfiguration.getName(), cacheType, keyTrustPeriod, ehCacheConfiguration);
	}
	
	public HybridCacheConfiguration(String cacheName, CacheType cacheType, int keyTrustPeriod, CacheConfiguration ehCacheConfiguration) {
		this.cacheName = cacheName;
		this.cacheType = cacheType;
		this.keyTrustPeriod = keyTrustPeriod;
		this.ehCacheConfiguration = ehCacheConfiguration;
	}
	
	
	
	
	/**
	 * Returns EhCache CacheConfiguration 
	 * @return the ehCacheConfiguration
	 */
	public CacheConfiguration getEhCacheConfiguration() {
		return this.ehCacheConfiguration;
	}
	/**
	 * Sets EhCache CacheConfiguration
	 * @param ehCacheConfiguration the ehCacheConfiguration to set
	 */
	public void setEhCacheConfiguration(CacheConfiguration ehCacheConfiguration) {
		this.ehCacheConfiguration = ehCacheConfiguration;
	}
	/**
	 * Returns a type of this cache. Can be either:
	 * HYBRID - objects are cached in both local EhCache and remote caching server. Objects are synced using key revision and key trust periods.  
	 * LOCAL - objects are cached in local EhCache server.
	 * REMOTE - objects are cached in remote caching server.
	 * @return the cacheType
	 */
	public CacheType getCacheType() {
		return this.cacheType;
	}
	/**
	 * Sets a type of cache.
	 * @param cacheType the cacheType to set
	 */
	public void setCacheType(CacheType cacheType) {
		this.cacheType = cacheType;
	}
	/**
	 * Returns a time period in milliseconds (since last key sync) during which local key revisions are not validated against revisions on the remote caching server. 
	 * This helps to speed up cache GET operations by reducing network latency. Only applies when cache type is HYBRID.  
	 * 
	 * @return the keyTrustPeriod
	 */
	public int getKeyTrustPeriod() {
		return this.keyTrustPeriod;
	}
	/**
	 * Sets a time period in milliseconds during which local key revisions can be trusted. 
	 * @param keyTrustPeriod the keyTrustPeriod to set
	 */
	public void setKeyTrustPeriod(int keyTrustPeriod) {
		this.keyTrustPeriod = keyTrustPeriod;
	}

	/**
	 *
	 * @return the cacheName
	 */
	public String getCacheName() {
		return this.cacheName;
	}

	/**
	 *
	 * @param cacheName the cacheName to set
	 */
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	@Override
	public int hashCode() {
		return getCacheName().hashCode();
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HybridCacheConfiguration) {
			return getCacheName().equals(((HybridCacheConfiguration)obj).getCacheName());
		}
		return false;
	}

	@Override
	protected HybridCacheConfiguration clone() {
		HybridCacheConfiguration conf = new HybridCacheConfiguration(this.cacheName, this.cacheType, this.keyTrustPeriod, this.ehCacheConfiguration);
		conf.setRemoteServerPort(this.getRemoteServerPort());
		conf.setRemoteSeverHost(this.getRemoteSeverHost());
		conf.setDatabaseIndex(this.getDatabaseIndex());
		conf.setCacheMode(this.getCacheMode());
		conf.setHashCacheName(this.getHashCacheName());
		return conf;
	}

	/**
	 *
	 * @return the remoteSeverHost
	 */
	public String getRemoteSeverHost() {
		return this.remoteSeverHost;
	}

	/**
	 *
	 * @param remoteSeverHost the remoteSeverHost to set
	 */
	public void setRemoteSeverHost(String remoteSeverHost) {
		this.remoteSeverHost = remoteSeverHost;
	}

	/**
	 *
	 * @return the remoteServerPort
	 */
	public Integer getRemoteServerPort() {
		return this.remoteServerPort;
	}

	/**
	 *
	 * @param remoteServerPort the remoteServerPort to set
	 */
	public void setRemoteServerPort(Integer remoteServerPort) {
		this.remoteServerPort = remoteServerPort;
	}

	/**
	 *
	 * @return the databaseIndex
	 */
	public Integer getDatabaseIndex() {
		return this.databaseIndex;
	}

	/**
	 *
	 * @param databaseIndex the databaseIndex to set
	 */
	public void setDatabaseIndex(Integer databaseIndex) {
		this.databaseIndex = databaseIndex;
	}

	/**
	 *
	 * @return the cacheMode
	 */
	public CacheMode getCacheMode() {
		return this.cacheMode;
	}

	/**
	 *
	 * @param cacheMode the cacheMode to set
	 */
	public void setCacheMode(CacheMode cacheMode) {
		this.cacheMode = cacheMode;
	}

	public String getHashCacheName() {
		return this.hashCacheName;
	}

	public void setHashCacheName(String hashCacheName) {
		this.hashCacheName = hashCacheName;
	}

}
