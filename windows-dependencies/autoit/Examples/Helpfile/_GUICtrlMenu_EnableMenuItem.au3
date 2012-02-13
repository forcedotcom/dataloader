#include <GuiMenu.au3>

_Main()

Func _Main()
	Local $hWnd, $hMain

	Run("notepad.exe")
	WinWaitActive("[CLASS:Notepad]")
	$hWnd = WinGetHandle("[CLASS:Notepad]")
	$hMain = _GUICtrlMenu_GetMenu($hWnd)

	; Disable/Gray Help menu
	_GUICtrlMenu_EnableMenuItem($hMain, 4, 3)

EndFunc   ;==>_Main
