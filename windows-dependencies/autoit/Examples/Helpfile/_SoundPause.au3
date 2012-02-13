#include <Sound.au3>

Local $aSound = _SoundOpen(@WindowsDir & "\media\tada.wav")
If @error = 2 Then
	MsgBox(0, "Error", "The file does not exist")
	Exit
ElseIf @extended <> 0 Then
	Local $iExtended = @extended ; Assign because @extended will be set after DllCall.
	Local $tText = DllStructCreate("char[128]")
	DllCall("winmm.dll", "short", "mciGetErrorStringA", "str", $iExtended, "ptr", DllStructGetPtr($tText), "int", 128)
	MsgBox(0, "Error", "The open failed." & @CRLF & "Error Number: " & $iExtended & @CRLF & "Error Description: " & DllStructGetData($tText, 1) & @CRLF & "Please Note: The sound may still play correctly.")
Else
	MsgBox(0, "Success", "The file opened successfully")
EndIf
_SoundPlay($aSound)
Sleep(1000)

_SoundPause($aSound)
Sleep(1000)

_SoundResume($aSound)

While 1
	Sleep(100)
	If _SoundPos($aSound, 2) >= _SoundLength($aSound, 2) Then ExitLoop
WEnd

_SoundClose($aSound)
