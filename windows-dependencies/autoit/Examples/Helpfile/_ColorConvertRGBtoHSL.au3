#include <Color.au3>

_Main()

Func _Main()
	Local $aiInput[3] = [128, 255, 128], $aiHSL, $aiRGB, $sOutput

	$aiHSL = _ColorConvertRGBtoHSL($aiInput)
	$aiRGB = _ColorConvertHSLtoRGB($aiHSL)

	$sOutput &= StringFormat("| R: %.3f" & @TAB & "| H: %.3f" & @TAB & "| R: %.3f" & @CRLF, $aiInput[0], $aiHSL[0], $aiRGB[0])
	$sOutput &= StringFormat("| G: %.3f" & @TAB & "| S: %.3f" & @TAB & "| G: %.3f" & @CRLF, $aiInput[1], $aiHSL[1], $aiRGB[1])
	$sOutput &= StringFormat("| B: %.3f" & @TAB & "| L: %.3f" & @TAB & "| B: %.3f" & @CRLF, $aiInput[2], $aiHSL[2], $aiRGB[2])
	MsgBox(0, "AutoIt", $sOutput)
EndFunc   ;==>_Main
