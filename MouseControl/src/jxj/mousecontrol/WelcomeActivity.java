package jxj.mousecontrol;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;



public class WelcomeActivity extends Activity {
	private EditText et_ip1;
	private EditText et_ip2;
	private EditText et_ip3;
	private EditText et_ip4;
	private EditText et_port;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        initialize();
    }


    private void getIP() {
		// TODO Auto-generated method stub
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(et_ip1.getText().toString().trim()+'.')
    	      .append(et_ip2.getText().toString().trim()+'.')
    	      .append(et_ip3.getText().toString().trim()+'.')
    	      .append(et_ip4.getText().toString().trim());
    	Data.IP=buffer.toString();
	}

    public void start(View v) {
		// TODO Auto-generated method stub
    	boolean ip_flag = checkIP();
    	if(ip_flag == false)
    		return;
    	boolean port_flag = checkPort();
    	if(port_flag == false)
    		return;
    	getIP();
    	Intent intent = new Intent(this, MouseControlActivity.class);
    	startActivity(intent);
    	overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);  
    	this.finish();
	}

	private boolean checkPort() {
		// TODO Auto-generated method stub
		String port = et_port.getText().toString().trim();
		if(port.isEmpty()){
			Toast.makeText(this, "端口不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		Data.port=Integer.parseInt(port);
		if(Data.port<0 || Data.port>65535){
			Toast.makeText(this, "请输入0~65535内的数字", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}


	private boolean checkIP() {
		// TODO Auto-generated method stub
		String ip1 = et_ip1.getText().toString().trim();
		String ip2 = et_ip2.getText().toString().trim();
		String ip3 = et_ip3.getText().toString().trim();
		String ip4 = et_ip4.getText().toString().trim();
		if(ip1.isEmpty()||ip2.isEmpty()||ip3.isEmpty()||ip4.isEmpty()){
			Toast.makeText(this, "IP不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}


	private void initialize() {
		// TODO Auto-generated method stub
		et_ip1 = (EditText) findViewById(R.id.et_ip1);
		et_ip2 = (EditText) findViewById(R.id.et_ip2);
		et_ip3 = (EditText) findViewById(R.id.et_ip3);
		et_ip4 = (EditText) findViewById(R.id.et_ip4);
		et_port = (EditText) findViewById(R.id.et_port);
	}


}
