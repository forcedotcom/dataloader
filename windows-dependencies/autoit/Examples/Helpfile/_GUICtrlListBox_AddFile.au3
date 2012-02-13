#include <GuiListBox.au3>
#include <GUIConstantsEx.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hListBox

	; Create GUI
	GUICreate("List Box Add File", 400, 296)
	$hListBox = GUICtrlCreateList("", 2, 2, 396, 296)
	GUISetState()

	; Add file
	_GUICtrlListBox_AddFile($hListBox, @WindowsDir & "\notepad.exe")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
