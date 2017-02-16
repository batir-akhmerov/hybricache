/**
 * 
 */
package org.hybricache.mocked;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.hybricache.HybriCache;
import org.hybricache.HybriCacheConfiguration;
import org.hybricache.key.HybriKey;
import org.hybricache.key.HybriKeyCache;
import org.hybricache.remote.RemoteCache;
import org.hybricache.remote.RemoteCacheFactory;
import org.hybricache.remote.RemoteValueWrapper;
import org.hybricache.remote.redis.RedisRemoteValueCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.ehcache.EhCacheCache;

/**
 * The BaseTest class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017 
 */
public class HybriCacheTest extends EasyMockSupport {

	protected HybriCache hybriCache;
	protected EhCacheCache ehCache;
	@SuppressWarnings("rawtypes")
	protected RemoteCache remoteCache;
	
	protected HybriKeyCache hybriKeyCache;
	
	protected HybriCacheConfiguration hybriConfig;
	
	protected HybriKey hybriKeyRemote;
	protected HybriKey hybriKeyLocal;
	protected String cacheName;
	
	protected String key;
	protected String value;
	
	@Before
    public void setUp() {
		this.cacheName = "remoteCache";
		this.key = "1";
		this.value = "Jan";
		
		this.remoteCache = EasyMock.createMock(RedisRemoteValueCache.class);
		this.ehCache = EasyMock.createMock(EhCacheCache.class);
		this.hybriConfig = new HybriCacheConfiguration();
		
		this.hybriKeyCache = //createMock(HybriKeyCache.class);
		
				EasyMock
		         .createMockBuilder(HybriKeyCache.class) //create builder first
		         .addMockedMethod("getKeyLocal") 
		         .addMockedMethod("getKeyRemote")
		         .addMockedMethod("putKeyLocal") 
		         .addMockedMethod("putKeyRemote")
		         .addMockedMethod("removeBothHybriKeys")
		         
		         .createMock();          // create the partial mock object
		
		
        
        this.hybriCache = new HybriCache(this.ehCache, this.hybriConfig, new RemoteCacheFactory(){
			@Override
			@SuppressWarnings("rawtypes")
			public RemoteCache getInstance(HybriCacheConfiguration conf) {
				return HybriCacheTest.this.remoteCache;
			}
		}, this.hybriKeyCache);
	}
       
    @After
    public void tearDown() {    	
        verifyAll();
    }

    @Test
    public void test_NoValue_NoKey() {
    	this.hybriKeyCache.setKeyTrustPeriod(100);
    	
    	EasyMock.expect(this.remoteCache.get(this.key)).andReturn(null).anyTimes();
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(null).anyTimes();
	    this.ehCache.evict(this.key);
	    EasyMock.expectLastCall().once();
	    EasyMock.replay(this.ehCache);
		
        
    	this.hybriKeyRemote = new HybriKey();
    	this.hybriKeyLocal = new HybriKey();
    	
    	EasyMock.expect(this.hybriKeyCache.getKeyLocal(this.key)).andReturn(this.hybriKeyLocal).times(1);
		EasyMock.expect(this.hybriKeyCache.getKeyRemote(this.key)).andReturn(this.hybriKeyRemote).times(1);
		
		this.hybriKeyCache.removeBothHybriKeys(this.key);
	    EasyMock.expectLastCall().once();
		EasyMock.replay(this.hybriKeyCache);
		
    	
    	validateCacheValue(this.hybriCache, this.key, null);
    }
    
