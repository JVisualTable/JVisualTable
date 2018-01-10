package org.vt.cache;

/**
 * cache manager interface
 * 
 * @author Zerg Law
 * 
 */
public interface ICacheManager {

    /**
     * Get Cache by name
     * 
     * @param cacheName
     * @return
     */
    public ICache getCache(String cacheName);

    /**
     * create cache
     * 
     * @param <T>
     *            t
     * @param cls
     *            cls
     * @param cacheName
     *            cache name
     * @return cache
     */
    public <T> ICache createCache(Class<T> cls, String cacheName);

    /**
     * delete cache
     * 
     * @param cacheName
     *            cacheName
     * 
     */
    public void deleteCache(String cacheName);

    /**
     * close cache,and destroy resource.
     */
    public void shutdown();
}
