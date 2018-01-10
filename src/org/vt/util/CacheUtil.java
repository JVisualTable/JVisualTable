package org.vt.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vt.cache.CacheManager;
import org.vt.cache.Element;
import org.vt.cache.orm.ColumnBean;
import org.vt.cache.orm.ColumnField;
import org.vt.cache.orm.JdbcType;

import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;

/**
 * cache util tools
 * 
 * @author Zerg Law
 * 
 */
public class CacheUtil {
    private static Logger logger = Logger.getLogger(CacheUtil.class.getName());
    private static String KEY = "CACHE";
    public static String CACHE_HOME = System.getProperty("user.home")
	    + File.separator + "cache";
    private static Map<String, List<Method>> methodGetMap = new HashMap();
    private static Map<String, List<Method>> methodSetMap = new HashMap();

    /**
     * reversal sort
     * 
     * @param ids
     * @return
     */
    public static int[] reversal(int[] ids) {
	int[] arr = new int[ids.length];
	for (int i = ids.length - 1; i >= 0; i--) {
	    arr[ids.length - 1 - i] = ids[i];
	}
	return arr;
    }

    /**
     * check disk is full
     * 
     * @param file
     * @param size
     * @return
     */
    public static boolean checkDiskSpace(File file, long recordSize) {
	CacheManager.getInstance();
	makeDir(file.getAbsolutePath());
	long space = file.getUsableSpace() / (1024 * 1024);// 单位MB
	long sum = recordSize / 100000L;
	sum = recordSize % 100000L == 0 ? sum : sum + 1;
	if (space > sum * 20) {
	    return true;
	}
	return false;
    }

    public static void makeDir(String dir) {
	File cacheFile = new File(dir);
	if (!cacheFile.exists()) {
	    cacheFile.mkdirs();
	}
    }

    public static String getUserDir() {
	return CACHE_HOME;
    }

    /**
     * load get/set method
     * 
     * @param cls
     * @param isGet
     * @return
     */
    public static List<Method> loadMethods(Class cls, boolean isGet) {
	if (isGet) {
	    List<Method> methods = methodGetMap.get(cls.getCanonicalName());
	    if (methods != null) {
		return methods;
	    }
	} else {
	    if (isGet) {
		List<Method> methods = methodSetMap.get(cls.getCanonicalName());
		if (methods != null) {
		    return methods;
		}
	    }
	}
	List<Method> list = new ArrayList<Method>();
	Field[] fields = cls.getDeclaredFields();
	for (Field f : fields) {
	    if (f.isAnnotationPresent(ColumnField.class)) {
		PropertyDescriptor descriptor;
		try {
		    descriptor = new PropertyDescriptor(f.getName(), cls);
		    if (isGet) {
			list.add(descriptor.getReadMethod());
		    } else {
			list.add(descriptor.getWriteMethod());
		    }
		} catch (IntrospectionException e) {
		    throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
		    throw new RuntimeException(e);
		}
	    }
	}
	if (isGet) {
	    methodGetMap.put(cls.getCanonicalName(), list);
	} else {
	    methodSetMap.put(cls.getCanonicalName(), list);
	}
	return list;
    }

    /**
     * Get Column index for show in table
     * 
     * @param columnBeans
     * @param index
     * @return
     */
    public static int getColumnIndexByShowInTableIndex(
	    ColumnBean[] columnBeans, int columnIndex) {
	for (ColumnBean bean : columnBeans) {
	    if (bean.isShowInTable()
		    && bean.getShowInTableIndex() == columnIndex) {
		return bean.getIndex();
	    }
	}
	return -1;
    }

    public static List<JdbcType> loadJdbcTypes(Class cls)
	    throws InstantiationException, IllegalAccessException {
	List<JdbcType> list = new ArrayList<JdbcType>();
	Field[] fields = cls.getDeclaredFields();
	for (Field f : fields) {
	    if (f.isAnnotationPresent(ColumnField.class)) {
		list.add(getJdbcType(f));
	    }
	}
	return list;
    }

