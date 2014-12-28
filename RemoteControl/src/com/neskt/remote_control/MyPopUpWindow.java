package com.neskt.remote_control;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

public class MyPopUpWindow {
	private View contentView;
	private PopupWindow popupWindow;
	private int height;
	private Context mContext;
	
	public MyPopUpWindow(int height, Context mContext) {
		// TODO Auto-generated constructor stub
		this.height = height;
		this.mContext = mContext;
	}
	
	public PopupWindow getPopupWindow() {

		// 一个自定义的布局，作为显示的内容
		contentView = LayoutInflater.from(mContext).inflate(
				R.layout.pop_window, null);

		popupWindow = new PopupWindow(contentView, LayoutParams.MATCH_PARENT,
				height / 3, true);
		if (height == 1920) {
			popupWindow.setAnimationStyle(R.style.popwin_anim_style);
		} else if (height == 1280) {
			popupWindow.setAnimationStyle(R.style.popwin_anim_style_1280);
		} else {
			popupWindow.setAnimationStyle(R.style.popwin_anim_style);
		}
		popupWindow.setTouchable(true);
		popupWindow
				.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		popupWindow.setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				return false;
				// 这里如果返回true的话，touch事件将被拦截
				// 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
			}
		});
		popupWindow.setBackgroundDrawable(new ColorDrawable());
		return popupWindow;
	}
	
	public View getContentView(){
		return contentView;
	}
}
