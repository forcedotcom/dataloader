#include <GuiListBox.au3>
#include <GUIConstantsEx.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $sItems, $aItems, $hListBox

	; Create GUI
	GUICreate("List Box Get Sel Items Text", 400, 296)
	$hListBox = GUICtrlCreateList("", 2, 2, 396, 296, BitOR($LBS_STANDARD, $LBS_EXTENDEDSEL))
	GUISetState()

	; Add strings
	_GUICtrlListBox_BeginUpdate($hListBox)
	For $iI = 1 To 9
		_GUICtrlListBox_AddString($hListBox, StringFormat("%03d : Random string", Random(1, 100, 1)))
	Next
	_GUICtrlListBox_EndUpdate($hListBox)

	; Select a few items
	_GUICtrlListBox_SetSel($hListBox, 3)
	_GUICtrlListBox_SetSel($hListBox, 4)
	_GUICtrlListBox_SetSel($hListBox, 5)

	; Get indexes of selected items
	$aItems = _GUICtrlListBox_GetSelItemsText($hListBox)
	For $iI = 1 To $aItems[0]
		$sItems &= @LF & $aItems[$iI]
	Next
	MsgBox(4160, "Information", "Items Selected: " & $sItems)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
