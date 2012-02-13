#include <WinAPI.au3>
#include <GUIConstantsEx.au3>

_Main()

Func _Main()
	Local $msg, $btnFocus, $win, $text
	GUICreate("__WinAPI_GetFocus Example", 200, 200)
	$btnFocus = GUICtrlCreateButton("Get Focus", 50, 85, 100, 30)
	GUISetState(@SW_SHOW)
	While 1
		$msg = GUIGetMsg()
		Select
			Case $msg = $GUI_EVENT_CLOSE
				Exit
			Case $msg = $btnFocus
				$win = _WinAPI_GetFocus()
				$text = "Full Title: " & WinGetTitle($win) & @LF
				$text &= "Full Text: " & WinGetText($win) & @LF
				$text &= "Handle: " & WinGetHandle($win) & @LF
				$text &= "Process: " & WinGetProcess($win) & @LF
				MsgBox(0, "", $text)
		EndSelect
	WEnd
EndFunc   ;==>_Main
