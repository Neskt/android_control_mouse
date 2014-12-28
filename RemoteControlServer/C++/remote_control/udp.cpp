#include "stdafx.h"
#include "messagehandle.h"
#include <stdio.h> 
#include <iostream>
#include <Winsock2.h> //windows socket的头文件  
#pragma comment( lib, "ws2_32.lib" )// 链接Winsock2.h的静态库文件  
using namespace std;


void udp_receive(){
	WORD wVersionRequested;  
    WSADATA wsaData;  
    int err;  
  
    wVersionRequested = MAKEWORD( 1, 1 );   
  
    err = WSAStartup( wVersionRequested, &wsaData );   
    if ( err != 0 ) {  
        return;  
    }  
  
    if ( LOBYTE( wsaData.wVersion ) != 1 ||  
        HIBYTE( wsaData.wVersion ) != 1 ) {   
            WSACleanup( );  
            return;  
    }  
	int len = sizeof(SOCKADDR);

	SOCKET sockRecv=socket(AF_INET,SOCK_DGRAM,IPPROTO_UDP);  
	SOCKADDR_IN local;  
    local.sin_addr.s_addr = htonl(INADDR_ANY);  
    local.sin_family=AF_INET;   
    local.sin_port=htons(31893);   
    
	if(bind(sockRecv, (SOCKADDR FAR *)&local, sizeof(local))!=0)
	{ 
		closesocket(sockRecv); 
		WSACleanup(); 
	    return ;
	}

	const char * split = ":"; 
    char * p;

	extern MessageHandle *mh;
	mh= new MessageHandle;

	char recvBuf[50];  
	while(true){
        recvfrom(sockRecv,recvBuf,50,0,(SOCKADDR*)&local,&len); 
		
		p = strtok (recvBuf,split);

		if (strcmp(p,"mouse")==0){
			mh->mouse_move(p);
		}else if(strcmp(p,"leftButton")==0) {
			mh->mouse_leftButton(p);
		}else if(strcmp(p,"rightButton")==0) {
			mh->mouse_rightButton();
		}else if(strcmp(p,"mousewheel")==0) {
		    mh->mouse_wheel(p);
		}else if(strcmp(p,"keyboard")==0) {
		    mh->keyboard(p);
		}else if(strcmp(p,"text")==0) {
		    mh->input_text(p);
		}
	}
	
	closesocket(sockRecv);  
    WSACleanup();
}


void udp_send(){
	
	WORD wVersionRequested;  
    WSADATA wsaData;  
    int err;  
  
    wVersionRequested = MAKEWORD( 1, 1 );   
  
    err = WSAStartup( wVersionRequested, &wsaData );   
    if ( err != 0 ) {  
        return;  
    }  
  
    if ( LOBYTE( wsaData.wVersion ) != 1 ||  
        HIBYTE( wsaData.wVersion ) != 1 ) {   
        WSACleanup( );  
        return;  
    }  


    SOCKET sockRecv=socket(AF_INET,SOCK_DGRAM,IPPROTO_UDP);  
  
    int len = sizeof(SOCKADDR);  
  
    SOCKADDR_IN local;  
        
	local.sin_addr.s_addr = htonl(INADDR_ANY);
    local.sin_family=AF_INET;   
    local.sin_port=htons(31895);

	if(bind(sockRecv, (SOCKADDR FAR *)&local, sizeof(local))!=0)
	{ 
		closesocket(sockRecv); 
		WSACleanup(); 
	    return ;
	}

	char recvBuf[50];
	while(true){
	  recvfrom(sockRecv,recvBuf,50,0,(SOCKADDR*)&local,&len);
	  if(strcmp(recvBuf,"Neskt10121411")==0){	    
	
	    local.sin_addr.s_addr = inet_addr(inet_ntoa(local.sin_addr));
	    local.sin_family=AF_INET;   
        local.sin_port=htons(31896);

        char sendBuf[14]="Neskt10121411";

	    sendto(sockRecv,sendBuf,strlen(sendBuf)+1,0,(SOCKADDR*)&local,len); 
	
	  }
	}
    closesocket(sockRecv);  
        
    WSACleanup();  
}