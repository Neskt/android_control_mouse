package com.neskt.remote_control;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MouseControlActivity extends Activity implements OnClickListener {
	private VelocityTracker vTracker = null;
	private DatagramSocket socket;
	private DatagramPacket packet;
	private Button bt_left;
	private Button bt_right;
	private boolean up_flag; // �ڶ�����ָ̧���flag
	private Toast toast;
	
	public static float mx = 0; // ���͵�����ƶ��Ĳ�ֵ
	public static float my = 0;
	public static float my2 = 0;
	public static float lx; // ��¼�ϴε�λ��
	public static float ly;
	public static float ly2; // ��¼�ڶ�����ָ�ϴε�λ��
	public static float fx; // ��ָ��һ�νӴ���Ļʱ������
	public static float fy;
	public static float vy1; // ��ָ�ٶ�
	public static float vy2; // �ٶȵĺ�
	
	private InetAddress serverAddress;
	private Boolean network_flag;
	private WifiManager wm;
	private PopupWindow popupWindow;
	private View contentView;
	
	private View view1, view2;// ��Ҫ������ҳ��
	private ViewPager viewPager;// viewpager
	private List<View> viewList;// ����Ҫ������ҳ����ӵ����list��
	private List<String> titleList;// viewpager�ı���
	PageIndicator mIndicator;
	
	private Context mContext = null;
	private RelativeLayout rel;
	
	private KeyControl keyControl;
	
	private Thread t;
	
	private byte data[];
	
	private long exitTime = 0;
	
	private Handler mHandler;
	private Handler handle = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == Constants.SUCCESS)
				Toast.makeText(MouseControlActivity.this,
						"��ʼ����ɣ�˫ָ���»���Ϊ����Ӵ~", Toast.LENGTH_SHORT).show();
			if(msg.what == Constants.FAILURE)
				Toast.makeText(MouseControlActivity.this,
						"��ʼ��ʧ��", Toast.LENGTH_SHORT).show();
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int height = dm.heightPixels;

		mContext = this;
		
		
		MyPopUpWindow myPopUpWindow = new MyPopUpWindow(height,this);
		popupWindow = myPopUpWindow.getPopupWindow();
		
		rel = (RelativeLayout) findViewById(R.id.rel);
		ImageButton button = (ImageButton) findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View view) {
				popupWindow.showAtLocation(rel, Gravity.BOTTOM, 0, 0);
			}
		});
		
		contentView = myPopUpWindow.getContentView();
		initViewPager();
		UnderlinePageIndicator indicator = (UnderlinePageIndicator) contentView
				.findViewById(R.id.indicator);
		indicator.setViewPager(viewPager);
		indicator.setFades(false);
		mIndicator = indicator;

		getButton();
		
		wm = (WifiManager) getSystemService(WIFI_SERVICE);
		if (!wm.isWifiEnabled()) {
			Toast.makeText(this, "WIFIδ���������WIFI�����¿���APP", Toast.LENGTH_SHORT)
					.show();
		} else {

			Toast.makeText(this, "��ʼ����...", Toast.LENGTH_SHORT).show();
			
			network_flag = true;
			init_network();

			try {
				socket = new DatagramSocket();// ����ҪInternetȨ��
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			initTouch();
			
			keyControl=new KeyControl();
			keyControl.initialize(mContext,view1,view2);
			
			t = new Thread() {
				public void run() {
					Looper.prepare();//1����ʼ��Looper
		            mHandler = new Handler(){//2����handler��CustomThreadʵ����Looper����
		                public void handleMessage (Message msg) {//3�����崦����Ϣ�ķ���
		                    switch(msg.what) {
		                    case Constants.MSG_MOUSE:
		                    	try {
									socket.send(packet);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		                    }
		                }
		            };
		            Looper.loop();//4��������Ϣѭ��
				};
			};
			t.setPriority(10);
			t.start();
			
		}

	}

	private void getButton() {
		// TODO Auto-generated method stub
		bt_left = (Button) findViewById(R.id.bt_mouse_left);
		bt_right = (Button) findViewById(R.id.bt_mouse_right);
		bt_right.setOnClickListener(this);

		bt_left.setOnTouchListener(new OnTouchListener() {
			// ��ôд��Ϊ�����֧���϶�
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// ������д�㰴��Ҫ��������飬����Ҫ����true���ܴ��������¼�
					bt_left.setBackgroundResource(R.drawable.cb_pressed_right);
					sendMessage("leftButton:down\0");
					return true;

				case MotionEvent.ACTION_UP:
					// ����д�ϰ�ť����ʱ��������飬����Ҫ����true���ܴ��������¼�
					bt_left.setBackgroundResource(R.drawable.cb_right);
					sendMessage("leftButton:up\0");
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
				// ��arg1.getAction()��
				// MotionEvent.ACTION_MASK����������������ӦACTION_POINTER_DOWN��ACTION_POINTER_UP
				switch (arg1.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					onMouseDown(arg1);
					break;
				case MotionEvent.ACTION_MOVE:
					if (arg1.getPointerCount() == 1) {
						// ˫ָ������Ϊ���ڵڶ�����ָ̧��ʱ���λ�ò�����������
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
		if ((my > 0 && my2 > 0 && vy2 > 2000)
				|| (my < 0 && my2 < 0 && vy2 < -2000)) {
			String str = "mousewheel" + ":" + my + "\0";
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
			sendMouseEvent("mouse", mx, my);

	}

	private void onMouseUp(MotionEvent ev) {
		if (fx == ev.getX() && fy == ev.getY()) {
			sendMessage("leftButton:click\0");
		}

	}

	private void sendMouseEvent(String type, float x, float y) {
		String str = type + ":" + x + "," + y + "\0";
		sendMessage(str);
	}

	private void sendMessage(String str) {
		try {
			data = str.getBytes();
			packet = new DatagramPacket(data, data.length, serverAddress,
					Constants.PORT);
			mHandler.obtainMessage(Constants.MSG_MOUSE, null).sendToTarget();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		sendMessage("rightButton:click\0");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((System.currentTimeMillis()-exitTime) > 2000) {
				toast = Toast.makeText(this, "�ٰ�һ�� �˳�����", Toast.LENGTH_SHORT);
				toast.show();
				exitTime = System.currentTimeMillis();
				return false;
			} else {
				finish();
				toast.cancel();
				return super.onKeyDown(keyCode, event);
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	private void init_network() {

		WifiInfo wifiInfo = wm.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));

		DhcpInfo di = wm.getDhcpInfo();
		long netmaskIpL = di.netmask;
		String netmaskIpS = long2ip(netmaskIpL);// ���������ַ
		//System.out.println(netmaskIpS);

		Constants.BroadcastIP = getIpBroadcast(ip, netmaskIpS);

		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					DatagramSocket socket = new DatagramSocket();

					String str = "Neskt10121411\0";
					byte data2[] = str.getBytes();
					// ����һ���յ�DatagramPacket����
					DatagramPacket packet2 = new DatagramPacket(data2,
							data2.length,
							InetAddress.getByName(Constants.BroadcastIP), 31895);
					while (network_flag) {
						socket.send(packet2);
						Thread.sleep(500);
					}
					socket.close();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

		new Thread() {
			public void run() {
				try {
					DatagramSocket socket2 = new DatagramSocket(31896);
					byte data[] = new byte[1024];
					// ����һ���յ�DatagramPacket����
					DatagramPacket packet = new DatagramPacket(data,
							data.length);
					socket2.receive(packet);
					String message = new String(packet.getData(), 0,
							packet.getLength(), "UTF-8");
					
					if (message.startsWith("Neskt10121411")){
						network_flag = false;
						socket2.close();
						Constants.IP = packet.getAddress().getHostAddress();
						serverAddress = InetAddress.getByName(Constants.IP);
						Message msg = new Message();
						msg.what=Constants.SUCCESS;
						handle.sendMessage(msg);
					}else{
						Message msg = new Message();
						msg.what=Constants.FAILURE;
						handle.sendMessage(msg);
					}
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();

	}

	String long2ip(long ip) {
		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf((int) (ip & 0xff)));
		sb.append('.');
		sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
		sb.append('.');
		sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
		sb.append('.');
		sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
		return sb.toString();
	}

	String getIpBroadcast(String ip, String netmask) {
		String ip_array[] = ip.split("\\.");
		String netmask_array[] = netmask.split("\\.");
		StringBuilder ip_broad = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			if ("255".equals(netmask_array[i])) {
				ip_broad.append(ip_array[i]);
				if (i != 3)
					ip_broad.append('.');
			} else {
				int temp = 255 - Integer.parseInt(netmask_array[i]);
				temp = temp | Integer.parseInt(ip_array[i]);
				ip_broad.append(temp);
				if (i != 3) {
					ip_broad.append('.');
				}
			}
		}
		return ip_broad.toString();
	}

public void initViewPager() {
		
		viewPager = (ViewPager) contentView.findViewById(R.id.viewpager);
		view1 = findViewById(R.layout.layout1);
		view2 = findViewById(R.layout.layout2);

		@SuppressWarnings("static-access")
		LayoutInflater lf = getLayoutInflater().from(this);
		view1 = lf.inflate(R.layout.layout1, null);
		view2 = lf.inflate(R.layout.layout2, null);

		viewList = new ArrayList<View>();// ��Ҫ��ҳ��ʾ��Viewװ��������
		viewList.add(view1);
		viewList.add(view2);

		titleList = new ArrayList<String>();// ÿ��ҳ���Title����
		titleList.add("function");
		titleList.add("send");

		PagerAdapter pagerAdapter = new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {

				return arg0 == arg1;
			}

			@Override
			public int getCount() {

				return viewList.size();
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeView(viewList.get(position));

			}

			@Override
			public int getItemPosition(Object object) {

				return super.getItemPosition(object);
			}

			@Override
			public CharSequence getPageTitle(int position) {

				return titleList.get(position);// ֱ��������������ɱ������ʾ�����Դ�������Կ���������û��ʹ��PagerTitleStrip����Ȼ�����ʹ�á�

			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(viewList.get(position));
				return viewList.get(position);
			}

		};

		viewPager.setAdapter(pagerAdapter);
	}
	
}
