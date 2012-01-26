#include <WinAPI.au3>
_Main()

Func _Main()
	Local $aWindows, $i, $text
	$aWindows = _WinAPI_EnumWindowsPopup()
	For $i = 1 To $aWindows[0][0]
		$text = "Window Handle: " & $aWindows[$i][0] & @LF
		$text &= "Window Class: " & $aWindows[$i][1] & @LF
		$text &= "Window Title: " & WinGetTitle($aWindows[$i][0]) & @LF
		$text &= "Window Text: " & WinGetText($aWindows[$i][0]) & @LF
		$text &= "Window Process: " & WinGetProcess($aWindows[$i][0])
		MsgBox(0, "Item " & $i & " of " & $aWindows[0][0], $text)
	Next
EndFunc   ;==>_Main
