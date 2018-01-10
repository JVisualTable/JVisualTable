package org.vt.cache.orm;

/**
 * Define For Table Column Define.
 * 
 * @author Zerg Law
 * 
 */
public class ColumnBean {

    /**
     * Database Table Primary Key
     */
    public static String DEFAULT_REFLECT_COLUMN_NAME = "primaryKeyId";
    /**
     * Real Table Index For Table Row
     */
    private int index;
    /**
     * Database Table's Id column flag, User Must Define it.
     */
    private boolean isId;
    /**
     * Show in Swing Table 's index
     */
    private int showInTableIndex = -1;
    /**
     * Column's Type, For jdbc Serializable oor UnSerializable
     */
    private JdbcType type = JdbcType.STRING;;
    /**
     * The Column Name For Swing Table Header
     */
    private String tableColumnName = null;
    /**
     * The Database Table ColumnName
     */
    private String dbColumnName = null;
    /**
     * Define Column For Show In Swing Table
     */
    private boolean isShowInTable = true;
    /**
     * Show or hidden For Swing Table Column
     */
    private boolean isHidden = false;
    /**
     * Default Column For Search
     */
    private boolean defaultSearch;

    public ColumnBean() {
	this(null, null, false);
    }

    public ColumnBean(String tableColumnName, String dbColumnName,
	    boolean isShowInTable) {
	super();
	this.tableColumnName = tableColumnName;
	this.dbColumnName = dbColumnName;
	this.isShowInTable = isShowInTable;
    }

    public JdbcType getType() {
	return type;
    }

    public void setType(JdbcType type) {
	this.type = type;
    }

    public boolean isShowInTable() {
	return isShowInTable;
    }

    public void setShowInTable(boolean isShowInTable) {
	this.isShowInTable = isShowInTable;
    }

    public String getColumnName() {
	return tableColumnName;
    }

    public void setColumnName(String columnName) {
	this.tableColumnName = columnName;
    }

    public String getKeyName() {
	return dbColumnName;
    }

    public void setKeyName(String keyName) {
	this.dbColumnName = keyName;
    }

    public int getShowInTableIndex() {
	return showInTableIndex;
    }

    public void setShowInTableIndex(int showInTableIndex) {
	this.showInTableIndex = showInTableIndex;
    }

    public int getIndex() {
	return index;
    }

    public void setIndex(int index) {
	this.index = index;
    }

    public boolean isId() {
	return isId;
    }

    public void setId(boolean isId) {
	this.isId = isId;
    }

    public String getTableColumnName() {
	return tableColumnName;
    }

    public void setTableColumnName(String tableColumnName) {
	this.tableColumnName = tableColumnName;
    }

    public String getDbColumnName() {
	return dbColumnName;
    }

    public void setDbColumnName(String dbColumnName) {
	this.dbColumnName = dbColumnName;
    }

    public boolean isHidden() {
	return isHidden;
    }

    public void setHidden(boolean isHidden) {
	this.isHidden = isHidden;
    }

    public boolean isDefaultSearch() {
	return defaultSearch;
    }

    public void setDefaultSearch(boolean defaultSearch) {
	this.defaultSearch = defaultSearch;
    }
}
