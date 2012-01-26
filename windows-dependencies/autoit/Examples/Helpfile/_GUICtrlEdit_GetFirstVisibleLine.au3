#include <GuiEdit.au3>
#include <GUIConstantsEx.au3>

$Debug_Ed = False ; Check ClassName being passed to Edit functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hEdit

	; Create GUI
	GUICreate("Edit Get First Visible Line", 400, 300)
	$hEdit = GUICtrlCreateEdit("", 2, 2, 394, 268)
	GUISetState()

	For $x = 0 To 20
		_GUICtrlEdit_AppendText($hEdit, StringFormat("[%02d] Append to the end?", $x) & @CRLF)
	Next

	MsgBox(4160, "Information", "First Visible Line: " & _GUICtrlEdit_GetFirstVisibleLine($hEdit))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
