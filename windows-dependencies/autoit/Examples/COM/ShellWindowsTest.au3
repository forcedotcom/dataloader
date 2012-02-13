; AutoItCOM 3.1.0
;
; Test file
;
; Counting the number of open shell windows

$oShell = ObjCreate("shell.application")	; Get the Windows Shell Object
$oShellWindows=$oShell.windows			; Get the collection of open shell Windows

if Isobj($oShellWindows) then

  $string=""					; String for displaying purposes

  for $Window in $oShellWindows  		; Count all existing shell windows
	$String = $String & $Window.LocationName & @CRLF
  next

  Msgbox(0,"Shell Windows","You have the following shell windows:" & @CRLF & @CRLF & $String);

endif


