#include <WinAPI.au3>
#include <Constants.au3>
#include <WindowsConstants.au3>
#include <GUIConstants.au3>

Example()

Func Example()
	; Enable GUI event mode.
	Opt("GUIOnEventMode", 1)

	; Create a simple GUI.
	Local $hWnd = GUICreate("DllCallAddress Example")

	; Register the close event handler.
	GUISetOnEvent($GUI_EVENT_CLOSE, "OnClose")

	; Show the GUI.
	GUISetState(@SW_SHOWNORMAL, $hWnd)

	; Get a pointer to the window's WindowProc().
	Local $pWndProc = _WinAPI_GetWindowLong($hWnd, $GWL_WNDPROC)


	; Tell the user what is about to happen.
	MsgBox(4096, "DllCallAddress Example Msg", "When you press OK the test window will close.")

	; Explicitly generate a WM_CLOSE event and pass it directly to the WindowProc().
	; This should never be done in a real application (Use _SendMessage() instead) but
	; it demonstrates how to use the function.
	DllCallAddress("LRESULT", $pWndProc, "HWND", $hWnd, "UINT", $WM_CLOSE, "WPARAM", 0, "LPARAM", 0)
EndFunc   ;==>Example

Func OnClose()
	GUIDelete(@GUI_WinHandle)
	MsgBox(4096, "DllCallAddress Example Msg", "Close event received, the test window should now be closed.")
EndFunc   ;==>OnClose