    @Test
    public void test_RemoteValue() {
    	this.hybriKeyCache.setKeyTrustPeriod(100);
    	
    	EasyMock.expect(this.remoteCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(null).anyTimes();
		this.ehCache.put(this.key, this.value);
	    EasyMock.expectLastCall().once();        
        EasyMock.replay(this.ehCache);
    	
    	
    	this.hybriKeyRemote = new HybriKey(1);
    	this.hybriKeyLocal = new HybriKey();
    	
    	EasyMock.expect(this.hybriKeyCache.getKeyLocal(this.key)).andReturn(this.hybriKeyLocal).times(1);
		EasyMock.expect(this.hybriKeyCache.getKeyRemote(this.key)).andReturn(this.hybriKeyRemote).times(1);
		
		this.hybriKeyCache.putKeyLocal(EasyMock.isA(String.class), EasyMock.isA(HybriKey.class));
	    EasyMock.expectLastCall().once();
	    this.hybriKeyCache.putKeyRemote(EasyMock.isA(String.class), EasyMock.isA(HybriKey.class));
	    EasyMock.expectLastCall().once();
		
		EasyMock.replay(this.hybriKeyCache);
    	
		// take remotely cached value and save it in local cache
    	validateCacheValue(this.hybriCache, this.key, this.value);
    }
    
    @Test
    public void test_LocalValueWithRecentKey() {
    	this.hybriKeyCache.setKeyTrustPeriod(1000);
    	
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
		EasyMock.replay(this.ehCache);
    	
    	
    	this.hybriKeyRemote = new HybriKey(1);
    	this.hybriKeyLocal = new HybriKey(1);
    	
    	pauseTest(50);
    	
    	EasyMock.expect(this.hybriKeyCache.getKeyLocal(this.key)).andReturn(this.hybriKeyLocal).times(1);
		EasyMock.replay(this.hybriKeyCache);
    	
		// return local cache since its key is recent
    	validateCacheValue(this.hybriCache, this.key, this.value);
    }
    
    @Test
    public void test_LocalValueWithValidKey() {
    	this.hybriKeyCache.setKeyTrustPeriod(100);
    	
    	EasyMock.expect(this.remoteCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
		this.ehCache.put(this.key, this.value);
	    EasyMock.expectLastCall().once();        
        EasyMock.replay(this.ehCache);
    	
    	
    	this.hybriKeyRemote = new HybriKey(1);
    	this.hybriKeyLocal = new HybriKey(1);
    	
    	pauseTest(50);
    	
    	EasyMock.expect(this.hybriKeyCache.getKeyLocal(this.key)).andReturn(this.hybriKeyLocal).times(1);
		EasyMock.expect(this.hybriKeyCache.getKeyRemote(this.key)).andReturn(this.hybriKeyRemote).times(1);
	
		this.hybriKeyCache.putKeyLocal(EasyMock.isA(String.class), EasyMock.isA(HybriKey.class));
	    EasyMock.expectLastCall().once();
		EasyMock.replay(this.hybriKeyCache);
    	
		// return local cache since its key is recent
    	validateCacheValue(this.hybriCache, this.key, this.value);
    }
	
    /* TODO: Enable and Fix
    @Test
    public void test_LocalValueOutdated() {
    	this.hybriKeyCache.setKeyTrustPeriod(100);
    	
    	EasyMock.expect(this.remoteCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(new RemoteValueWrapper<String>("JanOutdated")).anyTimes();
		this.ehCache.put(this.key, this.value);
	    EasyMock.expectLastCall().once();        
        EasyMock.replay(this.ehCache);
    	
    	
    	this.hybriKeyRemote = new HybriKey(2);
    	this.hybriKeyLocal = new HybriKey(1);
    	
    	pauseTest(0);
    	
    	EasyMock.expect(this.hybriKeyCache.getKeyLocal(this.key)).andReturn(this.hybriKeyLocal).times(1);
		EasyMock.expect(this.hybriKeyCache.getKeyRemote(this.key)).andReturn(this.hybriKeyRemote).times(1);
	
		this.hybriKeyCache.putKeyLocal(EasyMock.isA(String.class), EasyMock.isA(HybriKey.class));
	    EasyMock.expectLastCall().once();
		EasyMock.replay(this.hybriKeyCache);
    	
		// return local cache since its key is recent
    	validateCacheValue(this.hybriCache, this.key, this.value);
    }
    */
    
    
    
    
    
    
	
	protected void validateCacheValue(Cache cache, String key, String expectedValue){
		String cachedValue = getCacheValue(cache, key);
		assertEquals(expectedValue, cachedValue);
	}
	
	protected String getCacheValue(Cache cache, String key){
		ValueWrapper vw = cache.get(key);
		String cachedValue = (vw == null ? null : vw.get().toString());
		System.out.println(cachedValue);
		return cachedValue;
	}
	
	protected void pauseTest(long milisecs) {
		try {
			Thread.sleep(50);
    	}
    	catch(Exception e) {
    		throw new RuntimeException(e);
    	}
	}
	
}
