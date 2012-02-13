#include <GuiMenu.au3>

_Main()

Func _Main()
	Local $hWnd, $hMain

	Run("notepad.exe")
	WinWaitActive("[CLASS:Notepad]")
	$hWnd = WinGetHandle("[CLASS:Notepad]")
	$hMain = _GUICtrlMenu_GetMenu($hWnd)

	; Show that Menu handle belongs to Notepad
	Writeln("Notepad handle: 0x" & Hex($hWnd))
	Writeln("Menu Parent ..: 0x" & Hex(_GUICtrlMenu_FindParent($hMain)))

EndFunc   ;==>_Main

; Write a line of text to Notepad
Func Writeln($sText)
	ControlSend("[CLASS:Notepad]", "", "Edit1", $sText & @CR)
EndFunc   ;==>Writeln
