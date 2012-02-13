#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Example()

; Simple example: Embedding an Internet Explorer Object inside an AutoIt GUI
;
; See also: http://msdn.microsoft.com/workshop/browser/webbrowser/reference/objects/internetexplorer.asp
Func Example()
	Local $oIE, $GUI_Button_Back, $GUI_Button_Forward
	Local $GUI_Button_Home, $GUI_Button_Stop, $msg

	$oIE = ObjCreate("Shell.Explorer.2")

	; Create a simple GUI for our output
	GUICreate("Embedded Web control Test", 640, 580, (@DesktopWidth - 640) / 2, (@DesktopHeight - 580) / 2, BitOR($WS_OVERLAPPEDWINDOW, $WS_CLIPSIBLINGS, $WS_CLIPCHILDREN))
	GUICtrlCreateObj($oIE, 10, 40, 600, 360)
	$GUI_Button_Back = GUICtrlCreateButton("Back", 10, 420, 100, 30)
	$GUI_Button_Forward = GUICtrlCreateButton("Forward", 120, 420, 100, 30)
	$GUI_Button_Home = GUICtrlCreateButton("Home", 230, 420, 100, 30)
	$GUI_Button_Stop = GUICtrlCreateButton("Stop", 330, 420, 100, 30)

	GUISetState() ;Show GUI

	$oIE.navigate("http://www.autoitscript.com")

	; Waiting for user to close the window
	While 1
		$msg = GUIGetMsg()

		Select
			Case $msg = $GUI_EVENT_CLOSE
				ExitLoop
			Case $msg = $GUI_Button_Home
				$oIE.navigate("http://www.autoitscript.com")
			Case $msg = $GUI_Button_Back
				$oIE.GoBack
			Case $msg = $GUI_Button_Forward
				$oIE.GoForward
			Case $msg = $GUI_Button_Stop
				$oIE.Stop
		EndSelect

	WEnd

	GUIDelete()
EndFunc   ;==>Example
