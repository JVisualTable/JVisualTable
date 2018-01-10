package org.vt.cache;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;

import org.vt.beans.SearchCondition;
import org.vt.cache.orm.ColumnBean;
import org.vt.util.CacheUtil;
import org.vt.util.DBGeneratorHandle;
import org.vt.util.IdProductor;

/**
 * sqlite cache
 * 
 * @author Zerg Law
 * 
 */
public class SqliteCache<T> implements ICache<T> {
    private Logger logger = Logger.getLogger(getClass().getName());
    private String name;
    private DBGeneratorHandle handle;
    private Connection connection;
    private String tableName = "vtablecache";
    private Class cls;
    private ColumnBean[] columnBeans;
    private IdProductor productor = new IdProductor();

    private static String writeLock = "";

    public SqliteCache(Class<T> cls, String name, Connection connection) {
	this.cls = cls;
	try {
	    this.columnBeans = DBGeneratorHandle.checkAndUpdate(CacheUtil
		    .createColumnBeans(cls));
	} catch (InstantiationException e) {
	    throw new RuntimeException(e);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	}
	this.handle = new DBGeneratorHandle(columnBeans);
	this.name = name;
	this.connection = connection;
    }

    @Override
    public String getName() {
	return this.name;
    }

    @Override
    public int[] getKeys() {
	return getKeys(null, null);
    }

    @Override
    public int getSize() {
	PreparedStatement prep = null;
	ResultSet rs = null;
	int size = 0;
	try {
	    if (connection == null) {
		return 0;
	    }
	    // long t1 = System.currentTimeMillis();
	    String sql = "select count(*) from " + tableName;
	    prep = connection.prepareStatement(sql);
	    rs = prep.executeQuery();
	    // long t2 = System.currentTimeMillis();
	    while (rs.next()) {
		size = rs.getInt(1);
		break;
	    }
	    // long t3 = System.currentTimeMillis();
	    // System.out.println("sql:" + sql + (t2 - t1) + "," + (t3 - t2));
	} catch (SQLException ex) {
	    logger.log(Level.WARNING, ex.getMessage());
	    ;
	} finally {
	    if (rs != null) {
		try {
		    rs.close();
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		    ;
		}
	    }
	    if (prep != null) {
		try {
		    prep.close();
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		    ;
		}
	    }
	}
	return size;
    }

    @Override
    public List<Element<T>> get(int[] keys) {
	Map<Integer, Vector> map = new HashMap<Integer, Vector>();
	if (connection == null) {
	    return new ArrayList<Element<T>>();
	}
	PreparedStatement prep = null;
	ResultSet rs = null;
	// long t1 = System.currentTimeMillis();
	try {
	    String sql = "select * from " + this.tableName
		    + " where primaryKeyId in(" + getSqlIds(keys) + ")";
	    prep = connection.prepareStatement(sql);
	    rs = prep.executeQuery();

	    while (rs.next()) {
		Vector<Object> list = new Vector<Object>();
		int primaryKey = rs.getInt(1);
		for (Map.Entry<ColumnBean, String> entry : handle
			.getSqlColumnMap().entrySet()) {
		    if (ColumnBean.DEFAULT_REFLECT_COLUMN_NAME.equals(entry
			    .getValue())) {
			continue;
		    }
		    list.add(handle.getDBValue(entry.getKey(), rs
			    .getObject(entry.getValue())));
		}
		map.put(primaryKey, list);
	    }
	} catch (SQLException ex) {
	    logger.log(Level.WARNING, ex.getMessage());
	    ;
	} finally {
	    if (rs != null) {
		try {
		    rs.close();
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		    ;
		}
	    }
	    if (prep != null) {
		try {
		    prep.close();
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		    ;
		}
	    }
	}
	List<Element<T>> elements = new ArrayList<Element<T>>();
	Map<Integer, T> elementMaps;
	try {
	    elementMaps = CacheUtil.toBeans(cls, map);
	    for (int i = 0; i < keys.length; i++) {
		T t = elementMaps.get(keys[i]);
		if (t != null) {
		    elements.add(new Element(keys[i], t));
		}
	    }
	} catch (InstantiationException e) {
	    throw new RuntimeException(e);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}

	// long t2 = System.currentTimeMillis();
	// System.out.println("查询数:" + keys.length + " 耗时:" + (t2 - t1));
	return elements;

    }

    private List<Integer> getUserNewIds(int size) {
	List<Integer> ids = productor.getIds(size);
	return ids.isEmpty() ? productor.getNewIds(size, getKeys()) : ids;
    }

