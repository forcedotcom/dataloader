#include <GuiListBox.au3>
#include <GUIConstantsEx.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

; Warning this should not be used on items created using built-in functions
; Item data is the ControlID for each string

_Main()

Func _Main()
	Local $hListBox

	; Create GUI
	GUICreate("List Box Get Item Data", 400, 296)
	$hListBox = GUICtrlCreateList("", 2, 2, 396, 296)
	GUISetState()

	; Add strings
	_GUICtrlListBox_BeginUpdate($hListBox)
	For $iI = 1 To 9
		_GUICtrlListBox_AddString($hListBox, StringFormat("%03d : Random string", Random(1, 100, 1)))
	Next
	_GUICtrlListBox_EndUpdate($hListBox)

	; Set item data
	_GUICtrlListBox_SetItemData($hListBox, 4, 1234)

	; Get item data
	MsgBox(4160, "Information", "Item 5 Data: " & _GUICtrlListBox_GetItemData($hListBox, 4))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
