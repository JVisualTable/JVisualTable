package org.vt.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;

/**
 * Scroll bar adapter
 * 
 * @author Zerg Law
 */
public class ScrollBarShowTipsUtil {

    public static void bindVerticalScrollBarTips(final JTable table,
	    JScrollPane scrollPane) {
	JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
	if (verticalScrollBar.getUI() == null
		|| !(verticalScrollBar.getUI() instanceof BasicScrollBarUI)) {
	    System.err
		    .println("bindVerticalScrollBarTips fail. The VerticalScrollBar's UI is not BasicScrollBarUI.");
	    return;
	}
	BasicScrollBarUI scrollBarUI = (BasicScrollBarUI) verticalScrollBar
		.getUI();

	if (scrollPane.getUI() == null
		|| !(scrollPane.getUI() instanceof BasicScrollPaneUI)) {
	    System.err
		    .println("bindVerticalScrollBarTips fail. The ScrollPane's UI is not BasicScrollPaneUI.");
	    return;
	}
	BasicScrollPaneUI scrollPaneUI = (BasicScrollPaneUI) scrollPane.getUI();

	Object changeListener = getFieldValue(scrollPaneUI, getField(
		scrollPaneUI.getClass(), "vsbChangeListener"));

	if (changeListener == null
		|| !(changeListener instanceof ChangeListener)) {
	    System.err
		    .println("bindVerticalScrollBarTips fail. The ScrollPane's vsbChangeListener can't find.");
	    return;
	}
	ChangeListener vsbChangeListener = (ChangeListener) changeListener;

	MouseListener[] mouArray = verticalScrollBar.getMouseListeners();
	MouseMotionListener[] mouMotArray = verticalScrollBar
		.getMouseMotionListeners();
	MouseWheelListener wheelLis = (MouseWheelListener) getFieldValue(
		scrollPaneUI, getField(scrollPaneUI.getClass(),
			"mouseScrollListener"));
	scrollPane.removeMouseWheelListener(wheelLis);
	verticalScrollBar.removeMouseListener(mouArray[0]);
	verticalScrollBar.removeMouseMotionListener(mouMotArray[0]);
	TipShowMouseAdapter showMouseAdapter = new TipShowMouseAdapter(
		mouArray[0], mouMotArray[0], wheelLis, scrollBarUI,
		verticalScrollBar, table, vsbChangeListener);
	if (verticalScrollBar != null) {
	    verticalScrollBar.addMouseListener(showMouseAdapter);
	    verticalScrollBar.addMouseMotionListener(showMouseAdapter);
	}
	scrollPane.addMouseWheelListener(showMouseAdapter);
    }

