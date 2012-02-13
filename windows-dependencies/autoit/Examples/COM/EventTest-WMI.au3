; AutoIt 3.1.1.x beta
;
; COM Test file
;
; Test usage of Events with WMI

; See also: http://msdn.microsoft.com/library/en-us/wmisdk/wmi/making_an_asynchronous_call_with_vbscript.asp

#include "GUIConstants.au3"

; WMI Requires a separate Sink Event Handler
$oWMISink=ObjCreate("WbemScripting.SWbemSink")

if @Error then
  Msgbox(0,"","Error opening oWMISink. Error code: " & @error)
  exit
endif

; Initialize our Event Handler and connect it to the WMI Sink
$SinkObject=ObjEvent($oWMISink,"MYSINK_")

if @error then 
   Msgbox(0,"","Error initializing Events. Error code: " & @error)
   exit
endif


; Make a simple GUI to output events
GUICreate( "WMI Event Test", 640, 480 )
$GUIEdit=GUICtrlCreateEdit( "WMI Active processes list:" & @CRLF, 10, 10 , 600 , 400 )
GUISetState()       ;Show GUI

; Open WMI
$oWMI = ObjGet("winmgmts:root\cimv2")

; Execute our asynchronous query
$oWMI.ExecQueryAsync($oWMISink, "SELECT Name FROM Win32_Process")



; Loop until user closes window
While 1
    $msg = GUIGetMsg()
    If $msg = $GUI_EVENT_CLOSE Then ExitLoop
Wend
GUIDelete ()

exit


;---My Event Functions---

Func MYSINK_Cancel()
  ; WMI Wants us to cancel the event
  $oWMISink.Cancel
  GUICtrlSetData ( $GUIEdit, @cRLF & "Cancel was requested."  & @CRLF , "append" )
EndFunc


Func MYSINK_OnProgress()

EndFunc


Func MYSINK_OnObjectReady($objObject,$objAsyncContext)

   GUICtrlSetData ( $GUIEdit, "Active Process name is: " & $objObject.Name & @CRLF , "append" )

EndFunc


Func MYSINK_OnCompleted($iHResult,$objErrorObject,$objAsyncContext)
   
    $oWMISink.Cancel  ; Cancel any leftovers
    GUICtrlSetData ( $GUIEdit, "Completed: WMI Asynchronous operation is done." & @CRLF , "append" )
    GUICtrlSetData ($GUIEdit, @CRLF & "You can now close this window" & @CRLF , "append" )
EndFunc