package jxj.mousecontrol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class MouseControlActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener {
	private VelocityTracker vTracker = null;
	private DatagramSocket socket;
	private DatagramPacket packet;
	private Button bt_left;
	private Button bt_right;
	private RadioGroup mouse_sensitivity;
	private boolean key_flag;
	private boolean up_flag; // 第二个手指抬起的flag
	private Toast toast;
	private static int sensitivity = 1;
	private static float mx = 0; // 发送的鼠标移动的差值
	private static float my = 0;
	private static float my2 = 0;
	private static float lx; // 记录上次的位置
	private static float ly;
	private static float ly2; // 记录第二根手指上次的位置
	private static float fx; // 手指第一次接触屏幕时的坐标
	private static float fy;
	private float vy1; // 手指速度
	private float vy2; //速度的和
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);
		try {
			socket = new DatagramSocket();// 这里要Internet权限
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getButton();
		initTouch();
		new Thread() {
			public void run() {
				while (true) {
					try {
						//这是匿名内部类，在这里面MouseControlActivity.this和this是不同的
						//下面notify的this是Activity，所以这里要写MouseControlActivity。this
						//wait和notify外面要用synchronized包住
						synchronized (MouseControlActivity.this) {
							MouseControlActivity.this.wait();
						}
						socket.send(packet);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		}.start();

	}

	private void getButton() {
		// TODO Auto-generated method stub
		bt_left = (Button) findViewById(R.id.bt_left);
		bt_right = (Button) findViewById(R.id.bt_right);
		bt_right.setOnClickListener(this);
		mouse_sensitivity = (RadioGroup) findViewById(R.id.mouse_sensitivity);
		mouse_sensitivity.setOnCheckedChangeListener(this);
		bt_left.setOnTouchListener(new OnTouchListener() {
			//这么写是为了左键支持拖动
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// 这里填写你按下要处理的事情，这里要返回true才能触发提起事件
					bt_left.setBackgroundResource(R.drawable.button_pressed);
					sendMessage("leftButton:down");
					return true;

				case MotionEvent.ACTION_UP:
					// 这里写上按钮提起时处理的事情，这里要返回true才能触发提起事件
					bt_left.setBackgroundResource(R.drawable.button);
					sendMessage("leftButton:up");
					return true;
				}
				return false;
			}
		});
	}

	private void initTouch() {
		// TODO Auto-generated method stub
		FrameLayout touch = (FrameLayout) this.findViewById(R.id.panel);

		touch.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				//把arg1.getAction()和 MotionEvent.ACTION_MASK进行与运算后可以响应ACTION_POINTER_DOWN和ACTION_POINTER_UP
				switch (arg1.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					onMouseDown(arg1);
					break;
				case MotionEvent.ACTION_MOVE:
					if (arg1.getPointerCount() == 1) {
						//双指滚动后，为了在第二个手指抬起时鼠标位置不发生跳动。
						if (up_flag) {
							lx = arg1.getX();
							ly = arg1.getY();
							up_flag = false;
						}
						onMouseMove(arg1);
					} else if (arg1.getPointerCount() == 2) {
						onMiddleButtonMove(arg1);
					}
					break;
				case MotionEvent.ACTION_UP:
					onMouseUp(arg1);
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					onMiddleButtonDown(arg1);
					break;
				case MotionEvent.ACTION_POINTER_UP:
					up_flag = true;
					vTracker.clear();
					vTracker.recycle();
					vTracker = null;
					break;
				}

				return true;
			}
		});

	}

	private void onMiddleButtonDown(MotionEvent ev) {
		ly = ev.getY(0);
		ly2 = ev.getY(1);
		if (vTracker == null)
			vTracker = VelocityTracker.obtain();
		vTracker.addMovement(ev);

	}

	private void onMiddleButtonMove(MotionEvent ev) {
		vTracker.addMovement(ev);
		vTracker.computeCurrentVelocity(1000);
		vy1 = vTracker.getYVelocity();
		vy2 += vy1;
		float y = ev.getY(0);
		float y2 = ev.getY(1);
		my = y - ly;
		ly = y;
		my2 = y2 - ly2;
		ly2 = y2;
		if ((my > 0 && my2 > 0 && vy2 > 2000) || (my < 0 && my2 < 0 && vy2 < -2000)) {
			String str = "mousewheel" + ":" + my;
			sendMessage(str);
			vy2 = 0;
		}

	}

	private void onMouseDown(MotionEvent ev) {
		lx = ev.getX(); // 当手机第一放入时 把当前坐标付给lx
		ly = ev.getY();
		fx = ev.getX();
		fy = ev.getY();
	}

	private void onMouseMove(MotionEvent ev) {
		float x = ev.getX();
		mx = x - lx; // 当前鼠标位置 - 上次鼠标的位置
		lx = x; // 把当前鼠标的位置付给lx 以备下次使用
		float y = ev.getY();
		my = y - ly;
		ly = y;
		if (mx != 0 || my != 0)
			sendMouseEvent("mouse", mx * sensitivity, my * sensitivity);

	}

	private void onMouseUp(MotionEvent ev) {
		if (fx == ev.getX() && fy == ev.getY()) {
			sendMessage("leftButton:click");
		}

	}

	private void sendMouseEvent(String type, float x, float y) {
		String str = type + ":" + x + "," + y;
		sendMessage(str);
	}

	private void sendMessage(String str) {
		try {
			// 创建一个InetAddree
			InetAddress serverAddress = InetAddress.getByName(Data.IP);
			byte data[] = str.getBytes();
			packet = new DatagramPacket(data, data.length, serverAddress,
					Data.port);
			//唤醒发送线程
			synchronized (this) {
				notify();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		sendMessage("rightButton:click");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (key_flag == false) {
				toast = Toast.makeText(this, "再按一次 退出程序", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				key_flag = true;
				return false;
			} else {
				finish();
				toast.cancel();
				return super.onKeyDown(keyCode, event);
			}

		case KeyEvent.KEYCODE_MENU:
			Intent intent = new Intent(this, KeyControlActivity.class);
			startActivity(intent);
			// 这个函数有两个参数，一个参数是要start的activity进入时的动画，第二个参数则是当前activity画退出时的动画。
			// 注意
			// 1、必须在 StartActivity() 或 finish() 之后立即调用。
			// 2、而且在 2.1 以上版本有效
			// 3、手机设置-显示-动画，要开启状态
			overridePendingTransition(R.anim.in, android.R.anim.fade_out);

			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		// TODO Auto-generated method stub
		switch (arg0.getCheckedRadioButtonId()) {
		case R.id.sensitivity1:
			sensitivity = 1;
			break;
		case R.id.sensitivity2:
			sensitivity = 2;
			break;
		}
	}
}