    private static class TipShowMouseAdapter extends MouseAdapter implements
	    MouseMotionListener, MouseWheelListener {

	private JToolTip tip;
	private Popup tipWindow;
	private JTable table;
	private boolean isDragged;
	private ChangeListener vsbChangeListener;
	private MouseListener mouseLis;
	private MouseMotionListener mouseMotLis;
	private MouseWheelListener wheelLis;
	private BasicScrollBarUI barUI;
	private JScrollBar scrollbar;
	private int scrollBarValue;
	private int scrollBarStartValue;

	public TipShowMouseAdapter(MouseListener mouseLis,
		MouseMotionListener mouseMoLis, MouseWheelListener wheelLis,
		BasicScrollBarUI barUI, JScrollBar scrollbar, JTable table,
		ChangeListener vsbLis) {
	    this.mouseLis = mouseLis;
	    this.mouseMotLis = mouseMoLis;
	    this.wheelLis = wheelLis;
	    this.barUI = barUI;
	    this.scrollbar = scrollbar;
	    this.tip = new JToolTip();
	    this.table = table;
	    this.vsbChangeListener = vsbLis;
	}

	public JTable getTable() {
	    return table;
	}

	public JToolTip getTip() {
	    return tip;
	}

	public Popup getTipWindow() {
	    return tipWindow;
	}

	public void setTipWindow(Popup tipWindow) {
	    this.tipWindow = tipWindow;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	    wheelLis.mouseWheelMoved(e);
	    if (scrollbar.isShowing()) {
		if (isOverTable(e, table, scrollbar)) {
		    if (tipWindow != null) {
			tipWindow.hide();
		    }
		    return;
		}
		showTipWindow(this, e, getTooltips(), scrollbar);
	    }
	}

	@Override
	public void mouseReleased(MouseEvent e) {

	    scrollbar.getModel().addChangeListener(vsbChangeListener);

	    if (isDragged) {
		scrollbar.setValue(scrollBarValue);
	    }
	    mouseLis.mouseReleased(e);

	    if (isOverTable(e, table, scrollbar)) {
		if (tipWindow != null) {
		    tipWindow.hide();
		}
	    }
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    mouseMotLis.mouseMoved(e);
	    if (isOverThumb(e, scrollbar)) {
		if (this.getTipWindow() != null) {
		    this.getTipWindow().hide();
		}
		return;
	    }
	    showTipWindow(this, e, getTooltips(), scrollbar);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	    mouseLis.mouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
	    mouseLis.mouseExited(e);
	    if (tipWindow != null) {
		tipWindow.hide();
	    }
	}

	@Override
	public void mousePressed(MouseEvent e) {
	    isDragged = false;
	    mouseLis.mousePressed(e);
	    scrollBarStartValue = (Integer) getFieldValue(barUI, getField(barUI
		    .getClass(), "scrollBarValue"));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	    scrollbar.getModel().removeChangeListener(vsbChangeListener);
	    isDragged = true;
	    scrollBarValue = (Integer) getFieldValue(barUI, getField(barUI
		    .getClass(), "scrollBarValue"));
	    mouseMotLis.mouseDragged(e);
	    showTipWindow(this, e, getTooltips(), scrollbar);
	    if (isOverThumb(e, scrollbar) && tipWindow != null) {
		tipWindow.hide();
	    }
	}

	private boolean isOverTable(MouseEvent event, JTable table,
		JScrollBar bar) {
	    int tableX = (int) table.getLocationOnScreen().getX();
	    int clickX = (int) event.getPoint().getX();
	    if (scrollbar.getLocationOnScreen() == null) {
		return false;
	    }
	    int x = (int) (scrollbar.getLocationOnScreen().getX());
	    return clickX + tableX < x;
	}

	private boolean isOverThumb(MouseEvent event, JScrollBar bar) {
	    Rectangle thumb = getThumbBounds(bar);
	    Point click = event.getLocationOnScreen();
	    int y = (int) (thumb.getY() + bar.getLocationOnScreen().getY());
	    int height = (int) thumb.getHeight();
	    return click.y < y || click.y > y + height;
	}

	public String getTooltips() {
	    scrollBarValue = (Integer) getFieldValue(barUI, getField(barUI
		    .getClass(), "scrollBarValue"));
	    int[] result = getViewportRows(table, scrollBarValue);
	    return "From " + result[0] + " To " + result[1] + " Items，Row Sum:"
		    + table.getRowCount();
	}
    }

