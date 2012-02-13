// Empty project additions:
//		Added "AutoIt3.h" include
//		Added "AutoItX3.lib" to the input linker libraries
//
// AutoItX3.dll needs to be in the run path during execution

#include <Windows.h>
#include "AutoIt3.h"

int APIENTRY WinMain(HINSTANCE hInstance,
                     HINSTANCE hPrevInstance,
                     LPSTR     lpCmdLine,
                     int       nCmdShow)
{
 	// You can now call AutoIt commands, e.g. to send the keystrokes "hello"
	AU3_Sleep(1000);
	AU3_Run("notepad.exe", "", 1);
	AU3_WinWaitActive("Untitled -", "", 0);
	AU3_Send("Hello{!}", 0);

	// Get the text in the status bar
	//char szText[1000];
	//AU3_StatusbarGetText("Untitled -", "", 2, szText, 1000);
	//MessageBox(NULL, szText, "Text:", MB_OK);

	return 0;
}
