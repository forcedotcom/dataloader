; AutoItCOM 3.1.1
;
; Test file
;
; Test usage of creating objects on a remote computer
;
; Notes:
;
;- The remote Object must have DCOM (Distributed COM) functionality.
;- The remote computer must have 'Remote Registry Service' and 'File and Printersharing' turned on!
;
; To check for any DCOM-Enabled Objects, use DCOMCNFG.EXE (=Component Services MMC) on the remote computer.

$RemoteComputer="REMOTE"	; Change this to your remote computer name
$RemoteUsername="REMOTE\Administrator"	; Change this to your username on the remote computer
$RemotePassword="123456"	; Change this to your password on the remote computer

; First install our own Error Handler
$oErrObj = ObjEvent("AutoIt.Error","MyErrFunc")

; Open MediaPlayer on a remote computer
$oRemoteMedia = ObjCreate("MediaPlayer.MediaPlayer.1",$RemoteComputer,$RemoteUsername,$RemotePassword)

if @error then
	Msgbox(0,"Remote ObjCreate Test","Failed to open remote Object. Error code: " & hex(@error,8))
	exit
endif

Msgbox(0,"Remote Test","ObjCreate() of a remote object successfull !")


$Enabled=$oRemoteMedia.IsSoundCardEnabled

if not @error then 
	Msgbox(0,"Remote Test","Invoking a method on a remote Object successfull!" & @CRLF & _
							"Result of 'IsSoundCardEnabled?':  " & $Enabled )
	If $Enabled = -1 Then
		$oRemoteMedia.Open("c:\windows\media\Windows XP Startup.wav")
		if not @error then Msgbox(0,"Remote Test","Playing sound on a remote computer successful !")
	EndIf
Else
	Msgbox(0,"Remote Test","Invoking a method on a remote Object Failed !")
EndIf


Exit


; ------------------------
; My custom error function

Func MyErrFunc()

 $hexnum=hex($oerrobj.number,8)

 Msgbox(0,"","We intercepted a COM Error!!"        & @CRLF                   & @CRLF & _
			 "err.description is: "    & $oErrobj.description    & @CRLF & _
			 "err.windescription is: " & $oErrobj.windescription & @CRLF & _
			 "err.lastdllerror is: "   & $oerrobj.lastdllerror   & @CRLF & _
			 "err.scriptline is: "     & $oerrobj.scriptline     & @CRLF & _
			 "err.number is: "         & $hexnum                 & @CRLF & _
			 "err.source is: "         & $oerrobj.source         & @CRLF & _
			 "err.helpfile is: "       & $oerrobj.helpfile       & @CRLF & _
			 "err.helpcontext is: "    & $oerrobj.helpcontext _
			)
 Seterror(1)
EndFunc