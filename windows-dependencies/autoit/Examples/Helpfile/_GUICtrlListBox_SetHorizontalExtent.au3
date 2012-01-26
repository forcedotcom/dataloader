#include <GuiListBox.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hListBox

	; Create GUI
	GUICreate("List Box Set Horizontal Extent", 400, 296)
	$hListBox = GUICtrlCreateList("", 2, 2, 396, 296, BitOR($WS_BORDER, $WS_VSCROLL, $LBS_NOTIFY, $LBS_DISABLENOSCROLL, $WS_HSCROLL))
	GUISetState()

	; Add long string
	_GUICtrlListBox_AddString($hListBox, "AutoIt v3 is a freeware BASIC-like scripting language designed for automating the Windows GUI.")

	; Show current horizontal extent
	MsgBox(4160, "Information", "Horizontal Extent: " & _GUICtrlListBox_GetHorizontalExtent($hListBox))

	_GUICtrlListBox_SetHorizontalExtent($hListBox, 500)

	; Show current horizontal extent
	MsgBox(4160, "Information", "Horizontal Extent: " & _GUICtrlListBox_GetHorizontalExtent($hListBox))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