    public static int[] getViewportRows(JTable table, int scrollBarValue) {
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

    private static Rectangle getThumbBounds(JScrollBar scrollBar) {
	ScrollBarUI ui = scrollBar.getUI();
	if (ui instanceof BasicScrollBarUI) {
	    BasicScrollBarUI parUI = (BasicScrollBarUI) ui;
	    return (Rectangle) invoke(parUI, "getThumbBounds", null, null);
	}
	throw new RuntimeException("can't find scrollbar's BasicScrollBarUI..");
    }

    private static Object invoke(Object obj, final String methodName,
	    final Class[] classes, final Object[] objects) {
	Method method;
	try {
	    method = getMethod(obj.getClass(), methodName, classes);
	    method.setAccessible(true);
	    return method.invoke(obj, objects);
	} catch (Exception ex) {
	    Logger.getLogger(ScrollBarShowTipsUtil.class.getName()).log(
		    Level.SEVERE, null, ex);
	    throw new RuntimeException(
		    "can't find the method or parameters are wrong");
	}
    }

    private static Method getMethod(Class clazz, String methodName,
	    final Class[] classes) throws Exception {
	Method method = null;
	try {
	    method = clazz.getDeclaredMethod(methodName, classes);
	} catch (NoSuchMethodException e) {
	    try {
		method = clazz.getMethod(methodName, classes);
	    } catch (NoSuchMethodException ex) {
		if (clazz.getSuperclass() == null) {
		    return method;
		} else {
		    method = getMethod(clazz.getSuperclass(), methodName,
			    classes);
		}
	    }
	}
	return method;
    }

    private static Object getFieldValue(Object obj, Field f) {
	try {
	    f.setAccessible(true);
	    return f.get(obj);
	} catch (Exception e) {
	    Logger.getLogger(ScrollBarShowTipsUtil.class.getName()).log(
		    Level.SEVERE, null, e);
	    throw new RuntimeException("can't find the fieldValue");
	}
    }

    private static Field getField(Class clazz, final String fieldName) {
	Field f = null;
	try {
	    f = clazz.getDeclaredField(fieldName);
	} catch (NoSuchFieldException e) {
	    try {
		f = clazz.getField(fieldName);
	    } catch (Exception ex) {
		if (clazz.getSuperclass() == null) {
		    return f;
		} else {
		    f = getField(clazz.getSuperclass(), fieldName);
		}
	    }
	}
	return f;
    }

    private static void showTipWindow(TipShowMouseAdapter adapter,
	    MouseEvent e, String tipText, JScrollBar scrollBar) {
	if (adapter.getTipWindow() != null) {
	    adapter.getTipWindow().hide();
	}
	JComponent component = (JComponent) e.getSource();
	Point preferredLocation = component.getToolTipLocation(e);
	preferredLocation = component.getToolTipLocation(e);
	Dimension size;
	Point screenLocation = scrollBar.getLocationOnScreen();
	Point location;
	Point toFind;
	Popup tipWindow;
	if (preferredLocation != null) {
	    toFind = new Point(screenLocation.x + preferredLocation.x,
		    screenLocation.y + preferredLocation.y);
	} else {
	    toFind = e.getLocationOnScreen();

	    if (e instanceof MouseWheelEvent) {
		int scrollbarY = (int) scrollBar.getLocationOnScreen().getY();
		int tableX = (int) adapter.getTable().getLocationOnScreen()
			.getX();
		toFind.x = e.getX() + tableX;
		toFind.y = e.getY() + scrollbarY;
	    }
	}
	GraphicsConfiguration gc = getDrawingGC(toFind);
	if (gc == null) {
	    toFind = e.getLocationOnScreen();
	    if (e instanceof MouseWheelEvent) {
		int scrollbarY = (int) scrollBar.getLocationOnScreen().getY();
		int tableX = (int) adapter.getTable().getLocationOnScreen()
			.getX();
		toFind.x = e.getX() + tableX;
		toFind.y = e.getY() + scrollbarY;
	    }
	    gc = getDrawingGC(toFind);
	    if (gc == null) {
		gc = scrollBar.getGraphicsConfiguration();
	    }
	}
	Rectangle sBounds = gc.getBounds();
	Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
	sBounds.x += screenInsets.left;
	sBounds.y += screenInsets.top;
	int fixBoundsW = sBounds.width;
	size = adapter.getTip().getPreferredSize();
	if (toFind.x > size.getWidth()) {
	    sBounds.width -= (screenInsets.left + screenInsets.right
		    + screenInsets.left + (fixBoundsW - toFind.x) + 20);
	} else {
	    sBounds.x = toFind.x + 20;
	}
	sBounds.height -= (screenInsets.top + screenInsets.bottom);
	boolean leftToRight = isLeftToRight(scrollBar);
	if (preferredLocation != null) {
	    location = toFind;
	    if (!leftToRight) {
		location.x -= size.width;
	    }
	} else {
	    location = new Point(screenLocation.x + e.getX(), screenLocation.y
		    + e.getY() + 1);
	    if (e instanceof MouseWheelEvent) {
		location = new Point(screenLocation.x, screenLocation.y
			+ e.getY() - 27);
	    }
	    if (!leftToRight) {
		if (location.x - size.width >= 0) {
		    location.x -= size.width;
		}
	    }
	}
	if (location.x < sBounds.x) {
	    location.x = sBounds.x;
	} else if (location.x - sBounds.x + size.width > sBounds.width) {
	    location.x = sBounds.x + Math.max(0, sBounds.width - size.width);
	}
	if (location.y < sBounds.y) {
	    location.y = sBounds.y;
	} else if (location.y - sBounds.y + size.height > sBounds.height) {
	    location.y = sBounds.y + Math.max(0, sBounds.height - size.height);
	}
	toFind = e.getLocationOnScreen();
	if (e instanceof MouseWheelEvent) {
	    int scrollbarY = (int) scrollBar.getLocationOnScreen().getY();
	    int tableX = (int) adapter.getTable().getLocationOnScreen().getX();
	    toFind.x = e.getX() + tableX;
	    toFind.y = e.getY() + scrollbarY;
	}
	adapter.getTip().setTipText(tipText);
	adapter.setTipWindow(PopupFactory.getSharedInstance().getPopup(
		scrollBar, adapter.getTip(), location.x, location.y));
	adapter.getTipWindow().show();
    }

    private static GraphicsConfiguration getDrawingGC(Point toFind) {
	GraphicsEnvironment env = GraphicsEnvironment
		.getLocalGraphicsEnvironment();
	GraphicsDevice devices[] = env.getScreenDevices();
	for (GraphicsDevice device : devices) {
	    GraphicsConfiguration configs[] = device.getConfigurations();
	    for (GraphicsConfiguration config : configs) {
		Rectangle rect = config.getBounds();
		if (rect.contains(toFind)) {
		    return config;
		}
	    }
	}
	return null;
    }

    private static boolean isLeftToRight(Component comp) {
	return comp.getComponentOrientation().isLeftToRight();
    }

}
