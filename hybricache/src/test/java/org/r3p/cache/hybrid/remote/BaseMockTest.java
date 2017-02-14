/**
 * 
 */
package org.r3p.cache.hybrid.remote;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.r3p.cache.hybrid.HybridCache;
import org.r3p.cache.hybrid.HybridCacheConfiguration;
import org.r3p.cache.hybrid.key.HybridKey;
import org.r3p.cache.hybrid.key.HybridKeyCache;
import org.r3p.cache.hybrid.remote.redis.RedisRemoteValueCache;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.ehcache.EhCacheCache;

/**
 * The BaseTest class
 *
 * @author Batir Akhmerov
 * Created on Jan 26, 2017 
 */
public class BaseMockTest extends EasyMockSupport {

	protected HybridCache hybridCache;
	protected EhCacheCache ehCache;
	protected RemoteCache remoteCache;
	
	protected HybridKeyCache hybridKeyCache;
	
	protected HybridCacheConfiguration hybridConfig;
	
	protected HybridKey hybridKeyRemote;
	protected HybridKey hybridKeyLocal;
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
		this.hybridConfig = new HybridCacheConfiguration();
		
		this.hybridKeyCache = //createMock(HybridKeyCache.class);
		
				EasyMock
		         .createMockBuilder(HybridKeyCache.class) //create builder first
		         .addMockedMethod("getKeyLocal") 
		         .addMockedMethod("getKeyRemote")
		         .addMockedMethod("putKeyLocal") 
		         .addMockedMethod("putKeyRemote")
		         .addMockedMethod("removeBothHybridKeys")
		         
		         .createMock();          // create the partial mock object
		
		
        
        this.hybridCache = new HybridCache(this.ehCache, this.hybridConfig, new RemoteCacheFactory(){
			@Override
			public RemoteCache getInstance(HybridCacheConfiguration conf) {
				return remoteCache;
			}
		}, this.hybridKeyCache);
	}
       
    @After
    public void tearDown() {    	
        verifyAll();
    }

    @Test
    public void test_NoValue_NoKey() {
    	this.hybridKeyCache.setKeyTrustPeriod(100);
    	
    	EasyMock.expect(this.remoteCache.get(this.key)).andReturn(null).anyTimes();
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(null).anyTimes();
	    this.ehCache.evict(this.key);
	    EasyMock.expectLastCall().once();
	    EasyMock.replay(this.ehCache);
		
        
    	this.hybridKeyRemote = new HybridKey();
    	this.hybridKeyLocal = new HybridKey();
    	
    	EasyMock.expect(this.hybridKeyCache.getKeyLocal(this.key)).andReturn(this.hybridKeyLocal).times(1);
		EasyMock.expect(this.hybridKeyCache.getKeyRemote(this.key)).andReturn(this.hybridKeyRemote).times(1);
		
		this.hybridKeyCache.removeBothHybridKeys(this.key);
	    EasyMock.expectLastCall().once();
		EasyMock.replay(this.hybridKeyCache);
		
    	
    	validateCacheValue(this.hybridCache, this.key, null);
    }
    
    @Test
    public void test_RemoteValue() {
    	this.hybridKeyCache.setKeyTrustPeriod(100);
    	
    	EasyMock.expect(this.remoteCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(null).anyTimes();
		this.ehCache.put(this.key, this.value);
	    EasyMock.expectLastCall().once();        
        EasyMock.replay(this.ehCache);
    	
    	
    	this.hybridKeyRemote = new HybridKey(1);
    	this.hybridKeyLocal = new HybridKey();
    	
    	EasyMock.expect(this.hybridKeyCache.getKeyLocal(this.key)).andReturn(this.hybridKeyLocal).times(1);
		EasyMock.expect(this.hybridKeyCache.getKeyRemote(this.key)).andReturn(this.hybridKeyRemote).times(1);
		
		this.hybridKeyCache.putKeyLocal(EasyMock.isA(String.class), EasyMock.isA(HybridKey.class));
	    EasyMock.expectLastCall().once();
	    this.hybridKeyCache.putKeyRemote(EasyMock.isA(String.class), EasyMock.isA(HybridKey.class));
	    EasyMock.expectLastCall().once();
		
		EasyMock.replay(this.hybridKeyCache);
    	
		// take remotely cached value and save it in local cache
    	validateCacheValue(this.hybridCache, this.key, this.value);
    }
    
    @Test
    public void test_LocalValueWithRecentKey() {
    	this.hybridKeyCache.setKeyTrustPeriod(1000);
    	
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
		EasyMock.replay(this.ehCache);
    	
    	
    	this.hybridKeyRemote = new HybridKey(1);
    	this.hybridKeyLocal = new HybridKey(1);
    	
    	pauseTest(50);
    	
    	EasyMock.expect(this.hybridKeyCache.getKeyLocal(this.key)).andReturn(this.hybridKeyLocal).times(1);
		EasyMock.replay(this.hybridKeyCache);
    	
		// return local cache since its key is recent
    	validateCacheValue(this.hybridCache, this.key, this.value);
    }
    
    @Test
    public void test_LocalValueWithValidKey() {
    	this.hybridKeyCache.setKeyTrustPeriod(100);
    	
    	EasyMock.expect(this.remoteCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
		this.ehCache.put(this.key, this.value);
	    EasyMock.expectLastCall().once();        
        EasyMock.replay(this.ehCache);
    	
    	
    	this.hybridKeyRemote = new HybridKey(1);
    	this.hybridKeyLocal = new HybridKey(1);
    	
    	pauseTest(50);
    	
    	EasyMock.expect(this.hybridKeyCache.getKeyLocal(this.key)).andReturn(this.hybridKeyLocal).times(1);
		EasyMock.expect(this.hybridKeyCache.getKeyRemote(this.key)).andReturn(this.hybridKeyRemote).times(1);
	
		this.hybridKeyCache.putKeyLocal(EasyMock.isA(String.class), EasyMock.isA(HybridKey.class));
	    EasyMock.expectLastCall().once();
		EasyMock.replay(this.hybridKeyCache);
    	
		// return local cache since its key is recent
    	validateCacheValue(this.hybridCache, this.key, this.value);
    }
	
    @Test
    public void test_LocalValueOutdated() {
    	this.hybridKeyCache.setKeyTrustPeriod(100);
    	
    	EasyMock.expect(this.remoteCache.get(this.key)).andReturn(new RemoteValueWrapper<String>(this.value)).anyTimes();
    	EasyMock.replay(this.remoteCache);
		
		EasyMock.expect(this.ehCache.get(this.key)).andReturn(new RemoteValueWrapper<String>("JanOutdated")).anyTimes();
		this.ehCache.put(this.key, this.value);
	    EasyMock.expectLastCall().once();        
        EasyMock.replay(this.ehCache);
    	
    	
    	this.hybridKeyRemote = new HybridKey(2);
    	this.hybridKeyLocal = new HybridKey(1);
    	
    	pauseTest(0);
    	
    	EasyMock.expect(this.hybridKeyCache.getKeyLocal(this.key)).andReturn(this.hybridKeyLocal).times(1);
		EasyMock.expect(this.hybridKeyCache.getKeyRemote(this.key)).andReturn(this.hybridKeyRemote).times(1);
	
		this.hybridKeyCache.putKeyLocal(EasyMock.isA(String.class), EasyMock.isA(HybridKey.class));
	    EasyMock.expectLastCall().once();
		EasyMock.replay(this.hybridKeyCache);
    	
		// return local cache since its key is recent
    	validateCacheValue(this.hybridCache, this.key, this.value);
    }
    
    
    
    
    
    
    
	
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
