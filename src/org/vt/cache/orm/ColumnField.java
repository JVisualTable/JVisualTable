package org.vt.cache.orm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnField {

    /**
     * The Column is id Type, id column must define as: int、long or String, Bean
     * must hava one column define for id.
     * 
     * @return
     */
    public boolean isId() default false;

    /**
     * Swing Table Column Name
     * 
     * @return
     */
    public String tableColumnName() default "";

    /**
     * Is Show In Swing Table?
     * 
     * @return boolean
     */
    public boolean isShowInTable() default true;

    /**
     * Is Hidden In Swing Table?
     * 
     * @return
     */
    public boolean isHidden() default false;

    /**
     * Database Table columnName,it can't repeat for one bean
     * 
     * @return keyName
     */
    public String dbColumnName() default "";

}
