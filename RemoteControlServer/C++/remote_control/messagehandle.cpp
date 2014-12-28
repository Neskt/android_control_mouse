#include "stdafx.h"
#include <iostream>

#include <windows.h>
 
#include "messagehandle.h"
#pragma comment( lib, "user32.lib")


MessageHandle::MessageHandle():split(","){}

void MessageHandle::mouse_move(char* message){
	message = strtok(NULL,split);
	mx = atoi(message);
	message = strtok(NULL,split);
	my = atoi(message);

	mouse_event(MOUSEEVENTF_MOVE,mx,my,0,0);
}

void MessageHandle::mouse_leftButton(char* message){
	message = strtok(NULL,split);
	
	if(strcmp(message,"down")==0)
		mouse_event(MOUSEEVENTF_LEFTDOWN,0,0,0,0);
	else if (strcmp(message,"up")==0)
		mouse_event(MOUSEEVENTF_LEFTUP,0,0,0,0);
	else{
		mouse_event(MOUSEEVENTF_LEFTDOWN,0,0,0,0);
		mouse_event(MOUSEEVENTF_LEFTUP,0,0,0,0);
	}
	
}

void MessageHandle::mouse_rightButton(){
	mouse_event(MOUSEEVENTF_RIGHTDOWN,0,0,0,0);
	mouse_event(MOUSEEVENTF_RIGHTUP,0,0,0,0);
}

void MessageHandle::mouse_wheel(char* message){
	message = strtok(NULL,split);
	middle = atoi(message);
	if(middle>0)
		mouse_event(MOUSEEVENTF_WHEEL,0,0,-120,0);
	else
		mouse_event(MOUSEEVENTF_WHEEL,0,0,120,0);
}


void MessageHandle::keyboard(char* message){
	message = strtok(NULL,split);

	if(strcmp(message,"Space")==0){
		keybd_event(VK_SPACE,0,0,0);
	    keybd_event(VK_SPACE,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"Enter")==0){
		keybd_event(VK_RETURN,0,0,0);
	    keybd_event(VK_RETURN,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"Up")==0){
		keybd_event(VK_UP,0,0,0);
	    keybd_event(VK_UP,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"Down")==0){
		keybd_event(VK_DOWN,0,0,0);
	    keybd_event(VK_DOWN,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"Left")==0){
		keybd_event(VK_LEFT,0,0,0);
	    keybd_event(VK_LEFT,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"Right")==0){
		keybd_event(VK_RIGHT,0,0,0);
	    keybd_event(VK_RIGHT,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"BackSpace")==0){
		keybd_event(VK_BACK,0,0,0);
	    keybd_event(VK_BACK,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"Back")==0){
		keybd_event(VK_MENU,0,0,0);
		keybd_event(VK_LEFT,0,0,0);
		keybd_event(VK_LEFT,0,KEYEVENTF_KEYUP,0);
	    keybd_event(VK_MENU,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"Forward")==0){
		keybd_event(VK_MENU,0,0,0);
		keybd_event(VK_RIGHT,0,0,0);
		keybd_event(VK_RIGHT,0,KEYEVENTF_KEYUP,0);
	    keybd_event(VK_MENU,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"F5")==0){
		keybd_event(VK_F5,0,0,0);
	    keybd_event(VK_F5,0,KEYEVENTF_KEYUP,0);
	}else if (strcmp(message,"ESC")==0){
		keybd_event(VK_ESCAPE,0,0,0);
	    keybd_event(VK_ESCAPE,0,KEYEVENTF_KEYUP,0);
	}

}

void MessageHandle::input_text(char* message){
	message = strtok(NULL,split);
	UTF8ToGBK(message);

	keybd_event(VK_LCONTROL,0,0,0);
	keybd_event('V',0,0,0);
	keybd_event('V',0,KEYEVENTF_KEYUP,0);
	keybd_event(VK_LCONTROL,0,KEYEVENTF_KEYUP,0);
	
	
}

bool MessageHandle::CopyToClipboard(const char* pszData, const int nDataLen)
{
    if(OpenClipboard(NULL))
    {
        EmptyClipboard();
        HGLOBAL clipbuffer;
        char *buffer;
        clipbuffer = ::GlobalAlloc(GMEM_DDESHARE, nDataLen+1);
        buffer = (char *)::GlobalLock(clipbuffer);
        strcpy(buffer, pszData);
        GlobalUnlock(clipbuffer);
        SetClipboardData(CF_TEXT, clipbuffer);
        CloseClipboard();
        return TRUE;
    }
    return FALSE;
}

void MessageHandle::UTF8ToGBK(char*message){
	char   i;

	i=   MultiByteToWideChar(CP_UTF8,0,message,-1,NULL,0);      //自己去查查这个函数用法
	WCHAR   *strUnicode=new   WCHAR[i];       
	MultiByteToWideChar   (CP_UTF8,0,message,-1,strUnicode,i);   

	i=   WideCharToMultiByte(CP_ACP,0,strUnicode,-1,NULL,0,NULL,NULL);       
	char   *strGBK   =   new   char[i];       
	WideCharToMultiByte   (CP_ACP,0,strUnicode,-1,strGBK,i,NULL,NULL);

	CopyToClipboard(strGBK,strlen(strGBK));
	delete []strUnicode;
	delete []strGBK;
}