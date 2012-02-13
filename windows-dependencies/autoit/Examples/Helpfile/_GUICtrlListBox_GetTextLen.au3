#include <GuiListBox.au3>
#include <GUIConstantsEx.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $sText, $hListBox

	; Create GUI
	GUICreate("List Box Get Text Len", 400, 296)
	$hListBox = GUICtrlCreateList("", 2, 2, 396, 296, BitOR($LBS_STANDARD, $LBS_EXTENDEDSEL))
	GUISetState()

	; Add strings
	_GUICtrlListBox_BeginUpdate($hListBox)
	For $iI = 1 To 9
		$sText = StringFormat("%03d : Random string ", Random(1, 100, 1))
		For $iX = 1 To Random(1, 20, 1)
			$sText &= Chr(Random(65, 90, 1))
		Next
		_GUICtrlListBox_AddString($hListBox, $sText)
	Next
	_GUICtrlListBox_EndUpdate($hListBox)

	; Show item text length
	MsgBox(4160, "Information", "Item 5 Text Length: " & _GUICtrlListBox_GetTextLen($hListBox, 4))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
