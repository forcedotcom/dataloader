; AutoITCOM 3.1.0
;
; Test file
;
; Test enumerating groups using ADSI
;
; See also: http://msdn.microsoft.com/library/en-us/adsi/adsi/adsi_winnt_provider.asp

; Note: opening a local WINNT object consumes a lot of time.
; Microsoft advises using WMI for these purposes. 
; But on systems that do not support WMI, you don't have this alternative.


SplashTextOn ("WINNT ADSI Test", "Opening local WINNT Object..this might take a few seconds...",-1,40)

; Initialize a COM Error Handler
$oMyErr = ObjEvent("AutoIt.Error","MyErrFunc")

; Open ADSI
$colGroups = ObjGet("WinNT://" & @ComputerName )
if @error then
  Msgbox(0,"AutoItCOM ADSI Test","Failed to open WinNT://. Error code: " & hex(@error,8))
  exit
endif

SplashOff()

; Note: this is NOT the same as:  GetObject("WinNT://<computer name>/,group")

dim $Array[1]			; Our filter array

$Array[0]="group"		; We only include 'groups' in this filter

$colGroups.Filter = $Array  	; Apply filter

if not isobj($colgroups) then 
   msgbox(0,"Grouptest","$colgroups is not an object")
   exit
endif

$DisplayString="Groups defined on this computer: " & @CRLF		; To display the results

For $oGroup In $colGroups
    $DisplayString = $DisplayString & $oGroup.Name & @TAB
Next

Msgbox(0,"WINNT ADSI Test",$DisplayString)

exit


;---------------

Func MyErrFunc()

 $hexnum=hex($oMyErr.number,8)
 Msgbox(0,"","We intercepted a COM Error!!"        & @CRLF                   & @CRLF & _
			 "err.description is: "    & $oMyErr.description    & @CRLF & _
			 "err.windescription is: " & $oMyErr.windescription & @CRLF & _
			 "err.lastdllerror is: "   & $oMyErr.lastdllerror   & @CRLF & _
			 "err.scriptline is: "     & $oMyErr.scriptline     & @CRLF & _
			 "err.number is: "         & $hexnum                 & @CRLF & _
			 "err.source is: "         & $oMyErr.source         & @CRLF & _
			 "err.helpfile is: "       & $oMyErr.helpfile       & @CRLF & _
			 "err.helpcontext is: "    & $oMyErr.helpcontext _
			)
 Seterror(1)

EndFunc