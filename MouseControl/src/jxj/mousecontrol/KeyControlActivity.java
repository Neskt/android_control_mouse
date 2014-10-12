package jxj.mousecontrol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class KeyControlActivity extends Activity implements OnClickListener {
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
	private Button ctrl_w;
	private EditText et_keyoboard;
	private DatagramSocket socket;
	private DatagramPacket packet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.keyboard);
		initialize();
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread() {
			public void run() {
				while (true) {
					try {
						synchronized (KeyControlActivity.this) {
							KeyControlActivity.this.wait();
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

	private void initialize() {
		// TODO Auto-generated method stub
		et_keyoboard = (EditText) findViewById(R.id.et_keyoboard);
		send = (Button) findViewById(R.id.bt_send);
		space = (Button) findViewById(R.id.bt_space);
		enter = (Button) findViewById(R.id.bt_enter);
		backspace = (Button) findViewById(R.id.bt_backspace);
		back = (Button) findViewById(R.id.bt_back);
		forward = (Button) findViewById(R.id.bt_forward);
		up = (Button) findViewById(R.id.arrow_up);
		down = (Button) findViewById(R.id.arrow_down);
		left = (Button) findViewById(R.id.arrow_left);
		right = (Button) findViewById(R.id.arrow_right);
		f5 = (Button) findViewById(R.id.bt_f5);
		esc = (Button) findViewById(R.id.bt_esc);
		ctrl_w = (Button) findViewById(R.id.bt_ctrl_w);
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
		ctrl_w.setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(android.R.anim.fade_in, R.anim.out);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_send:
			String s = et_keyoboard.getText().toString();
			if (s.isEmpty()) {
				Toast.makeText(this, "信息为空", Toast.LENGTH_SHORT).show();
				return;
			}
			sendMessage("keyboard:message," + s);
			break;
		case R.id.bt_space:
			sendMessage("keyboard:key,Space");
			break;
		case R.id.bt_enter:
			sendMessage("keyboard:key,Enter");
			break;
		case R.id.bt_backspace:
			sendMessage("keyboard:key,BackSpace");
			break;
		case R.id.bt_back:
			sendMessage("keyboard:key,Back");
			break;
		case R.id.bt_forward:
			sendMessage("keyboard:key,Forward");
			break;
		case R.id.arrow_up:
			sendMessage("keyboard:key,Up");
			break;
		case R.id.arrow_down:
			sendMessage("keyboard:key,Down");
			break;
		case R.id.arrow_left:
			sendMessage("keyboard:key,Left");
			break;
		case R.id.arrow_right:
			sendMessage("keyboard:key,Right");
			break;
		case R.id.bt_f5:
			sendMessage("keyboard:key,F5");
			break;
		case R.id.bt_esc:
			sendMessage("keyboard:key,ESC");
			break;
		case R.id.bt_ctrl_w:
			sendMessage("keyboard:key,Ctrl+W");
			break;
		}
	}

	private void sendMessage(final String str) {
		try {
			// 创建一个InetAddree
			InetAddress serverAddress = InetAddress.getByName(Data.IP);
			byte data[] = str.getBytes();
			packet = new DatagramPacket(data, data.length, serverAddress,
					Data.port);
			synchronized (this) {
				notify();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
