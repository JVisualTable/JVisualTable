package org.vt.util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vt.cache.orm.ColumnBean;

/**
 * db process handle
 * 
 * @author Zerg Law
 * 
 */
public class DBGeneratorHandle {
    Map<ColumnBean, String> sqlColumnMap = new LinkedHashMap<ColumnBean, String>();

    public DBGeneratorHandle(ColumnBean[] columnBeans) {
	for (ColumnBean bean : columnBeans) {
	    sqlColumnMap.put(bean, bean.getKeyName());
	}
    }

    public Object getDBValue(ColumnBean columnBean, Object value) {
	switch (columnBean.getType()) {
	case BOOLEAN:
	    if (value == null) {
		return false;
	    }
	    if (value instanceof Integer) {
		int i = Integer.parseInt(value.toString());
		return i == 0 ? false : true;
	    }
	    break;
	case SHORT:
	    if (value == null) {
		return 0;
	    }
	    if (value instanceof Integer) {
		return Integer.valueOf(value.toString()).shortValue();
	    }
	    break;
	case INT:
	    if (value == null) {
		return 0;
	    }
	    if (value instanceof Integer) {
		return Integer.valueOf(value.toString());
	    }
	    break;
	case LONG:
	    if (value == null) {
		return 0L;
	    }
	    if (value instanceof Long) {
		return Long.valueOf(value.toString());
	    }
	    break;
	case STRING:
	    if (value == null) {
		return null;
	    }
	    return value.toString();
	case DATETIME:
	    if (value == null) {
		return null;
	    }
	    if (value instanceof Long) {
		return new Date(Long.valueOf(value.toString()));
	    }
	    break;
	case DOUBLE:
	    if (value == null) {
		return 0.0D;
	    }
	    if (value instanceof Double) {
		return Double.valueOf(value.toString());
	    }
	    break;
	case OBJECT:

	    break;
	default:
	    break;
	}
	return value;
    }

    private String getColumnDesc(ColumnBean columnBean) {
	String str = null;
	if (ColumnBean.DEFAULT_REFLECT_COLUMN_NAME.equals(columnBean
		.getKeyName())) {
	    str = "Integer primary key autoincrement";
	} else {
	    switch (columnBean.getType()) {
	    case BOOLEAN:
		str = "int1";
		break;
	    case SHORT:
		str = "int2";
		break;
	    case INT:
		str = "int4";
		break;
	    case LONG:
		str = "int8";
		break;
	    case STRING:
		str = "text";
		break;
	    case DATETIME:
		str = "datetime";
		break;
	    case DOUBLE:
		str = "double";
		break;
	    case OBJECT:
		str = "blob";
		break;
	    default:
		break;
	    }

	}
	return str;
    }

    public String createTableSql(String tableName) {
	String str = "";
	for (Map.Entry<ColumnBean, String> entry : sqlColumnMap.entrySet()) {
	    str = str + entry.getValue() + " " + getColumnDesc(entry.getKey())
		    + ",";
	}
	if (str.endsWith(",")) {
	    str = str.substring(0, str.length() - 1);
	}
	return "create table " + tableName + " (" + str + ")";
    }

    public String getColumnNameByTableShowIndex(int tableColumnIndex) {
	for (Map.Entry<ColumnBean, String> entry : sqlColumnMap.entrySet()) {
	    if (entry.getKey().getShowInTableIndex() == tableColumnIndex) {
		return entry.getValue();
	    }
	}
	throw new RuntimeException("can not find db column index:"
		+ tableColumnIndex);
    }

    public String getColumnNameByIndex(int tableColumnIndex) {
	for (Map.Entry<ColumnBean, String> entry : sqlColumnMap.entrySet()) {
	    if (entry.getKey().getIndex() == tableColumnIndex) {
		return entry.getValue();
	    }
	}
	throw new RuntimeException("can not find db column index:"
		+ tableColumnIndex);
    }

    public String getParamCreateInputSql() {
	String str = "";
	for (Map.Entry<ColumnBean, String> entry : sqlColumnMap.entrySet()) {
	    str = str + "?" + ",";
	}
	if (str.endsWith(",")) {
	    str = str.substring(0, str.length() - 1);
	}
	return str;
    }

    public String getParamUpdateInputSql() {
	String str = "";
	for (Map.Entry<ColumnBean, String> entry : sqlColumnMap.entrySet()) {
	    if (ColumnBean.DEFAULT_REFLECT_COLUMN_NAME.equals(entry.getValue())) {
		continue;
	    }
	    str = str + entry.getValue() + "=?" + ",";
	}
	if (str.endsWith(",")) {
	    str = str.substring(0, str.length() - 1);
	}
	return str;
    }

