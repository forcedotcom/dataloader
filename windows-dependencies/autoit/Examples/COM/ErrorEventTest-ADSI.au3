; AutoIt 3.1.1.x beta version
;
; COM Test File
;
; Error Event test using Winnt ADSI
;
; This will cause an ErrorEvent on most computers.

; Initialize my Error function
$oErrObj = ObjEvent("AutoIt.Error","MyErrFunc")

; Open Winnt object on local machine, this might take a few seconds time.
$objContainer=ObjGet("WinNT://" & @COMPUTERNAME)
if @error then
	Msgbox(0,"AutoItCOM Test","Failed to open WinNT://. Error code: " & hex(@error,8))
	exit
endif


$strUser="CBrooke"
$clsUser=$objContainer.Create("User", $strUser)

; This will only succeed on computers where local user passwords are allowed to be empty.
$clsUser.SetInfo()


; The line below should throw an Error after a short timeout,
; because "domain" and "MyGroup" do not exist.

$objGroup=ObjGet("WinNT://domain/MyGroup, group")

if @error then
   msgbox(0,"","error opening object $objGroup, error code: " & @error)
   exit
else
   $objGroup.Add($clsUser.ADsPath)
   $objGroup.SetInfo()
endif

exit


;----------------

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