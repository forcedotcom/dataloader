#include <GuiListBox.au3>
#include <GUIConstantsEx.au3>
#include <Constants.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hGUI, $hListBox

	; Create GUI
	$hGUI = GUICreate("(UDF Created) List Box Destroy", 400, 296)
	$hListBox = _GUICtrlListBox_Create($hGUI, "", 2, 2, 396, 296)
	GUISetState()

	; Add files
	_GUICtrlListBox_BeginUpdate($hListBox)
	_GUICtrlListBox_ResetContent($hListBox)
	_GUICtrlListBox_InitStorage($hListBox, 100, 4096)
	_GUICtrlListBox_Dir($hListBox, @WindowsDir & "\win*.exe")
	_GUICtrlListBox_AddFile($hListBox, @WindowsDir & "\notepad.exe")
	_GUICtrlListBox_Dir($hListBox, "", $DDL_DRIVES)
	_GUICtrlListBox_Dir($hListBox, "", $DDL_DRIVES, False)
	_GUICtrlListBox_EndUpdate($hListBox)

	MsgBox(4160, "Information", "Destroying ListBox")
	_GUICtrlListBox_Destroy($hListBox)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main
