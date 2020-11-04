package io.demo.weichai.req;

import java.awt.BorderLayout;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PluginMain {
	
	public static void run(String[] args) {
		int numIssues = getMKSSINISSUE();
		List<String> ids = getIssueIds(numIssues);
		System.out.println("MKSSI_NISSUE = " + numIssues + "; ids = " + ids);
		if(ids.size() == 0) {
			return;
		}
		JFileChooser jfc=new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileNameExtensionFilter filter=new FileNameExtensionFilter("docx文件", "docx");
		jfc.setFileFilter(filter);
		int ret = jfc.showDialog(new JLabel(), "导入技术协议文档");
		if(ret==JFileChooser.APPROVE_OPTION){
			//结果为：用户要保存的文件路径
			File file=jfc.getSelectedFile();
			System.out.println("file path = " + file.getAbsolutePath());
			// 初始化导入进度指示UI
			JFrame mainFrame = new JFrame();
			mainFrame.setSize(350, 100);
			mainFrame.setLocationRelativeTo(null);
			mainFrame.setUndecorated(true);
			Border border = BorderFactory.createRaisedBevelBorder();
			JPanel panel = new JPanel(new BorderLayout());
			mainFrame.add(panel);
			panel.setBorder(border);
			JLabel label = new JLabel("0% 正在解析...", JLabel.CENTER);
			panel.add(label,BorderLayout.CENTER);
			mainFrame.setVisible(true);
			// 启动导入程序
			Thread thread = new Thread(() -> {
				App.run(file, ids.get(0));
			});
			thread.start();
			
			try {
				while(true) {
					if("Importing".equals(App.stauts)) {
						int size = DocumentBuilder.getDocumentSize();
						label.setText(String.format("%s/%s 正在导入...", App.imported, size));
					} else if("Completed".equals(App.stauts)) {
						break;
					}
					Thread.sleep(500L);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			mainFrame.setVisible(false);
			mainFrame.dispose();
		};
	}

	private static List<String> getIssueIds(int numIssues) {
		List<String> ids = new LinkedList<>();
		for (int i = 0; i < numIssues; i++) {
			ids.add(getIssueId(i));
	    }
		return ids;
	}

	private static String getIssueId(int i) {
		String key = new StringBuffer("MKSSI_ISSUE").append(Integer.toString(i)).toString();
		String id = System.getProperty(key);
		if(id != null && !"".equals(id.trim())) {
			return id.trim();
		}
		return System.getenv(new StringBuilder("MKSSI_ISSUE").append(Integer.toString(i)).toString());
	}

	private static int getMKSSINISSUE() {
		String nissue = System.getProperty("MKSSI_NISSUE");
		if(nissue != null && !"".equals(nissue.trim())) {
			return Integer.valueOf(nissue.trim());
		}
		return Integer.parseInt(System.getenv("MKSSI_NISSUE") == null ? "0" : System.getenv("MKSSI_NISSUE"));
	}
	
	public static void main(String[] args) {
		PluginMain.run(args);
	}
}
