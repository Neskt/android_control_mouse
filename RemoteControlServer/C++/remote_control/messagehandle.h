#ifndef MESSAGE_HANDLE_H_
#define MESSAGE_HANDLE_H_

class MessageHandle{
public:
	
	MessageHandle();
	void mouse_move(char* message);
	void mouse_leftButton(char* message);
	void mouse_rightButton();
	void mouse_wheel(char* message);
	void keyboard(char* message);
	void input_text(char* message);
	bool CopyToClipboard(const char* pszData, const int nDataLen);
	void UTF8ToGBK(char*message);
private:
	const char * split;
	int mx;
	int my;
	int middle;
};

#endif