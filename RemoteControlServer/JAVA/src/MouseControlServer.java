import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MouseControlServer extends JFrame {
	private static int port;
	private static double mx; // 电脑鼠标的横坐标
	private static double my; // 电脑鼠标的纵坐标
	private DatagramSocket socket;
	ServerThread serverthread; // 初始化线程
	final JLabel messagebox;
	final JTextField field;
	final JButton stopbutton;
	final JButton startbutton;
	String message = null;
	String[] messages = null;
	String type = null;
	String info = null;

	public MouseControlServer() {
		super();
		setTitle("Mouse Server");
		setSize(230, 220);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Toolkit toolkit = getToolkit(); // 获得Toolkit对象
		Dimension dimension = toolkit.getScreenSize(); // 获得Dimension对象
		int screenHeight = dimension.height; // 获得屏幕的高度
		int screenWidth = dimension.width; // 获得屏幕的宽度
		int frm_Height = this.getHeight(); // 获得窗体的高度
		int frm_width = this.getWidth(); // 获得窗体的宽度
		setLocation((screenWidth - frm_width) / 2,
				(screenHeight - frm_Height) / 2); // 使用窗体居中显示

		getContentPane().setLayout(null);
		final JLabel label = new JLabel();
		try {
			label.setText("本机IP：" + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		label.setBounds(10, 20, 300, 25);
		Font font = new Font("Label", Font.PLAIN, 16);
		label.setFont(font);
		getContentPane().add(label);

		final JLabel label2 = new JLabel();
		label2.setText("请输入端口号：");
		label2.setBounds(10, 50, 100, 25);
		getContentPane().add(label2);

		field = new JTextField();
		field.setBounds(110, 50, 90, 25);
		field.setText("23333");
		getContentPane().add(field);

		startbutton = new JButton();
		startbutton.setText("开启");
		startbutton.setBounds(10, 90, 80, 25);
		getContentPane().add(startbutton);

		stopbutton = new JButton();
		stopbutton.setText("关闭");
		stopbutton.setEnabled(false);
		stopbutton.setBounds(120, 90, 80, 25);
		getContentPane().add(stopbutton);

		final JLabel label3 = new JLabel();
		label3.setText("请在手机端输入 本机IP 和 端口号");
		label3.setBounds(10, 120, 280, 25);
		getContentPane().add(label3);

		messagebox = new JLabel();
		messagebox.setText("");
		messagebox.setBounds(10, 150, 190, 25);
		getContentPane().add(messagebox);

		final JLabel label4 = new JLabel();
		label4.setText("By JXJ");
		label4.setBounds(160, 150, 190, 25);
		getContentPane().add(label4);

		startbutton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String str = field.getText().trim();
				int num;
				if (str.equals("")) {
					JOptionPane.showMessageDialog(null, "输入信息不能为空");
					return;
				}
				try {
					num = Integer.parseInt(str);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "端口号应该为数字");
					return;
				}
				if (num < 0 || num > 65535) {
					JOptionPane.showMessageDialog(null, "端口号应该大于0小于65535");
					return;
				}
				port = num;
				stopbutton.setEnabled(true);
				startbutton.setEnabled(false);
				start();

			}
		});

		stopbutton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				stop();
			}
		});

		setVisible(true);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		java.awt.EventQueue.invokeLater(new Runnable() {

			public void run() {
				new MouseControlServer();
			}
		});

	}

	public void start() {
		serverthread = new ServerThread();
		serverthread.start();
		messagebox.setText("开启信息监听");
		field.setEditable(false);

	}

	public void stop() {
		System.exit(0);
	}

	public class ServerThread extends Thread {

		public void run() {
			try {
				try {
					// 创建一个DatagramSocket对象，并指定监听的端口号
					socket = new DatagramSocket(port);
				} catch (Exception e) {
					messagebox.setText("端口被使用,请更换端口");
					startbutton.setEnabled(true);
					stopbutton.setEnabled(false);
					field.setEditable(true);
					return;
				}
				byte data[] = new byte[1024];
				// 创建一个空的DatagramPacket对象
				DatagramPacket packet = new DatagramPacket(data, data.length);
				// 使用receive方法接收客户端所发送的数据
				System.out.println("开启端口监听" + socket.getLocalPort());
				while (true) {
					socket.receive(packet);
					message = new String(packet.getData(), packet.getOffset(),
							packet.getLength(), "UTF-8");
//					System.out.println("message--->" + message);
					messages = message.split(":");
					if (messages.length >= 2) {
						type = messages[0];
						info = messages[1];
						if (type.equals("mouse"))
							MouseMove(info);
						if (type.equals("leftButton"))
							LeftButton(info);
						if (type.equals("rightButton"))
							RightButton(info);
						if (type.equals("mousewheel"))
							MouseWheel(info);
						if (type.equals("keyboard"))
							KeyBoard(info);
					}

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void MouseMove(String info) {
			String args[] = info.split(",");
			String x = args[0];
			String y = args[1];
			float px = Float.valueOf(x);
			float py = Float.valueOf(y);

			PointerInfo pinfo = MouseInfo.getPointerInfo(); // 得到鼠标的坐标
			java.awt.Point p = pinfo.getLocation();
			mx = p.getX(); // 得到当前电脑鼠标的坐标
			my = p.getY();
			java.awt.Robot robot;
			try {
				robot = new Robot();
				System.out.println(mx + "," + my);
				System.out.println(px + "," + py);
				robot.mouseMove((int) mx + (int) px, (int) my + (int) py);
			} catch (AWTException e) {
				e.printStackTrace();
			}

		}

		public void LeftButton(String info) throws AWTException {
			java.awt.Robot robot = new Robot();
			if ("down".equals(info))
				robot.mousePress(InputEvent.BUTTON1_MASK);
			else if ("up".equals(info))
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
			else {
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
			}
		}

		public void RightButton(String info) throws AWTException {
			java.awt.Robot robot = new Robot();
			robot.mousePress(InputEvent.BUTTON3_MASK);
			robot.mouseRelease(InputEvent.BUTTON3_MASK);
		}

		public void MouseWheel(String info) throws AWTException {
			java.awt.Robot robot = new Robot();
			float num = Float.valueOf(info);
			if (num > 0)
				robot.mouseWheel(1);
			else
				robot.mouseWheel(-1);
		}

		public void KeyBoard(String info) throws Exception {
			String args[] = info.split(",", 2);
			String type = null;
			String cont = null;
			java.awt.Robot robot = new Robot();
			type = args[0];
			cont = args[1];
			// 最好"message".equals(type)这么写，这样不会空指针异常
			if (type.equals("message")) {
				Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
				cb.setContents(new StringSelection(cont), null);// 调用粘贴板

				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.keyPress(KeyEvent.VK_V);
				robot.keyRelease(KeyEvent.VK_CONTROL);
				robot.keyRelease(KeyEvent.VK_V);
			} else if (type.equals("key")) {
				if (cont.equals("BackSpace")) {
					robot.keyPress(KeyEvent.VK_BACK_SPACE);
					robot.keyRelease(KeyEvent.VK_BACK_SPACE);
				} else if (cont.equals("Enter")) {
					robot.keyPress(KeyEvent.VK_ENTER);
					robot.keyRelease(KeyEvent.VK_ENTER);
				} else if (cont.equals("Up")) {
					robot.keyPress(KeyEvent.VK_UP);
					robot.keyRelease(KeyEvent.VK_UP);
				} else if (cont.equals("Down")) {
					robot.keyPress(KeyEvent.VK_DOWN);
					robot.keyRelease(KeyEvent.VK_DOWN);
				} else if (cont.equals("Left")) {
					robot.keyPress(KeyEvent.VK_LEFT);
					robot.keyRelease(KeyEvent.VK_LEFT);
				} else if (cont.equals("Right")) {
					robot.keyPress(KeyEvent.VK_RIGHT);
					robot.keyRelease(KeyEvent.VK_RIGHT);
				} else if (cont.equals("Space")) {
					robot.keyPress(KeyEvent.VK_SPACE);
					robot.keyRelease(KeyEvent.VK_SPACE);
				} else if (cont.equals("Back")) {
					robot.keyPress(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_LEFT);
					robot.keyRelease(KeyEvent.VK_LEFT);
					robot.keyRelease(KeyEvent.VK_ALT);
				} else if (cont.equals("Forward")) {
					robot.keyPress(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_RIGHT);
					robot.keyRelease(KeyEvent.VK_RIGHT);
					robot.keyRelease(KeyEvent.VK_ALT);
				} else if (cont.equals("F5")) {
					robot.keyPress(KeyEvent.VK_F5);
					robot.keyRelease(KeyEvent.VK_F5);
				} else if (cont.equals("ESC")) {
					robot.keyPress(KeyEvent.VK_ESCAPE);
					robot.keyRelease(KeyEvent.VK_ESCAPE);
				} else if (cont.equals("Ctrl+W")) {
					robot.keyPress(KeyEvent.VK_CONTROL);
					robot.keyPress(KeyEvent.VK_W);
					robot.keyRelease(KeyEvent.VK_W);
					robot.keyRelease(KeyEvent.VK_CONTROL);
				}
			}
		}
	}

}
