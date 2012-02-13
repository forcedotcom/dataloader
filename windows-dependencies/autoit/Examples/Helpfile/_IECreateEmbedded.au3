; *******************************************************
; Example 1 - Trap COM errors so that 'Back' and 'Forward'
;				outside of history bounds does not abort script
;				(expect COM errors to be sent to the console)
; *******************************************************

#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <IE.au3>

_IEErrorHandlerRegister()

Local $oIE = _IECreateEmbedded()
GUICreate("Embedded Web control Test", 640, 580, _
		(@DesktopWidth - 640) / 2, (@DesktopHeight - 580) / 2, _
		$WS_OVERLAPPEDWINDOW + $WS_VISIBLE + $WS_CLIPSIBLINGS + $WS_CLIPCHILDREN)
GUICtrlCreateObj($oIE, 10, 40, 600, 360)
Local $GUI_Button_Back = GUICtrlCreateButton("Back", 10, 420, 100, 30)
Local $GUI_Button_Forward = GUICtrlCreateButton("Forward", 120, 420, 100, 30)
Local $GUI_Button_Home = GUICtrlCreateButton("Home", 230, 420, 100, 30)
Local $GUI_Button_Stop = GUICtrlCreateButton("Stop", 340, 420, 100, 30)

GUISetState() ;Show GUI

_IENavigate($oIE, "http://www.autoitscript.com")

; Waiting for user to close the window
While 1
	Local $msg = GUIGetMsg()
	Select
		Case $msg = $GUI_EVENT_CLOSE
			ExitLoop
		Case $msg = $GUI_Button_Home
			_IENavigate($oIE, "http://www.autoitscript.com")
		Case $msg = $GUI_Button_Back
			_IEAction($oIE, "back")
		Case $msg = $GUI_Button_Forward
			_IEAction($oIE, "forward")
		Case $msg = $GUI_Button_Stop
			_IEAction($oIE, "stop")
	EndSelect
WEnd

GUIDelete()

Exit
