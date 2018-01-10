package demo;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vt.JVisualTable;
import org.vt.component.VisualTableModel;
import org.vt.locale.ResourceManager;
import org.vt.util.ScrollBarShowTipsUtil;
import org.vt.util.VisualTableUtil;

/**
 * asynchronous add datas
 * 
 * @author Zerg Law
 * 
 */
public class AsynchronousVisualTableDemo {
    private JVisualTable table = new JVisualTable(new VisualTableModel(
	    SimpleBean.class));
    private Logger logger = Logger.getLogger(AsynchronousVisualTableDemo.class
	    .getName());

    private JFrame createFrame() {
	JFrame frame = new JFrame("Simple Visual Table Demo");
	frame.setSize(800, 600);
	frame.setLocationRelativeTo(null);
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frame.addWindowListener(new WindowAdapter() {

	    @Override
	    public void windowClosed(WindowEvent e) {
		System.exit(0);
	    }
	});
	return frame;
    }

    private JPanel createPanel() {
	JPanel panel = new JPanel(new BorderLayout());

	JScrollPane jsp = new JScrollPane(table);
	ScrollBarShowTipsUtil.bindVerticalScrollBarTips(table, jsp);

	panel.add(jsp, BorderLayout.CENTER);
	VisualTableUtil.registerTableHeadPopupMenu(table);
	VisualTableModel model = (VisualTableModel) table.getModel();
	model.setResourceBundle(ResourceManager.getBundle(getClass()));
	initData(model);
	return panel;
    }

    int sum = 0;

    private void initData(final VisualTableModel model) {

	Thread thread = new Thread() {
	    public void run() {
		for (int a = 0; a < 100; a++) {
		    List<SimpleBean> beans = new ArrayList<SimpleBean>();
		    for (int i = 0; i < 10000; i++) {
			SimpleBean bean = new SimpleBean();
			bean.setId(sum);
			sum++;
			bean.setBooleanValue(new Random().nextBoolean() + "");

			bean.setShortValue((short) (i));

			bean.setLongValue(Math.abs(new Random().nextLong()));

			bean.setDoubleValue(new Random().nextDouble());

			bean.setStringValue("Cell Value:" + i);

			bean.setDateValue(new Date());

			bean.setUserObject(new UserObject(bean.getId(), System
				.currentTimeMillis()
				+ ""));
			beans.add(bean);
		    }
		    model.addBeans(beans, false);
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
			logger.log(Level.WARNING, e.getMessage());
		    }
		}
	    }
	};
	thread.start();
    }

    private void showDemo() {
	JFrame frame = createFrame();
	frame.getContentPane().add(createPanel());
	frame.getContentPane().add(
		VisualTableUtil.createStatusBar(null, table),
		BorderLayout.SOUTH);
	frame.setVisible(true);
    }

    public static void main(String[] args) {
	AsynchronousVisualTableDemo demo = new AsynchronousVisualTableDemo();
	demo.showDemo();
    }
}
