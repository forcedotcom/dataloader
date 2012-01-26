; Example 1
;
; Counting the number of open shell windows

Local $oShell = ObjCreate("shell.application") ; Get the Windows Shell Object
Local $oShellWindows = $oShell.windows ; Get the collection of open shell Windows

If IsObj($oShellWindows) Then

	Local $string = "" ; String for displaying purposes

	For $Window In $oShellWindows ; Count all existing shell windows
		$string = $string & $Window.LocationName & @CRLF
	Next

	MsgBox(0, "Shell Windows", "You have the following shell windows:" & @CRLF & @CRLF & $string);

EndIf
Exit


; Example 2
;
; Open the MediaPlayer on a REMOTE computer
Local $oRemoteMedia = ObjCreate("MediaPlayer.MediaPlayer.1", "name-of-remote-computer")

If Not @error Then
	MsgBox(0, "Remote ObjCreate Test", "ObjCreate() of a remote Mediaplayer Object successful !")
	$oRemoteMedia.Open(@WindowsDir & "\media\tada.wav") ; Play sound if file is present
Else
	MsgBox(0, "Remote ObjCreate Test", "Failed to open remote Object. Error code: " & Hex(@error, 8))
EndIf


