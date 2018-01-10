package org.vt.cache;

/**
 * cache manager
 * 
 * @author Zerg Law
 * 
 */
public class CacheManager implements ICacheManager {
    private static CacheManager instance = null;
    private ICacheManager cacheManager;

    private CacheManager(ICacheManager cacheManager) {
	this.cacheManager = cacheManager;
    }

    public static synchronized CacheManager getInstance() {
	if (instance == null) {
	    instance = new CacheManager(new SqliteCacheManager());
	}
	return instance;
    }

    @Override
    public ICache getCache(String cacheName) {
	return this.cacheManager.getCache(cacheName);
    }

    @Override
    public <T> ICache createCache(Class<T> cls, String cacheName) {
	return this.cacheManager.createCache(cls, cacheName);
    }

    @Override
    public void deleteCache(String cacheName) {
	this.cacheManager.deleteCache(cacheName);
    }

    @Override
    public void shutdown() {
	this.cacheManager.shutdown();
    }

}
