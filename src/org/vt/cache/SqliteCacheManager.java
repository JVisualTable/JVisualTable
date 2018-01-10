package org.vt.cache;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sqlite.SQLiteConfig;
import org.vt.util.CacheUtil;
import org.vt.util.DBGeneratorHandle;

import sun.jvmstat.monitor.MonitorException;

/**
 * sqlite cache manager
 * 
 * @author Zerg Law
 * 
 */
public class SqliteCacheManager implements ICacheManager {
    private Logger logger = Logger.getLogger(getClass().getName());
    private String cacheNameHeader = "sqlitecache";
    private String cacheDir = CacheUtil.getUserDir();
    /**
     * cache reflect name
     */
    private Map<String, String> dbMap = new HashMap<String, String>();
    private Map<String, ICache> cacheMap = new HashMap<String, ICache>();

    public SqliteCacheManager() {
	CacheUtil.makeDir(cacheDir);
	clearHistoryCacheFiles();
	try {
	    Class.forName("org.sqlite.JDBC");
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public ICache getCache(String cacheName) {
	return cacheMap.get(cacheName);
    }

    private String createCacheName() {
	String dbName = this.cacheNameHeader + "_" + CacheUtil.getPid() + "_"
		+ System.currentTimeMillis() + ".db";
	while (dbMap.containsValue(dbName)) {
	    try {
		Thread.sleep(1);
	    } catch (InterruptedException e) {
		;
	    }
	    dbName = this.cacheNameHeader + "_" + CacheUtil.getPid() + "_"
		    + System.currentTimeMillis() + ".db";
	}
	return dbName;
    }

    private SQLiteConfig getSQLiteConfig() {
	SQLiteConfig config = new SQLiteConfig();
	// config.setCacheSize(50 * 10000);
	config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
	config.setJournalMode(SQLiteConfig.JournalMode.WAL);

	return config;
    }

    @Override
    public <T> ICache createCache(Class<T> cls, String cacheName) {
	if (cacheMap.containsKey(cacheName)) {
	    throw new RuntimeException("cache name exist:" + cacheName);
	}
	String dbName = createCacheName();
	Connection connection = null;
	Statement statement = null;
	try {
	    connection = DriverManager.getConnection("jdbc:sqlite:"
		    + this.cacheDir + File.separator + dbName,
		    getSQLiteConfig().toProperties());
	    SqliteCache cache = new SqliteCache(cls, cacheName, connection);
	    // connection = DriverManager.getConnection("jdbc:sqlite::memory:");
	    connection.setAutoCommit(false);
	    statement = connection.createStatement();
	    statement.executeUpdate("drop table if exists "
		    + cache.getTableName() + " ;");
	    statement.executeUpdate(new DBGeneratorHandle(cache
		    .getColumnBeans()).createTableSql(cache.getTableName()));
	    connection.commit();
	    dbMap.put(cacheName, dbName);
	    cacheMap.put(cacheName, cache);
	    return cache;
	} catch (SQLException e) {
	    logger.log(Level.WARNING, e.getMessage());
	} finally {
	    if (statement != null) {
		try {
		    statement.close();
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		}
	    }
	}
	throw new RuntimeException("Cache Create failed.");
    }

    @Override
    public void deleteCache(String cacheName) {
	ICache cache = cacheMap.get(cacheName);
	File file = null;
	if (cache != null && cache instanceof SqliteCache) {
	    SqliteCache sqliteCache = (SqliteCache) cache;
	    Statement statement = null;
	    try {
		statement = sqliteCache.getConnection().createStatement();
		statement.executeUpdate("drop table if exists "
			+ sqliteCache.getTableName() + " ;");

		String dbName = dbMap.get(cacheName);
		cacheMap.remove(cacheName);
		dbMap.remove(cacheName);
		file = new File(this.cacheDir + File.separator + dbName);
	    } catch (SQLException ex) {
		logger.log(Level.WARNING, ex.getMessage());
	    } finally {
		try {
		    if (statement != null) {
			statement.close();
		    }
		    if (!sqliteCache.getConnection().isClosed()) {
			sqliteCache.getConnection().close();
		    }
		    sqliteCache.setConnection(null);
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		}
	    }

	    // remove db file
	    if (file != null && file.exists()) {
		if (!file.delete()) {
		    file.deleteOnExit();
		}
	    }
	}
    }

    /**
     * clear history db cache files;
     */
    private void clearHistoryCacheFiles() {
	// cache file name is xxx_pid_xxx
	try {
	    List<Integer> pids = CacheUtil.getAllJavaPid();
	    File[] files = new File(this.cacheDir).listFiles();
	    if (files == null) {
		return;
	    }
	    for (File file : files) {
		if (file.isFile()
			&& file.getName().startsWith(this.cacheNameHeader)
			&& (file.getName().endsWith(".db")
				|| file.getName().endsWith(".db-journal")
				|| file.getName().endsWith(".db-shm") || file
				.getName().endsWith(".db-wal"))) {
		    String[] splits = file.getName().split("_");
		    if (splits.length > 1) {
			if (splits[1].matches("\\d{1,}")) {
			    int processId = Integer.parseInt(splits[1]);
			    if (!pids.contains(processId)) {
				if (!file.delete()) {
				    System.err.println("delete file:"
					    + file.getAbsolutePath()
					    + " failed.");
				}
			    }
			}
		    }
		}
	    }
	} catch (MonitorException e) {
	    throw new RuntimeException(e);
	} catch (URISyntaxException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void shutdown() {
	for (Map.Entry<String, ICache> entry : cacheMap.entrySet()) {
	    deleteCache(entry.getKey());
	}
    }
}
