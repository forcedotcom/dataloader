; AutoIt 3.1.1.x beta
;
; COM Test file
;
; Test receiving Events from an open shell window
;
; See also:
; http://msdn.microsoft.com/library/en-us/shellcc/platform/shell/programmersguide/shell_basics/shell_basics_programming/objectmap.asp

$WindowName="COM"	;  Change this to an existing Window title

$oMyError=ObjEvent("AutoIt.Error","MyErrFunc")	; Catch any COM Errors

$oShell = ObjCreate("shell.application")

;See also: http://msdn.microsoft.com/library/en-us/shellcc/platform/shell/reference/objects/shellwindows/shellwindows.asp
$oShellWindows=$oShell.windows			; Get the collection of open shell Windows


if Isobj($oShellWindows) then

  $string=""
  $MyWindow=""

  ; Search in all windows for a window with the given name
  for $Window in $oShellWindows
	if $Window.LocationName = $WindowName then $MyWindow = $Window   ; Found a window
  next

  if IsObj($MyWindow) then
	; MyWindow is an Internet Explorer Object !
	;
	; See also:
	; http://msdn.microsoft.com/workshop/browser/webbrowser/reference/objects/internetexplorer.asp
	
	; Now we try to hook up our Event handler to this window
	
	$oMyEvent=ObjEvent($MyWindow,"MyEvent_")

	if @error then	; Failed to initialize event handler

		Msgbox(0,"COM Test","Error trying to hook Eventhandler on Window. Error number: " & hex(@error,8))
		$myWindow=""
		$oShellWindows=""
		exit

	endif
	Msgbox(0,"COM Test","Successfully received Events from a shell Window !");
  endif

else

  msgbox(0,"","you have no open shell window with the name " & $WindowName)

endif

exit

;-------------------
;Shell Window Events
;-------------------

Func MyEvent_aa()		; Dummy

EndFunc


;----------------

Func MyErrFunc()

  $HexNumber=hex($oMyError.number,8)

  Msgbox(0,"","We intercepted a COM Error !"       & @CRLF                          & @CRLF & _
			 "err.description is: "    & @TAB & $oMyError.description    & @CRLF & _
			 "err.windescription:"     & @TAB & $oMyError.windescription & @CRLF & _
			 "err.number is: "         & @TAB & $HexNumber              & @CRLF & _
			 "err.lastdllerror is: "   & @TAB & $oMyError.lastdllerror   & @CRLF & _
			 "err.scriptline is: "     & @TAB & $oMyError.scriptline     & @CRLF & _
			 "err.source is: "         & @TAB & $oMyError.source         & @CRLF & _
			 "err.helpfile is: "       & @TAB & $oMyError.helpfile       & @CRLF & _
			 "err.helpcontext is: "    & @TAB & $oMyError.helpcontext _
			)

  SetError(1)  ; to check for after this function returns
Endfunc