    @Override
    public boolean add(List<Element<T>> elements) {
	synchronized (writeLock) {
	    PreparedStatement prep = null;
	    try {
		if (connection == null) {
		    return false;
		}
		connection.setAutoCommit(false);
		prep = connection.prepareStatement("replace into "
			+ this.tableName + " values ("
			+ handle.getParamCreateInputSql() + ");");
		try {
		    Vector<Vector> vectors = CacheUtil.toDBVectors(elements);
		    Map<Integer, Object> addMap = new HashMap<Integer, Object>();
		    List<Integer> ids = getUserNewIds(elements.size());
		    for (int i = 0; i < elements.size(); i++) {
			Vector data = vectors.get(i);
			// prep.setObject(1,
			// element.getKey() == Integer.MIN_VALUE ? null
			// : element.getKey());
			prep.setObject(1, ids.get(i));
			for (int j = 0; j < data.size(); j++) {
			    Object obj = data.get(j);
			    if (obj != null && obj instanceof Date) {
				SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
				obj = format.format((Date) obj);
			    }
			    prep.setObject(j + 2, obj);
			}
			Method idGetMethod = CacheUtil
				.getIdGetMethod(cls, true);
			Object id = idGetMethod.invoke(elements.get(i)
				.getData());
			elements.get(i).setKey(ids.get(i));
			addMap.put(ids.get(i), id);
			prep.addBatch();
		    }
		    prep.executeBatch();
		    connection.commit();
		    prep.clearBatch();

		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
		return true;
	    } catch (SQLException ex) {
		logger.log(Level.WARNING, ex.getMessage());
		;
	    } finally {
		if (prep != null) {
		    try {
			prep.close();
		    } catch (SQLException ex) {
			logger.log(Level.WARNING, ex.getMessage());
			;
		    }
		}
	    }
	    return false;
	}
    }

    @Override
    public boolean update(Map<Integer, Element<T>> elementMap) {
	synchronized (writeLock) {
	    PreparedStatement prep = null;
	    try {
		if (connection == null) {
		    return false;
		}
		connection.setAutoCommit(false);
		prep = connection.prepareStatement("update " + this.tableName
			+ " set  " + handle.getParamUpdateInputSql()
			+ " where primaryKeyId=?;");

		for (Map.Entry<Integer, Element<T>> entry : elementMap
			.entrySet()) {
		    Vector data = CacheUtil
			    .toVector(entry.getValue().getData());
		    for (int i = 0; i < data.size(); i++) {
			prep.setObject(i + 1, data.get(i));
		    }
		    prep.setObject(data.size() + 1, entry.getKey());
		    prep.addBatch();
		}
		prep.executeBatch();
		connection.commit();
		prep.clearBatch();
		return true;
	    } catch (SQLException ex) {
		logger.log(Level.WARNING, ex.getMessage());
		;
	    } finally {
		if (prep != null) {
		    try {
			prep.close();
		    } catch (SQLException ex) {
			logger.log(Level.WARNING, ex.getMessage());
			;
		    }
		}
	    }
	    return false;
	}
    }

    @Override
    public int removeKeys(int columnIndex, Object[] values) {
	synchronized (writeLock) {
	    PreparedStatement prep = null;
	    ResultSet rs = null;
	    try {
		if (connection == null) {
		    return 0;
		}
		String sql = "delete  from " + this.tableName + " where "
			+ this.handle.getColumnNameByIndex(columnIndex)
			+ " in(" + handle.createKeys(values) + ")";
		// System.out.println("removeKeys sql:" + sql);
		prep = connection.prepareStatement(sql);
		return prep.executeUpdate();
	    } catch (SQLException ex) {
		logger.log(Level.WARNING, ex.getMessage());
		;
	    } finally {
		if (rs != null) {
		    try {
			rs.close();
		    } catch (SQLException ex) {
			logger.log(Level.WARNING, ex.getMessage());
			;
		    }
		}
		if (prep != null) {
		    try {
			prep.close();
		    } catch (SQLException ex) {
			logger.log(Level.WARNING, ex.getMessage());
			;
		    }
		}
	    }
	    return -1;
	}
    }

    @Override
    public int remove(int[] keys) {
	synchronized (writeLock) {
	    PreparedStatement prep = null;
	    try {
		if (connection == null) {
		    return -1;
		}
		String sql = "delete from " + this.tableName + " where "
			+ handle.getRemoveSql(keys);
		prep = connection.prepareStatement(sql);
		int result = prep.executeUpdate();

		return result;
	    } catch (SQLException ex) {
		logger.log(Level.WARNING, ex.getMessage());
		;
	    } finally {
		if (prep != null) {
		    try {
			prep.close();
		    } catch (SQLException ex) {
			logger.log(Level.WARNING, ex.getMessage());
			;
		    }
		}
	    }
	    return -1;
	}
    }

    @Override
    public int removeAll() {
	synchronized (writeLock) {
	    PreparedStatement prep = null;
	    try {
		if (connection == null) {
		    return -1;
		}
		String sql = "delete from " + this.tableName;
		prep = connection.prepareStatement(sql);
		int result = prep.executeUpdate();

		return result;
	    } catch (SQLException ex) {
		logger.log(Level.WARNING, ex.getMessage());
		;
	    } finally {
		if (prep != null) {
		    try {
			prep.close();
		    } catch (SQLException ex) {
			logger.log(Level.WARNING, ex.getMessage());
			;
		    }
		}
	    }
	    return -1;
	}
    }

    private String getSqlIds(int[] rows) {
	String str = "";
	for (int row : rows) {
	    str = str + row + ",";
	}
	if (str.endsWith(",")) {
	    str = str.substring(0, str.length() - 1);
	}
	return str;
    }

    @Override
    public int[] getKeys(SortKey sortKey, SearchCondition searchCondition) {
	PreparedStatement prep = null;
	ResultSet rs = null;
	int[] keys = null;
	// long t1 = System.currentTimeMillis();
	try {
	    if (connection == null) {
		return null;
	    }
	    String orderBy = null;
	    if (sortKey != null) {
		if (sortKey.getSortOrder() == SortOrder.ASCENDING) {
		    orderBy = "order by "
			    + handle.getColumnNameByTableShowIndex(sortKey
				    .getColumn()) + " asc";
		} else if (sortKey.getSortOrder() == SortOrder.DESCENDING) {
		    orderBy = "order by "
			    + handle.getColumnNameByTableShowIndex(sortKey
				    .getColumn()) + " desc";
		}
	    }
	    String sql = "select primaryKeyId from " + tableName;
	    String searchSql = null;
	    if (searchCondition != null) {
		if (!searchCondition.isAllColumnSearch()) {
		    String likeStr = "";
		    for (int index : searchCondition.getSearchIndexs()) {
			likeStr = likeStr
				+ handle.getColumnNameByTableShowIndex(index)
				+ " like '%"
				+ CacheUtil.sqliteEscape(searchCondition
					.getSearchTxt()) + "%'  escape '/' or ";
		    }

		    if (likeStr.length() > 0) {
			if (likeStr.endsWith(" or ")) {
			    searchSql = " where "
				    + likeStr.subSequence(0,
					    likeStr.length() - 3);
			}
		    }
		} else {
		    String likeStr = "";
		    for (Map.Entry<ColumnBean, String> entry : handle
			    .getSqlColumnMap().entrySet()) {
			if (entry.getKey().isShowInTable()) {
			    likeStr = likeStr
				    + entry.getValue()
				    + " like '%"
				    + CacheUtil.sqliteEscape(searchCondition
					    .getSearchTxt())
				    + "%'  escape '/' or ";
			}
		    }
		    if (likeStr.length() > 0) {
			if (likeStr.endsWith(" or ")) {
			    searchSql = " where "
				    + likeStr.subSequence(0,
					    likeStr.length() - 3);
			}
		    }
		}
	    }
	    if (searchSql != null) {
		sql = sql + searchSql;
	    }
	    if (orderBy != null) {
		sql = sql + " " + orderBy;
	    }

	    prep = connection.prepareStatement(sql);
	    rs = prep.executeQuery();
	    List<Integer> list = new ArrayList<Integer>();
	    // long t2 = System.currentTimeMillis();
	    while (rs.next()) {
		list.add(rs.getInt(1));
	    }
	    // long t3 = System.currentTimeMillis();

	    keys = CacheUtil.toIntArr(list);
	    list.clear();
	    // long t4 = System.currentTimeMillis();
	    // System.out.println("sql:" + sql + "耗时:" + (t2 - t1) + ","
	    // + (t3 - t2) + "," + (t4 - t3));
	} catch (SQLException ex) {
	    logger.log(Level.WARNING, ex.getMessage());
	    ;
	} finally {
	    if (rs != null) {
		try {
		    rs.close();
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		    ;
		}
	    }
	    if (prep != null) {
		try {
		    prep.close();
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		    ;
		}
	    }
	}
	return keys;
    }

    public Connection getConnection() {
	return connection;
    }

    public String getTableName() {
	return tableName;
    }

    public void setTableName(String tableName) {
	this.tableName = tableName;
    }

    @Override
    public int[] getKeys(int columnIndex, Object[] values) {
	PreparedStatement prep = null;
	ResultSet rs = null;
	try {
	    if (connection == null) {
		return null;
	    }
	    String sql = "select " + ColumnBean.DEFAULT_REFLECT_COLUMN_NAME
		    + " from " + this.tableName + " where "
		    + this.handle.getColumnNameByIndex(columnIndex) + " in("
		    + handle.createKeys(values) + ")";
	    prep = connection.prepareStatement(sql);
	    rs = prep.executeQuery();
	    List<Integer> list = new ArrayList<Integer>();
	    while (rs.next()) {
		list.add(rs.getInt(1));
	    }
	    return CacheUtil.toIntArr(list);
	} catch (SQLException ex) {
	    logger.log(Level.WARNING, ex.getMessage());
	    ;
	} finally {
	    if (rs != null) {
		try {
		    rs.close();
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		    ;
		}
	    }
	    if (prep != null) {
		try {
		    prep.close();
		} catch (SQLException ex) {
		    logger.log(Level.WARNING, ex.getMessage());
		    ;
		}
	    }
	}

	return null;
    }

    public void setConnection(Connection connection) {
	this.connection = connection;
    }

    @Override
    public ColumnBean[] getColumnBeans() {
	return this.columnBeans;
    }
}
