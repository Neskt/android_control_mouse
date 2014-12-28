// mouse_test.cpp : ����Ӧ�ó������ڵ㡣
//

#include "stdafx.h"
#include "remote_control.h"
#include "init.h"
#include "messagehandle.h"
#include <windows.h>

#define IDR_PAUSE 12

#define IDR_START 13


#include <shellapi.h>

#pragma   comment(lib,   "shell32.lib")


LPCTSTR szAppName = TEXT("Զ�̿���");

LPCTSTR szWndName = TEXT("Զ�̿���");

HMENU hmenu;//�˵����

HINSTANCE hInstance2;

MessageHandle *mh = 0;

LRESULT CALLBACK WndProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam)

{

    NOTIFYICONDATA nid;//�������ͼ��
	NOTIFYICONDATA nid2;//����ɾ��ͼ��

    UINT WM_TASKBARCREATED;

    POINT pt;//���ڽ����������

    int xx;//���ڽ��ղ˵�ѡ���ֵ

 

    // ��Ҫ�޸�TaskbarCreated������ϵͳ�������Զ������Ϣ

    WM_TASKBARCREATED = RegisterWindowMessage(TEXT("TaskbarCreated"));

    switch (message)

    {

    case WM_CREATE://���ڴ���ʱ�����Ϣ.

        nid.cbSize = sizeof(nid);

        nid.hWnd = hwnd;

        nid.uID = 100;

        nid.uFlags = NIF_ICON | NIF_MESSAGE | NIF_TIP | NIF_INFO;

        nid.uCallbackMessage = WM_USER;
		
       // nid.hIcon = (HICON)LoadImage(NULL,L"aaa.ico",IMAGE_ICON,0,0,LR_LOADFROMFILE);
		nid.hIcon =  LoadIcon(hInstance2, MAKEINTRESOURCE(IDI_ICON));
        lstrcpy(nid.szTip, szAppName);
		nid.dwInfoFlags = NIIF_INFO;
		lstrcpy(nid.szInfo, L"����������,��APP����Զ������");  //֪ͨ������  
	    lstrcpy(nid.szInfoTitle, L"��ʾ"); //���ݵı���  
		nid.uTimeout = 1000;             //֪ͨ������ʱ�䣬msΪ��λ 

        Shell_NotifyIcon(NIM_ADD, &nid);

        hmenu=CreatePopupMenu();//���ɲ˵�
		
        AppendMenu(hmenu,MF_STRING,IDR_PAUSE,TEXT("�˳�"));//Ϊ�˵��������ѡ��

        break;

    case WM_USER://����ʹ�øó���ʱ�����Ϣ.

        if (lParam == WM_RBUTTONDOWN)

        {

            GetCursorPos(&pt);//ȡ�������

            ::SetForegroundWindow(hwnd);//����ڲ˵��ⵥ������˵�����ʧ������

            //EnableMenuItem(hmenu,IDR_PAUSE,MF_GRAYED);//�ò˵��е�ĳһ����

            xx=TrackPopupMenu(hmenu,TPM_RETURNCMD,pt.x,pt.y,NULL,hwnd,NULL);//��ʾ�˵�����ȡѡ��ID

            if(xx==IDR_PAUSE) 
				SendMessage(hwnd, WM_CLOSE, wParam, lParam);

            

            //if(xx==0) PostMessage(hwnd,WM_LBUTTONDOWN,NULL,NULL);

            //MessageBox(hwnd, TEXT("�Ҽ�"), szAppName, MB_OK);

        }

        break;

    case WM_DESTROY://��������ʱ�����Ϣ.

		if(mh!=NULL){
		  delete mh;
		}

		nid2.hWnd = hwnd;
        nid2.uID = 100;
		nid2.uFlags = NIF_ICON;

        Shell_NotifyIcon(NIM_DELETE, &nid2);//һ��Ҫ��һ��NOTIFYICONDATA��ɾ�������е�ͼ��

        PostQuitMessage(0);
        break;

    default:

        /*

       * ��ֹ��Explorer.exe �����Ժ󣬳�����ϵͳϵͳ�����е�ͼ�����ʧ

        *

        * ԭ��Explorer.exe �����������ؽ�ϵͳ����������ϵͳ������������ʱ�����ϵͳ������

        * ע�����TaskbarCreated ��Ϣ�Ķ������ڷ���һ����Ϣ������ֻ��Ҫ��׽�����Ϣ�����ؽ�ϵ

        * ͳ���̵�ͼ�꼴�ɡ�

        */

        if (message == WM_TASKBARCREATED)

            SendMessage(hwnd, WM_CREATE, wParam, lParam);

        break;

    }

    return DefWindowProc(hwnd, message, wParam, lParam);

}

 

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance,

                   LPSTR szCmdLine, int iCmdShow)

{
	
    HWND hwnd;

    MSG msg;

    WNDCLASS wndclass;

	hInstance2= hInstance;

    HWND handle = FindWindow(NULL, szWndName);

    if (handle != NULL)

    {

        MessageBox(NULL, TEXT("Application is already running"), szAppName, MB_ICONERROR);

        return 0;

    }

 

    wndclass.style = CS_HREDRAW | CS_VREDRAW;

    wndclass.lpfnWndProc = WndProc;

    wndclass.cbClsExtra = 0;

    wndclass.cbWndExtra = 0;

    wndclass.hInstance = hInstance;

    wndclass.hIcon = LoadIcon(NULL, IDI_APPLICATION);

    wndclass.hCursor = LoadCursor(NULL, IDC_ARROW);

    wndclass.hbrBackground = (HBRUSH)GetStockObject(WHITE_BRUSH);

    wndclass.lpszMenuName = NULL;

    wndclass.lpszClassName = szAppName;

 

    if (!RegisterClass(&wndclass))

    {

        MessageBox(NULL, TEXT("This program requires Windows NT!"), szAppName, MB_ICONERROR);

        return 0;

    }

 

    // �˴�ʹ��WS_EX_TOOLWINDOW ������������ʾ���������ϵĴ��ڳ���ť

    hwnd = CreateWindowEx(WS_EX_TOOLWINDOW,

        szAppName, szWndName,

        WS_POPUP,

        CW_USEDEFAULT,

        CW_USEDEFAULT,

        CW_USEDEFAULT,

        CW_USEDEFAULT,

        NULL, NULL, hInstance, NULL);


    ShowWindow(hwnd, iCmdShow);

    UpdateWindow(hwnd);

	initialize();

    while (GetMessage(&msg, NULL, 0, 0))

    {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    return msg.wParam;
}
