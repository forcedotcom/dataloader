; AutoIt 3.1.1.x beta
;
; COM Test File
;
; Testing Error Event handling by creating a WMI exception

$MyCompName = @ComputerName

; Initialize my error handler
$oMyError = ObjEvent("AutoIt.Error","MyErrFunc")

; Open WMI
$oWMIservice =  ObjGet("winmgmts:\\" & $MyCompName  & "\root\cimv2")

; Check is WMI opened successfully
If @error then 
	Msgbox (0,"AutoItCOM Test","Error getting object $oWMIservice. Error code: " & @error)
	exit
Endif

; Try a query
$ColProcessors = $oWMIService.ExecQuery("Select * from Win32_Processor")


; THIS line should deliberately fail!
$item1=$ColProcessors.item(1)

If @error then
  Msgbox (0,"AutoItCOM test","Test passed: We got an error number: " & @error)
Else
  Msgbox (0,"AutoItCOM test","Test failed!")
Endif

Exit



; This is my custom error handler
Func MyErrFunc()

  $HexNumber=hex($oMyError.number,8)

  Msgbox(0,"AutoItCOM Test","We intercepted a COM Error !"       & @CRLF  & @CRLF & _
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