    /**
     * to beans
     * 
     * @param cls
     * @param map
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> Map<Integer, T> toBeans(Class<T> cls,
	    Map<Integer, Vector> map) throws InstantiationException,
	    IllegalAccessException, ClassNotFoundException, IOException {
	List<Method> valueSetMethods = loadMethods(cls, false);
	List<JdbcType> jdbcTypes = loadJdbcTypes(cls);
	Map<Integer, T> retMap = new HashMap<Integer, T>();
	for (Map.Entry<Integer, Vector> entry : map.entrySet()) {
	    Vector vector = entry.getValue();
	    Object obj = null;
	    try {
		obj = cls.newInstance();
		for (int i = 0; i < valueSetMethods.size(); i++) {
		    JdbcType type = jdbcTypes.get(i);
		    Object value = vector.get(i);
		    if (type == JdbcType.OBJECT) {
			if (value != null && value instanceof byte[]) {
			    value = ObjectByteArrayOutputStream
				    .getObject((byte[]) value);
			}
		    } else if (type == JdbcType.DATETIME) {
			if (value != null && !value.toString().equals("")) {
			    // System.out.println(vector);
			    SimpleDateFormat format = new SimpleDateFormat(
				    "yyyy-MM-dd HH:mm:ss");
			    value = format.parse(value.toString());
			} else {
			    System.out.println(vector);
			}
		    }
		    valueSetMethods.get(i).invoke(obj, value);
		}
	    } catch (InstantiationException e) {
		logger.log(Level.WARNING, e.getMessage());
		;
	    } catch (IllegalAccessException e) {
		logger.log(Level.WARNING, e.getMessage());
		;
	    } catch (IllegalArgumentException e) {
		logger.log(Level.WARNING, e.getMessage());
		;
	    } catch (InvocationTargetException e) {
		logger.log(Level.WARNING, e.getMessage());
		;
	    } catch (ParseException e) {
		// TODO 自动生成的 catch 块
		logger.log(Level.WARNING, e.getMessage());
		;
	    }
	    retMap.put(entry.getKey().intValue(), (T) obj);
	}
	return retMap;
    }

    public static <T> T toBean(Class<T> cls, Vector vector) {
	List<Method> valueSetMethods = loadMethods(cls, false);
	Object obj = null;
	try {
	    obj = cls.newInstance();
	    for (int i = 0; i < valueSetMethods.size(); i++) {
		valueSetMethods.get(i).invoke(obj, vector.get(i));
	    }
	} catch (InstantiationException e) {
	    throw new RuntimeException(e);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	} catch (IllegalArgumentException e) {
	    throw new RuntimeException(e);
	} catch (InvocationTargetException e) {
	    throw new RuntimeException(e);
	}
	return (T) obj;
    }

    public static Vector toVector(Object bean) {
	List<Method> valueGetMethods = loadMethods(bean.getClass(), true);
	Vector vector = new Vector();
	try {
	    List<JdbcType> jdbcTypes = loadJdbcTypes(bean.getClass());
	    for (int i = 0; i < valueGetMethods.size(); i++) {
		Method m = valueGetMethods.get(i);
		JdbcType type = jdbcTypes.get(i);

		Object value = m.invoke(bean);
		if (type == JdbcType.OBJECT) {
		    if (value != null) {
			try {
			    value = ObjectByteArrayOutputStream
				    .serialize(value).getBytes();
			} catch (IOException e) {
			    throw new RuntimeException(e);
			}
		    }
		} else if (type == JdbcType.DATETIME) {
		    if (value != null && value instanceof Date) {
			SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
			value = format.format((Date) value);
		    }
		}
		vector.add(value);
	    }
	} catch (IllegalArgumentException e) {
	    throw new RuntimeException(e);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	} catch (InvocationTargetException e) {
	    throw new RuntimeException(e);
	} catch (InstantiationException e) {
	    throw new RuntimeException(e);
	}
	return vector;
    }

    /***
     * bean to vector
     * 
     * @param elements
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> Vector<Vector> toDBVectors(List<Element<T>> elements)
	    throws InstantiationException, IllegalAccessException,
	    ClassNotFoundException, IOException {
	if (elements.isEmpty()) {
	    return new Vector<Vector>();
	}
	List<Method> valueGetMethods = loadMethods(elements.get(0).getData()
		.getClass(), true);
	List<JdbcType> jdbcTypes = loadJdbcTypes(elements.get(0).getData()
		.getClass());
	Vector<Vector> vectors = new Vector<Vector>();
	for (Element element : elements) {
	    Vector vector = new Vector();
	    for (int i = 0; i < valueGetMethods.size(); i++) {
		Method m = valueGetMethods.get(i);
		JdbcType type = jdbcTypes.get(i);
		try {
		    Object value = m.invoke(element.getData());
		    if (type == JdbcType.OBJECT) {
			if (value != null) {
			    value = ObjectByteArrayOutputStream
				    .serialize(value).getBytes();
			}
		    }
		    vector.add(value);
		} catch (IllegalArgumentException e) {
		    logger.log(Level.WARNING, e.getMessage());
		    ;
		} catch (IllegalAccessException e) {
		    logger.log(Level.WARNING, e.getMessage());
		    ;
		} catch (InvocationTargetException e) {
		    logger.log(Level.WARNING, e.getMessage());
		    ;
		}
	    }
	    vectors.add(vector);
	}
	return vectors;
    }

    /***
     * bean to vector for showing in table
     * 
     * @param elements
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> Vector<Vector> toShowVectors(List<Element<T>> elements)
	    throws InstantiationException, IllegalAccessException,
	    ClassNotFoundException, IOException {
	if (elements.isEmpty()) {
	    return new Vector<Vector>();
	}
	List<Method> valueGetMethods = loadMethods(elements.get(0).getData()
		.getClass(), true);
	List<JdbcType> jdbcTypes = loadJdbcTypes(elements.get(0).getData()
		.getClass());
	Vector<Vector> vectors = new Vector<Vector>();
	for (Element element : elements) {
	    Vector vector = new Vector();
	    for (int i = 0; i < valueGetMethods.size(); i++) {
		Method m = valueGetMethods.get(i);
		JdbcType type = jdbcTypes.get(i);
		try {
		    Object value = element.getData();
		    if (type == JdbcType.OBJECT) {
			if (value != null && value instanceof byte[]) {
			    value = ObjectByteArrayOutputStream
				    .getObject((byte[]) value);
			}
		    }
		    if (value != null) {
			vector.add(m.invoke(value));
		    }
		} catch (IllegalArgumentException e) {
		    logger.log(Level.WARNING, e.getMessage());
		    ;
		} catch (IllegalAccessException e) {
		    logger.log(Level.WARNING, e.getMessage());
		    ;
		} catch (InvocationTargetException e) {
		    logger.log(Level.WARNING, e.getMessage());
		    ;
		}
	    }
	    vectors.add(vector);
	}
	return vectors;
    }

    /**
     *Get Id by annotation
     * 
     * @param cls
     * @param isGet
     * @return
     */
    public static Method getIdGetMethod(Class cls, boolean isGet) {
	Field[] fields = cls.getDeclaredFields();
	for (Field f : fields) {
	    if (f.isAnnotationPresent(ColumnField.class)) {
		ColumnField columnField = f.getAnnotation(ColumnField.class);
		if (columnField.isId()) {
		    PropertyDescriptor descriptor;
		    try {
			descriptor = new PropertyDescriptor(f.getName(), cls);
			if (isGet) {
			    return descriptor.getReadMethod();
			} else {
			    return descriptor.getWriteMethod();
			}
		    } catch (IntrospectionException e) {
			throw new RuntimeException(e);
		    } catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		    }
		}
	    }
	}

