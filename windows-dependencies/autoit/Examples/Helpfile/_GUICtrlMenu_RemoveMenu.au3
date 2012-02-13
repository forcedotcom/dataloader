#include <GuiMenu.au3>

_Main()

Func _Main()
	Local $hWnd, $hMain

	; Open Notepad
	Run("notepad.exe")
	WinWaitActive("[CLASS:Notepad]")
	$hWnd = WinGetHandle("[CLASS:Notepad]")
	$hMain = _GUICtrlMenu_GetMenu($hWnd)

	; Remove Help menu
	_GUICtrlMenu_RemoveMenu($hMain, 4)

EndFunc   ;==>_Main
