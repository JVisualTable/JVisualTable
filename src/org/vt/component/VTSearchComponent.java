package org.vt.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

import org.vt.JVisualTable;
import org.vt.beans.SearchCondition;
import org.vt.cache.orm.ColumnBean;
import org.vt.locale.ResourceManager;
import org.vt.locale.VTResourceBundle;
import org.vt.util.CacheUtil;

/**
 * Visual table search componenet
 * 
 * @author Zerg Law
 * 
 */
public class VTSearchComponent extends JTextField {

    private static final long serialVersionUID = -4540603582487928511L;
    private JVisualTable table;
    private static final int PADDING = 8;
    private ImageIcon searchIcon = new ImageIcon(VTSearchComponent.class
	    .getResource("searchSplit.png"));
    private ImageIcon deleteIcon = new ImageIcon(VTSearchComponent.class
	    .getResource("remove.png"));
    private String tip = "Search...";
    private boolean hasFocus = false;
    private boolean isHover = false;
    private JPopupMenu popupMenu = new JPopupMenu();
    private ButtonGroup bg = new ButtonGroup();

    public VTSearchComponent(JVisualTable table) {
	super(20);
	this.table = table;
	initListeners();
	popupMenu = createPopupMenu();
    }

    private int[] getDefaultSearchColumns() {
	List<Integer> arr = new ArrayList<Integer>();
	VisualTableModel model = (VisualTableModel) table.getModel();
	ColumnBean[] columnBeans = model.getColumnBeans();
	int index = 0;
	for (int i = 0; i < columnBeans.length; i++) {
	    ColumnBean columnBean = columnBeans[i];
	    if (columnBean.isShowInTable() && !columnBean.isHidden()) {
		if (columnBean.isDefaultSearch()) {
		    arr.add(index);
		}
		index++;
	    }
	}
	return CacheUtil.toIntArr(arr);
    }

    private AbstractAction getSearchAction() {
	return new AbstractAction() {

	    @Override
	    public void actionPerformed(ActionEvent ae) {
		MenuElement[] menuElements = popupMenu.getSubElements();
		for (int i = 0; i < menuElements.length; i++) {
		    if (menuElements[i] != null
			    && menuElements[i] instanceof JRadioButtonMenuItem) {
			JRadioButtonMenuItem radioButtonMenuItem = (JRadioButtonMenuItem) menuElements[i];
			if (radioButtonMenuItem.isSelected()) {
			    String str = getText();
			    int[] indexs = new int[] { i - 1 };
			    if (i == 0) {
				indexs = getDefaultSearchColumns();
			    }
			    SearchCondition searchCondition = new SearchCondition(
				    false, indexs, str);
			    if (str.equals("")) {
				searchCondition = null;
			    }
			    ((VisualTableModel) table.getModel())
				    .searchChange(searchCondition);
			}
		    }
		}
	    }
	};
    }

