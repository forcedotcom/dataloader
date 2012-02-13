#include <GuiEdit.au3>
#include <GUIConstantsEx.au3>

$Debug_Ed = False ; Check ClassName being passed to Edit functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hGUI, $hEdit

	; Create GUI
	$hGUI = GUICreate("Edit Destroy", 400, 300)
	$hEdit = _GUICtrlEdit_Create($hGUI, "This is a test" & @CRLF & "Another Line", 2, 2, 394, 268)
	GUISetState()

	_GUICtrlEdit_AppendText($hEdit, @CRLF & "Append to the end?")

	MsgBox(4160, "Information", "Destroy Edit Control")
	_GUICtrlEdit_Destroy($hEdit)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
