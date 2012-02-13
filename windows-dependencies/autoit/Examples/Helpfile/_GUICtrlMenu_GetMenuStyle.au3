#include <GuiMenu.au3>

_Main()

Func _Main()
	Local $hWnd, $hMain, $hFile

	; Open Notepad
	Run("notepad.exe")
	WinWaitActive("[CLASS:Notepad]")
	$hWnd = WinGetHandle("[CLASS:Notepad]")
	$hMain = _GUICtrlMenu_GetMenu($hWnd)
	$hFile = _GUICtrlMenu_GetItemSubMenu($hMain, 0)

	; Get/Set File menu style
	Writeln("File menu style: 0x" & Hex(_GUICtrlMenu_GetMenuStyle($hFile)))
	_GUICtrlMenu_SetMenuStyle($hFile, $MNS_NOCHECK)
	Writeln("File menu style: 0x" & Hex(_GUICtrlMenu_GetMenuStyle($hFile)))

EndFunc   ;==>_Main

; Write a line of text to Notepad
Func Writeln($sText)
	ControlSend("[CLASS:Notepad]", "", "Edit1", $sText & @CR)
EndFunc   ;==>Writeln
