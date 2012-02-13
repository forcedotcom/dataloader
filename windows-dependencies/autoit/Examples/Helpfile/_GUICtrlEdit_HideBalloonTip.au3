#include <GuiEdit.au3>
#include <GUIConstantsEx.au3>

$Debug_Ed = False ; Check ClassName being passed to Edit functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hGui, $hEdit, $sTitle = "ShowBalloonTip", $sText = "Displays a balloon tip associated with an edit control"

	; Create GUI
	$hGui = GUICreate("Edit ShowBalloonTip", 400, 300)
	$hEdit = _GUICtrlEdit_Create($hGui, "", 2, 2, 394, 268)
	GUISetState()

	; Set Text
	_GUICtrlEdit_SetText($hEdit, "This is a test" & @CRLF & "Another Line" & @CRLF & "Append to the end?" & @CRLF & @CRLF)

	_GUICtrlEdit_ShowBalloonTip($hEdit, $sTitle, $sText, $TTI_INFO)
	Sleep(1000)
	Local $bool = _GUICtrlEdit_HideBalloonTip($hEdit)
	_GUICtrlEdit_AppendText($hEdit, "HideBalloonTip = " & $bool & @CRLF)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
