#include <GuiEdit.au3>
#include <GUIConstantsEx.au3>

$Debug_Ed = False ; Check ClassName being passed to Edit functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hEdit

	; Create GUI
	GUICreate("Edit Empty Undo Buffer", 400, 300)
	$hEdit = GUICtrlCreateEdit("This is a test" & @CRLF & "Another Line", 2, 2, 394, 268)
	GUISetState()

	_GUICtrlEdit_AppendText($hEdit, @CRLF & "Append to the end?")

	MsgBox(4160, "Information", "Can Undo: " & _GUICtrlEdit_CanUndo($hEdit))

	_GUICtrlEdit_EmptyUndoBuffer($hEdit)

	MsgBox(4160, "Information", "Can Undo: " & _GUICtrlEdit_CanUndo($hEdit))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
