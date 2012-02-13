_Example()




Func _Example()

	; Error monitoring. This will trap all COM errors while alive.
	; This particular object is declared as local, meaning after the function returns it will not exist.
	Local $oErrorHandler = ObjEvent("AutoIt.Error", "_ErrFunc")

	; Create Internet Explorer object
	Local $oIE = ObjCreate("InternetExplorer.Application")
	; Check for errors
	If @error Then Return

	$oIE.Visible = True ; set visibility

	; Custom sink object
	Local $oIEEvents = ObjEvent($oIE, "_IEEvent_", "DWebBrowserEvents2")

	; Navigate somewhere
	$oIE.navigate("http://www.google.com/")
	; Check for errors while loading
	If @error Then
		$oIE.Quit()
		Return
	EndIf

	; Wait for page to load
	While 1
		If $oIE.readyState = "complete" Or $oIE.readyState = 4 Then ExitLoop
		Sleep(10)
	WEnd

	; Deliberately cause error by calling non-existing method
	$oIE.PlayMeARockAndRollSong()
	; Check for errors
	If @error Then MsgBox(48 + 262144, "COM Error", "@error is set to COM error number." & @CRLF & "@error = " & @error)

	; Wait few seconds to see if more events will be fired
	Sleep(3000)

	; Nothing more to do. Close IE and return from the function
	$oIE.Quit()

	#forceref $oErrorHandler, $oIEEvents

EndFunc   ;==>_Example


; BeforeNavigate2 method definition
Func _IEEvent_BeforeNavigate2($IEpDisp, $IEURL, $IEFlags, $IETargetFrameName, $IEPostData, $IEHeaders, $IECancel)
	ConsoleWrite("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!--BeforeNavigate2 fired--!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " & @CRLF & _
			"$IEpDisp = " & $IEpDisp() & "  -  " & ObjName($IEpDisp) & @CRLF & _ ; e.g. default property and name for the object
			"$IEURL = " & $IEURL & @CRLF & _
			"$IEFlags = " & $IEFlags & @CRLF & _
			"$IETargetFrameName = " & $IETargetFrameName & @CRLF & _
			"$IEPostData = " & $IEPostData & @CRLF & _
			"$IEHeaders = " & $IEHeaders & @CRLF & _
			"$IECancel = " & $IECancel & @CRLF & _
			"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " & @CRLF & @CRLF)
EndFunc   ;==>_IEEvent_BeforeNavigate2

; User's COM error function. Will be called if COM error occurs
Func _ErrFunc($oError)
	; Do anything here.
	ConsoleWrite("err.number is: " & @TAB & $oError.number & @CRLF & _
			"err.windescription:" & @TAB & $oError.windescription & @CRLF & _
			"err.description is: " & @TAB & $oError.description & @CRLF & _
			"err.source is: " & @TAB & $oError.source & @CRLF & _
			"err.helpfile is: " & @TAB & $oError.helpfile & @CRLF & _
			"err.helpcontext is: " & @TAB & $oError.helpcontext & @CRLF & _
			"err.lastdllerror is: " & @TAB & $oError.lastdllerror & @CRLF & _
			"err.scriptline is: " & @TAB & $oError.scriptline & @CRLF & _
			"err.retcode is: " & @TAB & $oError.retcode & @CRLF & @CRLF)
EndFunc   ;==>_ErrFunc
