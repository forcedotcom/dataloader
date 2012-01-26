#include <GuiListBox.au3>
#include <GUIConstantsEx.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hListBox

	; Create GUI
	GUICreate("List Box Set Column Width", 400, 296)
	$hListBox = GUICtrlCreateList("", 2, 2, 396, 296, BitOR($LBS_STANDARD, $LBS_MULTICOLUMN))
	GUISetState()

	; Set the width of the columns
	_GUICtrlListBox_SetColumnWidth($hListBox, 100)

	; Add strings
	_GUICtrlListBox_BeginUpdate($hListBox)
	For $iI = 1 To 50
		_GUICtrlListBox_AddString($hListBox, StringFormat("Item %03d", $iI))
	Next
	_GUICtrlListBox_EndUpdate($hListBox)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