    private AbstractAction getClearAction() {
	return new AbstractAction() {

	    @Override
	    public void actionPerformed(ActionEvent ae) {
		setText("");
		SearchCondition searchCondition = null;
		((VisualTableModel) table.getModel())
			.searchChange(searchCondition);
	    }
	};
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
	InputMap map = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
	map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
	map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");

	getActionMap().put("search", getSearchAction());
	getActionMap().put("clear", getClearAction());
	FocusAdapter focusAdapter = new FocusAdapter() {

	    @Override
	    public void focusGained(FocusEvent e) {
		hasFocus = true;
		repaint();
	    }

	    @Override
	    public void focusLost(FocusEvent e) {
		hasFocus = false;
		repaint();
	    }
	};
	this.addFocusListener(focusAdapter);
	// 查询类型弹出菜单
	this.addMouseListener(new MouseAdapter() {

	    private boolean isNeedShow;
	    private boolean isNeedDelete;

	    @Override
	    public void mouseEntered(MouseEvent e) {
		isHover = true;
		repaint();
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		isHover = false;
		repaint();
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
		if (isInLeftIcon(e)) {
		    isNeedShow = true;
		}
		if (isInRightIcon(e)) {
		    isNeedDelete = true;
		}
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
		if (isInLeftIcon(e)) {
		    if (isNeedShow) {
			showTypePopup();
		    }
		}
		isNeedShow = false;

		if (isInRightIcon(e)) {
		    if (isNeedDelete) {
			getClearAction().actionPerformed(null);
		    }
		}
		isNeedDelete = false;
	    }
	});

	this.addMouseMotionListener(new MouseMotionListener() {

	    @Override
	    public void mouseDragged(MouseEvent e) {
	    }

	    @Override
	    public void mouseMoved(MouseEvent e) {
		if (isInLeftIcon(e) || isInRightIcon(e)) {
		    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else {
		    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		}
	    }
	});

	VisualTableModel model = (VisualTableModel) table.getModel();
	model.addPropertyChangeListener(new PropertyChangeListener() {

	    @Override
	    public void propertyChange(PropertyChangeEvent pce) {
		if (VisualTableModel.PROPERTY_COLUMN_CHANGE.equals(pce
			.getPropertyName())) {
		    VTSearchComponent.this.popupMenu = createPopupMenu();
		}
	    }
	});
    }

    public void showTypePopup() {
	JComponent firstMenuItem = (JComponent) popupMenu.getComponents()[1];
	firstMenuItem.setToolTipText("默认搜索列:" + getDefaultSearchColumnNames());
	if (popupMenu.isVisible()) {
	    popupMenu.setVisible(false);
	    return;
	}
	setPopupVisable(popupMenu, true);
	this.requestFocus();
    }

    /**
     * 设置弹出菜单可见性
     * 
     * @param popupMenu
     *            弹出菜单
     * @param visable
     *            true:可见 false:不可见
     */
    private void setPopupVisable(JPopupMenu popupMenu1, boolean visable) {
	if (popupMenu1 == popupMenu) {
	    popupMenu1.show(this, 0, 0);
	}
	if (visable) {
	    reLocation(popupMenu1);
	}
    }

    /**
     * 重定位弹出菜单
     * 
     * @param popupMenu
     *            弹出菜单
     */
    protected void reLocation(JPopupMenu popupMenu) {
	if (!popupMenu.isVisible()) {
	    return;
	}
	Rectangle bounds = this.getBounds();
	Point location = this.getLocationOnScreen();
	// popupMenu.setLocation(location.x, location.y + bounds.height - 1);
	popupMenu.setLocation(location.x - searchIcon.getIconWidth(),
		location.y + bounds.height);
    }

    /**
     * 是否是单击左边的图标
     * 
     * @param e
     *            事件
     * @return true:单击左边图标 false:非单击左边图标
     */
    protected boolean isInLeftIcon(MouseEvent e) {
	return e.getX() < searchIcon.getIconWidth() + PADDING;
    }

    /**
     * 是否是单击右边的图标
     * 
     * @param e
     *            事件
     * @return true:单击右边图标 false:非单击右边图标
     */
    private boolean isInRightIcon(MouseEvent e) {
	return e.getX() > getWidth() - getInsets().right;
    }

    @Override
    public void paint(Graphics g) {
	super.paint(g);
	int x = getInsets().left / 2 - searchIcon.getIconWidth() / 2;
	int y = getSize().height / 2 - searchIcon.getIconHeight() / 2;
	Color color = g.getColor();
	if (isHover) {
	    g.setColor(new Color(233, 233, 241));
	    g.fillRect(x, y, searchIcon.getIconWidth(), searchIcon
		    .getIconHeight());
	    g.setColor(color);
	}
	g.drawImage(searchIcon.getImage(), x, y, searchIcon.getIconWidth(),
		searchIcon.getIconHeight(), null);

	if (!hasFocus && getText().trim().equals("")) {
	    FontMetrics fontMetrics = g.getFontMetrics();
	    int height = (int) fontMetrics.getStringBounds(tip, g).getHeight();
	    g.setColor(Color.GRAY);
	    g.drawString(tip, getInsets().left, getSize().height / 2 + height
		    / 2 - 2);
	}
	if (!getText().trim().equals("")) {
	    x = getWidth() - getInsets().right + getInsets().right / 2
		    - searchIcon.getIconWidth() / 2;
	    g.drawImage(deleteIcon.getImage(), x, y, null);
	}
    }

    @Override
    public Insets getInsets() {
	Insets insets = super.getInsets();
	insets.left = searchIcon.getIconWidth() + PADDING;
	insets.right = deleteIcon.getIconWidth() + PADDING;
	return insets;
    }

    private String getDefaultSearchColumnNames() {
	List<String> arr = new ArrayList<String>();
	VisualTableModel model = (VisualTableModel) table.getModel();
	ColumnBean[] columnBeans = model.getColumnBeans();
	for (int i = 0; i < columnBeans.length; i++) {
	    ColumnBean columnBean = columnBeans[i];
	    if (columnBean.isShowInTable() && !columnBean.isHidden()) {
		if (columnBean.isDefaultSearch()) {
		    arr.add(columnBean.getTableColumnName());
		}
	    }
	}
	return arr.toString();
    }

    protected JPopupMenu createPopupMenu() {
	final JPopupMenu typeMenu = new JScrollPopupMenu();
	// 添加列的菜单
	final JRadioButtonMenuItem defaultColumns = new JRadioButtonMenuItem(
		ResourceManager.getBundle(VTResourceBundle.class).getString(
			"chooseAll"));

	bg.add(defaultColumns);
	defaultColumns.setSelected(true);
	typeMenu.add(defaultColumns);
	VisualTableModel model = (VisualTableModel) table.getModel();
	ColumnBean[] columnBeans = model.getColumnBeans();
	for (int i = 0; i < columnBeans.length; i++) {
	    ColumnBean columnBean = columnBeans[i];
	    if (columnBean.isShowInTable() && !columnBean.isHidden()) {
		final JRadioButtonMenuItem columnItem = new JRadioButtonMenuItem(
			columnBean.getColumnName());
		columnItem.putClientProperty("columnIndex", i);
		bg.add(columnItem);
		typeMenu.add(columnItem);
	    }
	}
	JMenuItem menuItem = new JMenuItem("自定义默认搜索列");
	typeMenu.add(menuItem);

	menuItem.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		DefaultSearchColumnChooserDialog defaultSearchColumnChooserDialog = new DefaultSearchColumnChooserDialog(
			table);
		defaultSearchColumnChooserDialog.setVisible(true);
	    }
	});
	return typeMenu;
    }
}
