; AutoItCOM 3.1.0
;
; Test File
;
; Returns processor information

$MyCompName = @ComputerName

$oWMIservice =  ObjGet("winmgmts:\\" & $MyCompName  & "\root\cimv2")

if @error then 
	Msgbox (0,"","error getting object. Error code: " & @error)
endif

$ColProcessors = $oWMIService.ExecQuery("Select * from Win32_Processor")

$Counter = 1

FOR $Processor IN $ColProcessors

  Msgbox(0, "WMI Test","Processor " & $Counter & @CRLF & "Id: " & $Processor.processorid & @CRLF & _
                       "Maximum Clock Speed: " & $Processor.MaxClockSpeed)
  $Counter = $Counter + 1

NEXT