    public String getRemoveSql(int[] primaryKeyIds) {
	List<Integer[]> list = CacheUtil.getRangeArr(primaryKeyIds);
	String str = "";
	List<Integer> sIds = new ArrayList<Integer>();
	List<Integer[]> rIds = new ArrayList<Integer[]>();
	for (Integer[] arr : list) {
	    if (arr[0].intValue() == arr[1].intValue()) {
		sIds.add(arr[0].intValue());
	    } else {
		rIds.add(arr);
	    }
	}
	if (!sIds.isEmpty()) {
	    String subSql = "";
	    for (int id : sIds) {
		subSql = subSql + id + ",";
	    }
	    if (subSql.endsWith(",")) {
		subSql = subSql.substring(0, subSql.length() - 1);
	    }
	    str = ColumnBean.DEFAULT_REFLECT_COLUMN_NAME + " in(" + subSql
		    + ") ";
	}
	if (!rIds.isEmpty()) {
	    String subSql = "";
	    for (Integer[] ids : rIds) {
		String s = "(" + ColumnBean.DEFAULT_REFLECT_COLUMN_NAME + ">="
			+ ids[0] + " and "
			+ ColumnBean.DEFAULT_REFLECT_COLUMN_NAME + "<="
			+ ids[1] + ")";
		subSql = subSql + s + " or ";
	    }
	    if (subSql.endsWith("or ")) {
		subSql = subSql.substring(0, subSql.length() - 3);
	    }
	    if (!str.equals("")) {
		str = str + " or " + subSql;
	    } else {
		str = subSql;
	    }
	}
	return str;
    }

    public Map<ColumnBean, String> getSqlColumnMap() {
	return sqlColumnMap;
    }

    public static void initSqlColumnKeys(ColumnBean[] columnBeans) {
	Map<ColumnBean, String> sqlColumnMap = new LinkedHashMap<ColumnBean, String>();
	int eIndex = 0;
	int cIndex = 0;
	int index = 0;
	for (ColumnBean bean : columnBeans) {
	    bean.setIndex(index);
	    index++;
	    String keyName;
	    if (bean.isShowInTable()) {
		keyName = "C" + cIndex;
		bean.setShowInTableIndex(cIndex);
		cIndex++;
	    } else {
		keyName = "E" + eIndex;
		eIndex++;
	    }
	    if (bean.getKeyName() != null
		    && !bean.getKeyName().trim().equals("")) {
		keyName = bean.getKeyName();
	    }
	    if (sqlColumnMap.containsValue(keyName)) {
		throw new RuntimeException("column name:" + keyName
			+ " exist，please change it.");
	    }
	    sqlColumnMap.put(bean, keyName);
	    bean.setKeyName(keyName);
	}
    }

    public static void fireChangeShowTableIndexs(ColumnBean[] columnBeans) {
	int index = 0;
	for (int i = 0; i < columnBeans.length; i++) {
	    ColumnBean columnBean = columnBeans[i];
	    if (columnBean.isShowInTable() && !columnBean.isHidden()) {
		columnBean.setShowInTableIndex(index);
		index++;
	    } else {
		columnBean.setShowInTableIndex(-1);
	    }
	}
    }

    public static ColumnBean[] checkAndUpdate(ColumnBean[] columnBeans) {
	List<ColumnBean> retBeans = new ArrayList<ColumnBean>();
	for (ColumnBean bean : columnBeans) {
	    // 其它列名称不允许为默认主键id
	    if (bean.getKeyName() != null
		    && bean.getKeyName().equalsIgnoreCase(
			    ColumnBean.DEFAULT_REFLECT_COLUMN_NAME)) {
		throw new RuntimeException("Column key name can't be '"
			+ ColumnBean.DEFAULT_REFLECT_COLUMN_NAME
			+ ", please change it.");
	    }
	}
	ColumnBean idColumnBean = new ColumnBean(null,
		ColumnBean.DEFAULT_REFLECT_COLUMN_NAME, false);
	retBeans.add(idColumnBean);
	retBeans.addAll(Arrays.asList(columnBeans));
	ColumnBean[] beans = retBeans.toArray(new ColumnBean[retBeans.size()]);
	initSqlColumnKeys(beans);
	DBGeneratorHandle.fireChangeShowTableIndexs(beans);
	return beans;
    }

    private Object toString(Object obj) {
	if (obj != null) {
	    if (obj instanceof Integer) {
		return ((Integer) obj).intValue();
	    }
	    if (obj instanceof Double) {
		return ((Double) obj).doubleValue();
	    }
	    if (obj instanceof Short) {
		return ((Short) obj).shortValue();
	    }
	    if (obj instanceof Long) {
		return ((Long) obj).longValue();
	    }
	    if (obj instanceof Boolean) {
		return ((Boolean) obj).booleanValue() ? 1 : 0;
	    }
	    if (obj instanceof java.util.Date) {
		return ((Date) obj).getTime();
	    }

	    return obj.toString();
	} else {
	    return "";
	}
    }

    public String createKeys(Object[] keys) {
	String str = "";
	for (int i = 0; i < keys.length; i++) {
	    str = str + toString(keys[i]) + ",";
	}
	if (str.endsWith(",")) {
	    str = str.substring(0, str.length() - 1);
	}
	return str;
    }

}
