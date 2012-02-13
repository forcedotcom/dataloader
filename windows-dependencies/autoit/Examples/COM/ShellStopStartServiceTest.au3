; AutoITCOM 3.1.1
;
; Test file
;
; Stops and starts the 'Windows Update' service
;
;
; This requires Boolean support in AutoIt
;
;
; http://msdn.microsoft.com/library/en-us/shellcc/platform/shell/reference/objects/ishelldispatch2/ishelldispatch2.asp

; Open Windows Shell object
$oShell=ObjCreate("shell.application")


if $oShell.IsServiceRunning("wuauserv") then  

   $oShell.ServiceStop("wuauserv",false)

   Msgbox(0,"Service Stopped","Service: automatic update services is now stopped")
   
   $oShell.ServiceStart("wuauserv",false)

   Msgbox(0,"Service Started","Service: automatic update services is started again")

endif