	return null;
    }

    /**
     * Get Column Beans
     * 
     * @param cls
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static ColumnBean[] createColumnBeans(Class cls)
	    throws InstantiationException, IllegalAccessException {
	Field[] fields = cls.getDeclaredFields();
	List<ColumnBean> columnBeans = new ArrayList<ColumnBean>();
	for (Field f : fields) {
	    if (f.isAnnotationPresent(ColumnField.class)) {
		ColumnField columnField = f.getAnnotation(ColumnField.class);
		if (columnField.isShowInTable()) {
		    if (columnField.tableColumnName().equals("")) {
			throw new RuntimeException(
				"Column Name can't be null, when it's show in table.");
		    }
		}
		ColumnBean columnBean = new ColumnBean(columnField
			.tableColumnName(), columnField.dbColumnName(),
			columnField.isShowInTable());
		columnBean.setId(columnField.isId());
		columnBean.setType(getJdbcType(f));
		if (columnBean.isId()) {
		    if (columnBean.getType() != JdbcType.INT
			    && columnBean.getType() != JdbcType.LONG
			    && columnBean.getType() != JdbcType.STRING) {
			throw new RuntimeException(
				"ColumnField is defined as id, but field not (int、long、string)，please change it.");
		    }
		}
		columnBean.setHidden(columnField.isHidden());
		columnBeans.add(columnBean);
	    }
	}
	if (columnBeans.isEmpty()) {
	    throw new RuntimeException(
		    "ColumnField Annotation not define in class:" + cls);
	}
	int idCount = 0;
	for (ColumnBean columnBean : columnBeans) {
	    if (columnBean.isId()) {
		idCount++;
	    }
	}
	if (idCount == 0) {
	    throw new RuntimeException(
		    "ColumnField isId Annotation must be defined at JavaBean field: "
			    + cls);
	} else if (idCount > 1) {
	    throw new RuntimeException(
		    "ColumnField isId Annotation has more than one，please keep only one 'isId' Annotation: "
			    + cls);
	}
	ColumnBean[] retBeans = columnBeans.toArray(new ColumnBean[columnBeans
		.size()]);

	return retBeans;
    }

    public static JdbcType getJdbcType(Field field)
	    throws InstantiationException, IllegalAccessException {
	return getJdbcType(field.getType());
    }

    public static JdbcType getJdbcType(Class cls)
	    throws InstantiationException, IllegalAccessException {
	if (cls.isPrimitive()) {
	    if (cls.getName().equals("int")) {
		return JdbcType.INT;
	    }
	    if (cls.getName().equals("boolean")) {
		return JdbcType.BOOLEAN;
	    }
	    if (cls.getName().equals("char")) {
		return JdbcType.SHORT;
	    }
	    if (cls.getName().equals("short")) {
		return JdbcType.SHORT;
	    }
	    if (cls.getName().equals("long")) {
		return JdbcType.LONG;
	    }
	    if (cls.getName().equals("float")) {
		return JdbcType.DOUBLE;
	    }
	    if (cls.getName().equals("double")) {
		return JdbcType.DOUBLE;
	    }
	}
	if (cls.isArray()) {
	    return JdbcType.OBJECT;
	}
	if (cls.isEnum()) {
	    return JdbcType.OBJECT;
	}

	if (cls.isInstance(1)) {
	    return JdbcType.INT;
	}
	if (cls.isInstance(true)) {
	    return JdbcType.BOOLEAN;
	}
	if (cls.isInstance((short) 1)) {
	    return JdbcType.SHORT;
	}
	if (cls.isInstance(1L)) {
	    return JdbcType.SHORT;
	}
	if (cls.isInstance(1D)) {
	    return JdbcType.DOUBLE;
	}
	if (cls.isInstance("")) {
	    return JdbcType.STRING;
	}
	if (cls.isInstance(new Date())) {
	    return JdbcType.DATETIME;
	}

	return JdbcType.OBJECT;
    }

    /**
     *Get Java Process Id
     * 
     * @return
     */
    public static String getPid() {
	String wholeName = ManagementFactory.getRuntimeMXBean().getName();
	if (null != wholeName && wholeName.indexOf("@") != -1) {
	    return wholeName.substring(0, wholeName.indexOf("@"));
	}
	return "";
    }

    /**
     * Get All Java Process Id
     * 
     * @return
     * @throws MonitorException
     * @throws URISyntaxException
     */
    public static List<Integer> getAllJavaPid() throws MonitorException,
	    URISyntaxException {
	List<Integer> pids = new ArrayList<Integer>();
	MonitoredHost host = MonitoredHost.getMonitoredHost("localhost");
	for (Object process : host.activeVms()) {
	    if (process != null && process instanceof Integer) {
		pids.add(((Integer) process).intValue());
	    }
	}
	return pids;
    }

    /**
     * Integer List to int arr
     * 
     * @param list
     * @return
     */
    public static int[] toIntArr(List<Integer> list) {
	int[] arr = new int[list.size()];
	for (int i = 0; i < list.size(); i++) {
	    arr[i] = list.get(i).intValue();
	}
	return arr;
    }

    /**
     * Concat array
     * 
     * @param a
     * @param b
     * @return
     */
    public static int[] concat(int[] a, int[] b) {
	int[] c = new int[a.length + b.length];
	System.arraycopy(a, 0, c, 0, a.length);
	System.arraycopy(b, 0, c, a.length, b.length);
	return c;
    }

    /**
     * disconcat arr
     * 
     * @param a
     * @param b
     * @return
     */
    public static int[] disconcat(int[] arr1, int[] arr2) {
	List<Integer> ids = new ArrayList<Integer>();
	for (int i : arr1) {
	    boolean exist = false;
	    for (int j : arr2) {
		if (i == j) {
		    exist = true;
		    break;
		}
	    }
	    if (!exist) {
		ids.add(i);
	    }
	}
	return toIntArr(ids);
    }

    /**
     * Get Range Area，In: 1,2,3,5,6,8,10，Out:1-3,5-6,8-8,10-10
     * 
     * @param indexs
     * @return
     */
    public static List<Integer[]> getRangeArr(int[] indexs) {
	return getRangeArr(toList(indexs));
    }

    /**
     * int list to Integer List
     * 
     * @param indexs
     * @return
     */
    public static List<Integer> toList(int[] indexs) {
	List<Integer> list = new ArrayList<Integer>();
	for (int i : indexs) {
	    list.add(i);
	}
	return list;
    }

    /**
     * Get Range Area，In: 1,2,3,5,6,8,10，Out:1-3,5-6,8-8,10-10
     * 
     * @param list
     * @return
     */
    public static List<Integer[]> getRangeArr(List<Integer> list) {
	List<Integer[]> retlist = new ArrayList<Integer[]>();
	Collections.sort(list);
	List<List<Integer>> cList = new ArrayList<List<Integer>>();
	for (int i = 0; i < list.size(); i++) {
	    int value = list.get(i);
	    if (cList.isEmpty()) {
		cList.add(new ArrayList<Integer>(Arrays.asList(value)));
	    } else {
		List<Integer> lastList = cList.get(cList.size() - 1);
		int lastListValue = lastList.get(lastList.size() - 1);
		if (value - lastListValue <= 1) {
		    lastList.add(value);
		} else {
		    cList.add(new ArrayList<Integer>());
		    cList.get(cList.size() - 1).add(value);
		}
	    }
	}
	for (List<Integer> uList : cList) {
	    if (uList.size() == 1) {
		retlist.add(new Integer[] { uList.get(0), uList.get(0) });
	    } else {
		retlist.add(new Integer[] { uList.get(0),
			uList.get(uList.size() - 1) });
	    }
	}
	return retlist;
    }

    /**
     * sqlite like replace
     * 
     * @param keyWord
     * @return
     */
    public static String sqliteEscape(String keyWord) {
	keyWord = keyWord.replace("/", "//");
	keyWord = keyWord.replace("'", "''");
	keyWord = keyWord.replace("[", "/[");
	keyWord = keyWord.replace("]", "/]");
	keyWord = keyWord.replace("%", "/%");
	keyWord = keyWord.replace("&", "/&");
	keyWord = keyWord.replace("_", "/_");
	keyWord = keyWord.replace("(", "/(");
	keyWord = keyWord.replace(")", "/)");
	return keyWord;
    }
}
