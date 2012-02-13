#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <WindowsConstants.au3>

Example()

Func Example()
	Local $hGUI = GUICreate("String height & width.")
	GUISetState(@SW_SHOW, $hGUI)

	Local $sText = "This is  some text" ; The text we want to find the height & width of.
	Local $aStringDimension = GetStringDimensions($hGUI, $sText) ; Retrieve a 1 dimensional array with $aArray[0] = width & $aArray[1] = height.
	MsgBox(0, "String Height and Width", 'The height and width of the string "' & $sText & '" is:' & @CRLF & _
			"Width: " & $aStringDimension[0] & @CRLF & _
			"Height: " & $aStringDimension[1] & @CRLF)
	Do
		Sleep(50)
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>Example

Func GetStringDimensions($hWnd, $sText)
	Local $hDC = _WinAPI_GetDC($hWnd) ; Get the device context handle of the current window.
	Local $hFont = _SendMessage($hWnd, $WM_GETFONT) ; Retrieve the font with which the control is currently drawing its text.
	Local $hSelectObject = _WinAPI_SelectObject($hDC, $hFont) ; Select the object of the context device.
	Local $tSIZE = _WinAPI_GetTextExtentPoint32($hDC, $sText) ; Retrieve the height & width of a string.

	_WinAPI_SelectObject($hDC, $hSelectObject)
	_WinAPI_ReleaseDC($hWnd, $hDC) ; Release the device context.
	Local $aReturn[2] = [DllStructGetData($tSIZE, 1), DllStructGetData($tSIZE, 2)] ; Set an array with the width & height of the string.
	Return $aReturn
EndFunc   ;==>GetStringDimensions