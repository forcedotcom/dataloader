#include <GuiListBox.au3>
#include <GUIConstantsEx.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $iIndex, $hListBox

	; Create GUI
	GUICreate("List Box Find In Text", 400, 296)
	$hListBox = GUICtrlCreateList("", 2, 2, 396, 296)

	GUISetState()

	; Add strings
	_GUICtrlListBox_BeginUpdate($hListBox)
	For $iI = 1 To 9
		_GUICtrlListBox_AddString($hListBox, StringFormat("%03d : Random string", Random(1, 100, 1)))
	Next
	_GUICtrlListBox_InsertString($hListBox, "eXaCt tExT", 3)
	_GUICtrlListBox_EndUpdate($hListBox)

	; Find an item
	$iIndex = _GUICtrlListBox_FindInText($hListBox, "exa")
	_GUICtrlListBox_SetCurSel($hListBox, $iIndex)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
