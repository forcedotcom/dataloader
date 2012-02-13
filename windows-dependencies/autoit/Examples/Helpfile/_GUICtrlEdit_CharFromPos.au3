#include <GuiEdit.au3>
#include <GUIConstantsEx.au3>

$Debug_Ed = False ; Check ClassName being passed to Edit functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $aCharPos[2], $hEdit

	; Create GUI
	GUICreate("Edit Char From Pos", 400, 300)
	$hEdit = GUICtrlCreateEdit("This is a test" & @CRLF & "Another Line", 2, 2, 394, 268)
	GUISetState()

	_GUICtrlEdit_AppendText($hEdit, @CRLF & "Append to the end?")

	$aCharPos = _GUICtrlEdit_CharFromPos($hEdit, 100, 20)
	MsgBox(4160, "Information", StringFormat("Char Nearsest Point: [%2d]", $aCharPos[0]) & @LF & _
			StringFormat("Line Nearest Point: [%2d]", $aCharPos[1]))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
