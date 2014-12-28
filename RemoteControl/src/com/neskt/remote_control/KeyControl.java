package com.neskt.remote_control;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class KeyControl implements OnClickListener {
	private Button send;
	private Button space;
	private Button enter;
	private Button backspace;
	private Button back;
	private Button forward;
	private Button up;
	private Button down;
	private Button left;
	private Button right;
	private Button f5;
	private Button esc;
	private EditText et_keyoboard;
	private DatagramSocket socket;
	private DatagramPacket packet;
	private Context mContext;
	private Handler mHandler;
	

	public void initialize(Context context, View layout1,View layout2) {
		// TODO Auto-generated method stub
		mContext = context;
		
		space = (Button) layout1.findViewById(R.id.bt_space);
		back = (Button) layout1.findViewById(R.id.bt_back);
		forward = (Button) layout1.findViewById(R.id.bt_forward);
		up = (Button) layout1.findViewById(R.id.arrow_up);
		down = (Button) layout1.findViewById(R.id.arrow_down);
		left = (Button) layout1.findViewById(R.id.arrow_left);
		right = (Button) layout1.findViewById(R.id.arrow_right);
		f5 = (Button) layout1.findViewById(R.id.bt_f5);
		esc = (Button) layout1.findViewById(R.id.bt_esc);
		
		send = (Button) layout2.findViewById(R.id.bt_send);
		enter = (Button) layout2.findViewById(R.id.bt_enter);
		backspace = (Button) layout2.findViewById(R.id.bt_backspace);
		et_keyoboard = (EditText) layout2.findViewById(R.id.et_keyoboard);
		
		send.setOnClickListener(this);
		space.setOnClickListener(this);
		enter.setOnClickListener(this);
		backspace.setOnClickListener(this);
		back.setOnClickListener(this);
		forward.setOnClickListener(this);
		up.setOnClickListener(this);
		down.setOnClickListener(this);
		left.setOnClickListener(this);
		right.setOnClickListener(this);
		f5.setOnClickListener(this);
		esc.setOnClickListener(this);
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Thread() {
			public void run() {
				Looper.prepare();
				mHandler = new Handler(){//2、绑定handler到CustomThread实例的Looper对象
	                public void handleMessage (Message msg) {//3、定义处理消息的方法
	                    switch(msg.what) {
	                    case Constants.KEY:
	                    	try {
								socket.send(packet);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                    }
	                }
	            };
	            Looper.loop();//4、启动消息循环
			};
		}.start();
	}

	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_send:
			String s = et_keyoboard.getText().toString();
			if (s.isEmpty()) {
				Toast.makeText(mContext, "信息为空", Toast.LENGTH_SHORT).show();
				return;
			}
			sendMessage("text:"+s+"\0");
			et_keyoboard.setText("");
			break;
		case R.id.bt_space:
			sendMessage("keyboard:Space\0");
			break;
		case R.id.bt_enter:
			sendMessage("keyboard:Enter\0");
			break;
		case R.id.bt_backspace:
			sendMessage("keyboard:BackSpace\0");
			break;
		case R.id.bt_back:
			sendMessage("keyboard:Back\0");
			break;
		case R.id.bt_forward:
			sendMessage("keyboard:Forward\0");
			break;
		case R.id.arrow_up:
			sendMessage("keyboard:Up\0");
			break;
		case R.id.arrow_down:
			sendMessage("keyboard:Down\0");
			break;
		case R.id.arrow_left:
			sendMessage("keyboard:Left\0");
			break;
		case R.id.arrow_right:
			sendMessage("keyboard:Right\0");
			break;
		case R.id.bt_f5:
			sendMessage("keyboard:F5\0");
			break;
		case R.id.bt_esc:
			sendMessage("keyboard:ESC\0");
			break;
		}
	}

	private void sendMessage(final String str) {
		try {
			InetAddress serverAddress = InetAddress.getByName(Constants.IP);
			byte[] data = str.getBytes();
			packet = new DatagramPacket(data, data.length, serverAddress,
					Constants.PORT);
			mHandler.obtainMessage(Constants.KEY, null).sendToTarget();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
