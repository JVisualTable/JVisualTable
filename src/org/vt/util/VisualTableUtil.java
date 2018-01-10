package org.vt.util;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.vt.JVisualTable;
import org.vt.beans.BatchBean;
import org.vt.beans.OperaType;
import org.vt.component.VisualTableColumnChooserDialog;
import org.vt.component.VisualTableModel;
import org.vt.locale.ResourceManager;
import org.vt.locale.VTResourceBundle;

/**
 * VisualTable Tools
 * 
 * @author Zerg Law
 * 
 */
public class VisualTableUtil {

    
    private static ResourceBundle bundle = ResourceManager
	    .getBundle(VTResourceBundle.class);

    /**
     * hide or show columns
     * 
     * @param visualTable
     *            visualTable
     */
    public static void registerTableHeadPopupMenu(final JVisualTable visualTable) {
	final JPopupMenu tblSettingPopupMenu = new JPopupMenu();
	AbstractAction tblSettingAction = new AbstractAction() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		VisualTableColumnChooserDialog dialog = new VisualTableColumnChooserDialog(
			visualTable);
		dialog.setVisible(true);
	    }
	};
	JMenuItem menuitemTableSet = new JMenuItem(bundle
		.getString("column_select"));
	menuitemTableSet.addActionListener(tblSettingAction);
	tblSettingPopupMenu.add(menuitemTableSet);
	visualTable.getTableHeader().addMouseListener(new MouseAdapter() {

	    @Override
	    public void mousePressed(final MouseEvent e) {
		SwingUtilities.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			    tblSettingPopupMenu.show(e.getComponent(),
				    e.getX(), e.getY());
			}
		    }
		});
	    }

	    @Override
	    public void mouseReleased(final MouseEvent e) {
		SwingUtilities.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			    tblSettingPopupMenu.show(e.getComponent(),
				    e.getX(), e.getY());
			}
		    }
		});
	    }
	});
    }

    public static int getScreenMaxRow(JTable table) {
	Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
	int height = (int) screensize.getHeight();
	int rowHeight = table.getRowHeight();
	if (height > 0 && rowHeight > 0) {
	    return height / rowHeight + 1;
	}
	return height;
    }

    public static int[] getViewportRows(JTable table, int scrollBarValue) {
	JScrollBar scrollBar = getVerticalScrollBar(table);
	if (scrollBar != null && !scrollBar.isShowing()) {
	    int screenMaxRow = getScreenMaxRow(table);
	    int rowCount = table.getRowCount();
	    rowCount = rowCount > screenMaxRow ? screenMaxRow : rowCount;
	    return new int[] { 0, rowCount };
	}
	int[] result = new int[2];
	int rowHeight = table.getRowHeight();

	int startRow = scrollBarValue % rowHeight == 0 ? scrollBarValue
		/ rowHeight : scrollBarValue / rowHeight + 1;

	if (scrollBarValue == 0) {
	    startRow = 1;
	}
	double endValue = scrollBarValue + table.getVisibleRect().getHeight();
	int endRow;
	double rawEndRow = endValue / rowHeight;
	if (rawEndRow / (int) rawEndRow == 1.00) {
	    endRow = (int) rawEndRow;
	} else {
	    endRow = (int) rawEndRow + 1;
	}
	result[0] = startRow;
	result[1] = endRow;
	return result;
    }

    public static JScrollBar getVerticalScrollBar(JTable table) {
	Object parent = table.getParent();
	if (parent != null && parent instanceof JViewport) {
	    JViewport viewport = (JViewport) parent;
	    Object pParent = viewport.getParent();
	    if (pParent != null && pParent instanceof JScrollPane) {
		return ((JScrollPane) pParent).getVerticalScrollBar();
	    }
	}
	return null;
    }

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

    public static List<Integer> toList(int[] indexs) {
	List<Integer> list = new ArrayList<Integer>();
	for (int i : indexs) {
	    list.add(i);
	}
	return list;
    }

    private static void actionSelect(JTable table, JLabel label) {
	int selectRow = table.getSelectedRow();
	int selectRowCount = table.getSelectedRows().length;
	int rowCount = table.getModel().getRowCount();
	if (table.getRowSorter() != null) {
	    rowCount = table.getRowSorter().getViewRowCount();
	}
	if ((selectRow >= 0) && (selectRowCount >= 0) && selectRow <= rowCount
		&& rowCount != 0) {
	    label.setText(bundle.getString("Current") + (selectRow + 1)
		    + bundle.getString("Items") + ","
		    + bundle.getString("Selected") + selectRowCount
		    + bundle.getString("Items") + ","
		    + bundle.getString("Total") + rowCount
		    + bundle.getString("Items"));
	} else {
	    label.setText(bundle.getString("Total") + rowCount
		    + bundle.getString("Items"));
	}
    }

    public static JPanel createStatusBar(JComponent otherPanel,
	    final JTable table) {
	JPanel panelAll = new JPanel(new GridLayout(1, 2));
	JPanel tableDescriptionPanel = new JPanel(new FlowLayout(
		FlowLayout.LEADING, 0, 0));
	final JLabel label = new JLabel("");
	table.getSelectionModel().addListSelectionListener(
		new ListSelectionListener() {

		    @Override
		    public void valueChanged(ListSelectionEvent e) {
			actionSelect(table, label);
		    }
		});
	table.getModel().addTableModelListener(new TableModelListener() {

	    @Override
	    public void tableChanged(TableModelEvent e) {
		actionSelect(table, label);
	    }
	});
	if (table.getModel() instanceof VisualTableModel) {
	    ((VisualTableModel) table.getModel())
		    .addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent pce) {
			    if (pce.getPropertyName().equals(
				    VisualTableModel.PROPERTY_MAX_COUNT_CHANGE)) {
				actionSelect(table, label);
			    }
			}
		    });
	}
	if (table.getRowSorter() != null) {
	    table.getRowSorter().addRowSorterListener(new RowSorterListener() {

		@Override
		public void sorterChanged(RowSorterEvent e) {
		    actionSelect(table, label);
		}
	    });
	}
	tableDescriptionPanel.add(label);
	panelAll.add(tableDescriptionPanel);
	if (otherPanel != null) {
	    panelAll.add(otherPanel);
	}
	actionSelect(table, label);
	return panelAll;
    }

    /**
     * remove repeat list
     * 
     * @param vecAlm
     *            vecAlm
     * @return List<List<UserAlmBean>>
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static <T> List<BatchBean<T>> deleteRepeatBeans(
	    VisualTableModel model, List<BatchBean<T>> batchBeans)
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	List<BatchBean<T>> noRepeatList = new ArrayList<BatchBean<T>>();
	Method idGetMethod = CacheUtil.getIdGetMethod(model.getBeanClass(),
		true);
	Map<Object, List<Integer>> indexMap = new HashMap<Object, List<Integer>>();
	for (int i = 0; i < batchBeans.size(); i++) {
	    BatchBean batchBean = batchBeans.get(i);
	    Object id = null;
	    if (batchBean.getType() == OperaType.ADD
		    || batchBean.getType() == OperaType.UPDATE) {
		id = idGetMethod.invoke(batchBean.getT());
	    } else {
		id = batchBean.getT();
	    }
	    if (!indexMap.containsKey(id)) {
		indexMap.put(id, new ArrayList<Integer>(Arrays
			.asList(new Integer[] { i })));
	    } else {
		List<Integer> indexs = indexMap.get(id);
		indexs.add(i);
	    }
	}
	for (Map.Entry<Object, List<Integer>> entry : indexMap.entrySet()) {
	    List<Integer> indexs = entry.getValue();
	    if (indexs.size() == 1) {
		noRepeatList.add(batchBeans.get(indexs.get(0).intValue()));
	    } else {
		BatchBean lastBatchBean = batchBeans.get(indexs.get(indexs
			.size() - 1));
		if (lastBatchBean.getType() == OperaType.UPDATE) {
		    boolean isNowAdd = false;
		    for (int i = 0; i < indexs.size() - 1; i++) {
			BatchBean frontBatchBean = batchBeans
				.get(indexs.get(i));
			if (frontBatchBean.getType() == OperaType.ADD) {
			    isNowAdd = true;
			}
		    }
		    if (isNowAdd) {
			lastBatchBean.setType(OperaType.ADD);
		    }
		}
		noRepeatList.add(lastBatchBean);
	    }
	}
	return noRepeatList;
    }

    public static <T> List<List<BatchBean<T>>> subListForBatchSingle(
	    List<BatchBean<T>> batchBeans) {
	List<List<BatchBean<T>>> lists = new ArrayList<List<BatchBean<T>>>();
	List<BatchBean<T>> addList = new ArrayList<BatchBean<T>>();
	List<BatchBean<T>> updateList = new ArrayList<BatchBean<T>>();
	List<BatchBean<T>> removeList = new ArrayList<BatchBean<T>>();
	for (BatchBean batchBean : batchBeans) {
	    if (batchBean.getType() == OperaType.ADD) {
		addList.add(batchBean);
	    } else if (batchBean.getType() == OperaType.UPDATE) {
		updateList.add(batchBean);
	    } else if (batchBean.getType() == OperaType.REMOVE) {
		removeList.add(batchBean);
	    }
	}
	if (!addList.isEmpty()) {
	    lists.add(addList);
	}
	if (!updateList.isEmpty()) {
	    lists.add(updateList);
	}
	if (!removeList.isEmpty()) {
	    lists.add(removeList);
	}
	return lists;
    }

    public static <T> List<T> toBeans(List<BatchBean<T>> batchBeans) {
	List<T> list = new ArrayList<T>();
	for (BatchBean<T> batchBean : batchBeans) {
	    list.add(batchBean.getT());
	}
	return list;
    }

    public static <T> Map<Integer, T> toUpdateBeans(
	    List<BatchBean<T>> batchBeans) {
	Map<Integer, T> map = new HashMap<Integer, T>();
	for (BatchBean<T> batchBean : batchBeans) {
	    map.put(batchBean.getPrimaryKey(), batchBean.getT());
	}
	return map;
    }

    public static <T> int[] toPrimaryKeys(List<BatchBean<T>> batchBeans) {
	int[] keys = new int[batchBeans.size()];
	for (int i = 0; i < batchBeans.size(); i++) {
	    keys[i] = batchBeans.get(i).getPrimaryKey();
	}
	return keys;
    }

    public static <T> List<BatchBean<T>> beanToBatchBeanForAdd(List<T> beans) {
	List<BatchBean<T>> batchBeans = new ArrayList<BatchBean<T>>();
	for (T t : beans) {
	    BatchBean<T> batchBean = new BatchBean<T>();
	    batchBean.setT(t);
	    batchBean.setType(OperaType.ADD);
	    batchBeans.add(batchBean);
	}
	return batchBeans;
    }

    public static <T> List<BatchBean<T>> beanToBatchBeanForUpdate(
	    Map<Integer, T> beanMap) {
	List<BatchBean<T>> batchBeans = new ArrayList<BatchBean<T>>();
	for (Map.Entry<Integer, T> entry : beanMap.entrySet()) {
	    BatchBean<T> batchBean = new BatchBean<T>();
	    batchBean.setT(entry.getValue());
	    batchBean.setType(OperaType.UPDATE);
	    batchBean.setPrimaryKey(entry.getKey());
	    batchBeans.add(batchBean);
	}
	return batchBeans;
    }

    public static <T> List<BatchBean<T>> beanToBatchBeanForRemove(
	    int[] primaryKeys) {
	List<BatchBean<T>> batchBeans = new ArrayList<BatchBean<T>>();
	for (int key : primaryKeys) {
	    BatchBean<T> batchBean = new BatchBean<T>();
	    batchBean.setPrimaryKey(key);
	    batchBean.setType(OperaType.REMOVE);
	    batchBeans.add(batchBean);
	}
	return batchBeans;
    }
}
