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
	private boolean up_flag; // �ڶ�����ָ̧���flag
	private Toast toast;
	private static int sensitivity = 1;
	private static float mx = 0; // ���͵�����ƶ��Ĳ�ֵ
	private static float my = 0;
	private static float my2 = 0;
	private static float lx; // ��¼�ϴε�λ��
	private static float ly;
	private static float ly2; // ��¼�ڶ�����ָ�ϴε�λ��
	private static float fx; // ��ָ��һ�νӴ���Ļʱ������
	private static float fy;
	private float vy1; // ��ָ�ٶ�
	private float vy2; //�ٶȵĺ�
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);
		try {
			socket = new DatagramSocket();// ����ҪInternetȨ��
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
						//���������ڲ��࣬��������MouseControlActivity.this��this�ǲ�ͬ��
						//����notify��this��Activity����������ҪдMouseControlActivity��this
						//wait��notify����Ҫ��synchronized��ס
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
			//��ôд��Ϊ�����֧���϶�
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// ������д�㰴��Ҫ��������飬����Ҫ����true���ܴ��������¼�
					bt_left.setBackgroundResource(R.drawable.button_pressed);
					sendMessage("leftButton:down");
					return true;

				case MotionEvent.ACTION_UP:
					// ����д�ϰ�ť����ʱ��������飬����Ҫ����true���ܴ��������¼�
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
				//��arg1.getAction()�� MotionEvent.ACTION_MASK����������������ӦACTION_POINTER_DOWN��ACTION_POINTER_UP
				switch (arg1.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					onMouseDown(arg1);
					break;
				case MotionEvent.ACTION_MOVE:
					if (arg1.getPointerCount() == 1) {
						//˫ָ������Ϊ���ڵڶ�����ָ̧��ʱ���λ�ò�����������
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
		lx = ev.getX(); // ���ֻ���һ����ʱ �ѵ�ǰ���긶��lx
		ly = ev.getY();
		fx = ev.getX();
		fy = ev.getY();
	}

	private void onMouseMove(MotionEvent ev) {
		float x = ev.getX();
		mx = x - lx; // ��ǰ���λ�� - �ϴ�����λ��
		lx = x; // �ѵ�ǰ����λ�ø���lx �Ա��´�ʹ��
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
			// ����һ��InetAddree
			InetAddress serverAddress = InetAddress.getByName(Data.IP);
			byte data[] = str.getBytes();
			packet = new DatagramPacket(data, data.length, serverAddress,
					Data.port);
			//���ѷ����߳�
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
				toast = Toast.makeText(this, "�ٰ�һ�� �˳�����", Toast.LENGTH_SHORT);
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
			// �������������������һ��������Ҫstart��activity����ʱ�Ķ������ڶ����������ǵ�ǰactivity���˳�ʱ�Ķ�����
			// ע��
			// 1�������� StartActivity() �� finish() ֮���������á�
			// 2�������� 2.1 ���ϰ汾��Ч
			// 3���ֻ�����-��ʾ-������Ҫ����״̬
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
