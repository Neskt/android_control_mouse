#include "stdafx.h"
#include "udp.h"
#include <windows.h>
#include <stdlib.h>
#include <process.h>

unsigned _stdcall ThreadProc1(void* param);
unsigned _stdcall ThreadProc2(void* param);

void initialize(){
  HANDLE handle1 = (HANDLE)_beginthreadex(NULL, 0, ThreadProc1, NULL, NULL, NULL);
  HANDLE handle2 = (HANDLE)_beginthreadex(NULL, 0, ThreadProc2, NULL, NULL, NULL);

  CloseHandle(handle1);
  CloseHandle(handle2);
}

unsigned _stdcall ThreadProc1(void* param)
{
 udp_receive();
 return 0;
}

unsigned _stdcall ThreadProc2(void* param)
{
 udp_send();
 return 0;
}