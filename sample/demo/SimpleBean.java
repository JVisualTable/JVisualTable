package demo;

import java.util.Date;

import org.vt.cache.orm.ColumnField;

/**
 * <p>
 * 1.Bean must define ColumnField
 * </p>
 * <p>
 * 2.Bean must be POJO Bean, must have a empty construct method
 * </p>
 * <p>
 * 3.Bean Must has a Id ColumnField
 * </p>
 * author Zerg Law
 */
public class SimpleBean {
    @ColumnField(tableColumnName = "C1-ID", isId = true)
    private int id;
    @ColumnField(tableColumnName = "C2-boolean", isHidden = false)
    private String booleanValue;
    @ColumnField(tableColumnName = "C3-short")
    private short shortValue;
    @ColumnField(tableColumnName = "C4-double")
    private double doubleValue;
    @ColumnField(tableColumnName = "C5-long")
    private long longValue;
    @ColumnField(tableColumnName = "C6-string")
    private String stringValue;
    @ColumnField(tableColumnName = "C7-datetime")
    private Date dateValue;
    @ColumnField(tableColumnName = "C8-other")
    private UserObject userObject;

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public String getBooleanValue() {
	return booleanValue;
    }

    public void setBooleanValue(String booleanValue) {
	this.booleanValue = booleanValue;
    }

    public short getShortValue() {
	return shortValue;
    }

    public void setShortValue(short shortValue) {
	this.shortValue = shortValue;
    }

    public double getDoubleValue() {
	return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
	this.doubleValue = doubleValue;
    }

    public long getLongValue() {
	return longValue;
    }

    public void setLongValue(long longValue) {
	this.longValue = longValue;
    }

    public String getStringValue() {
	return stringValue;
    }

    public void setStringValue(String stringValue) {
	this.stringValue = stringValue;
    }

    public Date getDateValue() {
	return dateValue;
    }

    public void setDateValue(Date dateValue) {
	this.dateValue = dateValue;
    }

    public UserObject getUserObject() {
	return userObject;
    }

    public void setUserObject(UserObject userObject) {
	this.userObject = userObject;
    }

}
