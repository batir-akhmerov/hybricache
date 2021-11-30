<a name="top"></a>
# HybriCache
HybriCache is a hybrid cache solution based on EhCache and Redis. HybriCache is almost as fast as EhCache and utilization of Redis makes HybriCache a Cluster friendly. HybriCache is ideal solution if you’re switching over to a clustered environment on AWS and need a fast caching library. If your application is already configured for EhCache – that’s even easier, HybriCache uses existing EhCache configuration, just add HybriCache jar to the classpath, replace EhCacheManager with HybriCacheManager in a spring context and set Elasticache server’s Host and Port to the HybriCacheManager instance. And that is all.

## Table of Content
* [What is wrong with AWS Elasticache?](#whats_wrong_elasticache)
* [Why HybriCache is faster than AWS Elasticache Redis?](#hybricache_faster)
* [HybriCache Cache Type](#hybricache_type)
* [Getting Started](#getting_started)
  * [Download HybriCache Jar](#download_jar)
  * [Download dependencies](#download_other_jar)
  * [Include Jars](#include_jar)
  * [Configure Caches](#configure_caches)
  * [Declare Cache Manager](#declare_cache_manager)
  * [Set Environment Properties](#env_properties)

<a name="whats_wrong_elasticache"></a>
## What is wrong with AWS Elasticache? 
AWS Elasticache Redis is a non-sql in-memory database and:

1.	Network Latency. Redis is being accessed over TCP/IP protocol and often located on a separate server instance.

2.	Every time you put/get an object to or from Redis it takes some time to serialize/deserialize object’s instance.

So if your application makes a couple of dozens cache hits per server response – you’d probably be better off with just AWS Elasticache alone.  

But if a number of cache hits is around of few hundreds or even thousands - you will face a performance decrease and longer page load time. This is especially noticeable when comparing a pure EhCache caching with a pure AWS Elasticache caching. We tested some pages within one of our applications with 3000 cache hits per page load on AWS Beanstalk instance for just EhCahce and just Elasticache Redis:

1.	EhCache showed an average page load time of 1+ seconds. 

2.	AWS Elasticache Redis was 8-9 seconds.

Things did not get much better after upgrading to the top-end memory optimized Elasticache instance type with high network performance – page load goes to 5-6 seconds which is still not acceptable for a production site.

HybriCache source code contains 2 performance tests where we check execution time of 3000 GET Object calls in 2 modes:

1.	Local (only EhCache is enabled). Result: Execution took 37 milliseconds

2.	Remote (only Redis cache is enabled). Result: Execution took 576 milliseconds.

As you can see - EhCache is almost 15 times faster than Redis caching.
(Some other tests you can find online show 10 times difference)

So, what is wrong with AWS Elasticache?  - Network Latency + Time needed for object Serialization, along with single-threaded nature of Redis.

[Top](#top)

<a name="hybricache_faster"></a>
## Why HybriCache is faster than AWS Elasticache Redis?
HybriCache  is NOT a replacement for AWS Elasticache nor EhCache. HybriCache utilizes a hybrid cache model when both EhCache and Redis work together caching objects:

1.	Caching Locally (EhCache) on a webserver instance for faster access.

2.	Caching Remotely on AWS Elasticache (Redis) for clustering support.

Cached Objects are replicated using Revisions:

*	If a Local Cache Revision matches a Remote Cache Revision – Cached Object is extracted from a fast Local Cache (EhCache).

*	Otherwise Cached Object is taken from a Remote Cache (Redis) and HybriCache updates Local Cache.

“Key Trust Period” is another mechanism that makes HybriCache even faster. “Key Trust Period” is a time period in milliseconds in which HybriCache can trust the Local Cache Revision. This is useful when objects are rarely changed in Cache but might be extracted from HybriCache more than once during one page load. Setting “Key Trust Period” can prevent redundant calls to Redis to verify the Remote Cache Revision - our tests shows that even such a simple call "GET Integer Key from Redis" made 3000 times dramatically affects performance. “Key Trust Period” defaults to 1000 milliseconds and can be overridden for each Cache Database.

[Top](#top)

<a name="hybricache_type"></a>
## HybriCache Cache Types
Each Cache Database can be set to one of 3 Cash Strategies(Types) supported by HybriCache:

1. HYBRID - default cache type utilizing hybrid caching model (see above).
2. LOCAL - caches objects ONLY on local EhCache instance running on a webserver (one of instances in a cluster). No cache replication is supported by this type of cache. A fastest type of cache. Useful for caches when synchronization with a cluster is not critical but speed is important, e.g. caching of Merged CSS and JS resources.
3. REMOTE - caches objects ONLY on remote AWS Elasticache Redis instance. Clustering is supported. Slowest cache type. Used for object caches when speed is not critical but the latest cache revision is highly important, e.g. caching of User’s Session metadata.

[Top](#top)

<a name="getting_started"></a>
## Getting Started
<a name="download_jar"></a>
1. Download the latest [hybricache.jar]( https://github.com/batir-akhmerov/hybricache/raw/master/hybricache/target/hybricache-0.0.1.jar)

<a name="download_other_jar"></a>
2. Download other dependencies jars (if your application does not use them yet). A full list of dependencies is in [pom.xml](https://raw.githubusercontent.com/batir-akhmerov/hybricache/master/hybricache/pom.xml)

<a name="include_jar"></a>
3. Include jars into a .classpath file:

   ```
   <classpathentry kind="lib" path="hybricache.jar"/>
   <!-- include all other dependencies jars if needed -->
   ```
   
<a name="configure_caches"></a>    
4. Configure Caches. If your application already uses EhCache then by default no additional configuration is needed. HybriCache will configure itself for all EhCache Caches set in ehcache.xml. Here is a sample ehcache.xml:

   ```
      <ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:noNamespaceSchemaLocation="ehcache.xsd"
	updateCheck="true"
	monitoring="autodetect"
	dynamicConfig="true">
	
	<cache name="mergedCssJsCache" ...>
		...
	</cache>
		
	<cache name="userSessionCache" ...>
		...
	</cache>
		
	<cache name="daoBeanCache" ...>
		...
	</cache>
	
      </ehcache>
   ```

   All 3 caches above will be configured with HybriCache defaults: HYBRID type and 1000 milliseconds Key Trust Period.

   To declare custom HybriCache configuration for certain/all pre-configured EhCaches – provide HybriCache configuration via hybriCacheConfigurationList list in spring-context.xml or ApplicationConfiguration class:

   ```
  @Configuration
  public class AppContextConfiguration  {	
	@Bean
	public List<HybriCacheConfiguration> hybriCacheConfigurationList() {
		List<HybriCacheConfiguration> list = new ArrayList<>();
		list.add( new HybriCacheConfiguration(
				"mergedCssJsCache",
				CacheType.LOCAL,
				0
			)
		);
		list.add( new HybriCacheConfiguration(
				"userSessionCache",
				CacheType.REMOTE,
				0
			)
		);
		return list;		
	}	
}
```
   Here mergedCssJsCache is configured as a LOCAL cache and userSessionCache as REMOTE. 
   Cache daoBeanCache will be a HYBRID cache by default.
   
<a name="declare_cache_manager"></a> 
5. Declare default application cacheManager. If ehCacheManager was a default manager - rename it since it's needed for HybriCache, otherwise declare ehCahceManager too:

   ```
@Configuration
public class AppContextConfiguration {
	
	@Inject protected Environment env;
	
	@Bean
	public HybriCacheManager cacheManager() {
		String host = this.env.getProperty("remote.cache.server.host");
		String port = this.env.getProperty("remote.cache.server.port");
		Integer intPort = null;
		if (port != null) {
			intPort = Integer.parseInt(port);
		}		
		HybriCacheManager cacheManager =  new HybriCacheManager( new EhCacheCacheManager(ehCacheCacheManager().getObject()), host,  intPort);
		cacheManager.setHybriCacheConfigurationList(hybriCacheConfigurationList());
		return cacheManager;
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheCacheManager() {
		EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
		cmfb.setConfigLocation(new ClassPathResource("ehcache.xml"));
		cmfb.setShared(true);
		return cmfb;
	}
  
  @Bean
	public List<HybriCacheConfiguration> hybriCacheConfigurationList() {
		...
	}	
}

   ```
<a name="env_properties"></a> 
6. Add AWS Elasticache Redis host name and port into environment properties.

   ```
   remote.cache.server.host=https://[url.to elasticache.redis.instance].amazonaws.com
   remote.cache.server.port=6379
   ```
   
[Top](#top)
