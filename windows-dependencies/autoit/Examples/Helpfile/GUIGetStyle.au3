#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

Example()

Func Example()
	Local $NewStyle = False, $hWnd, $Style, $GuiStyles, $Msg

	$hWnd = GUICreate("Gui Style", 260, 100)
	$Style = GUICtrlCreateButton("Set Style", 45, 50, 150, 20)

	$GuiStyles = GUIGetStyle($hWnd) ; be careful the style change after opening

	GUISetState()


	While 1
		$Msg = GUIGetMsg()
		Switch $Msg
			Case $GUI_EVENT_CLOSE
				Exit
			Case $Style
				If Not $NewStyle Then
					GUISetStyle(BitOR($WS_POPUPWINDOW, $WS_THICKFRAME), BitOR($WS_EX_CLIENTEDGE, $WS_EX_TOOLWINDOW))
					GUICtrlSetData($Style, 'Undo Style')
					$NewStyle = True
				Else
					GUISetStyle($GuiStyles[0], $GuiStyles[1])
					GUICtrlSetData($Style, 'Set Style')
					$NewStyle = False
				EndIf
			Case Else
		EndSwitch
	WEnd
EndFunc   ;==>Example
