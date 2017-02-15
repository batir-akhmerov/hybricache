# HybriCache
HybriCache is a hybrid cache solution based on EhCache and Redis. HybriCache is almost as fast as EhCache and Redis utilization makes HybriCache a Cluster Environment friendly. HybriCache is ideal solution if you’re switching over to a clustered environment of Amazon Web Service and need a fast caching library. If your application is already configured for EhCache – that’s even easier, HybriCache uses existing EhCache configuration, just add HybriCache jar to the classpath, replace EhCacheManager with HybriCacheManager in a spring context and set Elasticache server’s Host and Port to the HybriCacheManager instance. And that is all.

# What is wrong with AWS Elasticache? 
AWS Elasticache Redis is a non-sql in-memory database and:

1.	It’s located in a separate server instance, which is being accessed over TCP/IP protocol.

2.	Every time you put/get an object to or from Redis it takes some time to serialize/deserialize object’s instance.

So if your application makes a couple of dozens cache hits per server response – you’d probably be better off with just AWS Elasticache alone.  

But if a number of cache hits is around of few hundreds or even thousands - you will face a performance decrease and longer page load time. This is especially noticeable when comparing a pure EhCache caching with a pure AWS Elasticache caching. We tested some of our pages with 3000 cache hits per server response on AWS Beanstalk instance for just EhCahce and just Elasticache Redis:

1.	EhCache showed an average page load time of 1+ seconds. 

2.	AWS Elasticache Redis was 8-9 seconds.

Things did not get much better after upgrading to the top-end memory optimized Elasticache instance with high network performance – page load went to 5-6 seconds which is still not acceptable for a production site.

HybriCache source code contains 2 performance tests where we check execution time of 3000 get object cache operations in 2 modes:

1.	Local (only EhCache is enabled). Result: Cache [ehCache] got hit 3000 times. Execution took 37 milliseconds

2.	Remote (only Redis cache is enabled). Result: Cache [redisCache] got hit 3000 times. Execution took 576 milliseconds.

As you can see above - EhCache is almost 15 times faster than Redis caching.
(Some other tests you can find online show 10 times difference, but anyway)

So, what is wrong with AWS Elasticache?  - Network Latency and Time needed for object Serialization.


# Why HybriCache is faster than AWS Elasticache Redis?
HybriCache utilizes a hybrid cache model when EhCache and Redis work together caching objects on both sides:

1.	Caching Locally (EhCache) on a webserver instance for faster access.

2.	Caching Remotely on AWS Elasticache (Redis) for clustering support.

Cached Objects are synchronized using Revisions:

-	If a Local Cache Revision matches a Remote Cache Revision – Cached Object is extracted from a fast Local Cache (EhCache).

-	Otherwise Cached Object is taken from a Remote Cache (Redis) and HybriCache updates Local Cache along with its Revision.

“Key Trust Period” is another mechanism that makes HybriCache even faster. “Key Trust Period” is a time period in milliseconds in which HybriCache can trust the Local Cache Revision. This is useful when objects are rarely changed in Cache but might be extracted from HybriCache more than once during one server’s response. Setting “Key Trust Period” can prevent redundant calls to Redis trying to verify Remote Cache Revision. “Key Trust Period” default value is 1000 milliseconds and it can be set per each Cache Database.
