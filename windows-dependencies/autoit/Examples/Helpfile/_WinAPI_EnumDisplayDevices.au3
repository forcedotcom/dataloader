#include <WinAPI.au3>
_Main()

Func _Main()
	Local $aDevice, $i = 0, $text
	While 1
		$aDevice = _WinAPI_EnumDisplayDevices("", $i)
		If Not $aDevice[0] Then ExitLoop
		$text = "Successful? " & $aDevice[0] & @LF
		$text &= "Device (Adapter or Monitor): " & $aDevice[1] & @LF
		$text &= "Description (Adapter or Monitor): " & $aDevice[2] & @LF
		$text &= "Device State Flag: " & $aDevice[3] & @LF
		If BitAND($aDevice[3], 32) Then $text &= @TAB & "- The device has more display modes than its output devices support" & @LF

		If BitAND($aDevice[3], 16) Then $text &= @TAB & "- The device is removable; it cannot be the primary display" & @LF
		If BitAND($aDevice[3], 8) Then $text &= @TAB & "- The device is VGA compatible" & @LF
		If BitAND($aDevice[3], 4) Then $text &= @TAB & "- Represents a pseudo device used to mirror application drawing for remoting" & @LF
		If BitAND($aDevice[3], 2) Then $text &= @TAB & "- The primary desktop is on the device" & @LF
		If BitAND($aDevice[3], 1) Then $text &= @TAB & "- The device is part of the desktop" & @LF

		$text &= "Plug and Play identifier string: " & $aDevice[4] & @LF
		MsgBox(0, "", $text)
		$i += 1
	WEnd
EndFunc   ;==>_Main
