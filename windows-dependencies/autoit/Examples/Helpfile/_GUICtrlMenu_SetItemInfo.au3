#include <GuiMenu.au3>

_Main()

Func _Main()
	Local $hWnd, $hMain, $hFile, $tInfo

	; Open Notepad
	Run("notepad.exe")
	WinWaitActive("[CLASS:Notepad]")
	$hWnd = WinGetHandle("[CLASS:Notepad]")
	$hMain = _GUICtrlMenu_GetMenu($hWnd)
	$hFile = _GUICtrlMenu_GetItemSubMenu($hMain, 0)

	; Get/Set Open command ID
	$tInfo = _GUICtrlMenu_GetItemInfo($hFile, 1)
	Writeln("Open command ID: " & DllStructGetData($tInfo, "ID"))
	DllStructSetData($tInfo, "ID", 0)
	_GUICtrlMenu_SetItemInfo($hFile, 1, $tInfo)
	$tInfo = _GUICtrlMenu_GetItemInfo($hFile, 1)
	Writeln("Open command ID: " & DllStructGetData($tInfo, "ID"))

EndFunc   ;==>_Main

; Write a line of text to Notepad
Func Writeln($sText)
	ControlSend("[CLASS:Notepad]", "", "Edit1", $sText & @CR)
EndFunc   ;==>Writeln
