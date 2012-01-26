; AutoIt 3.1.1 beta version
;
; COM Test file
;
; Test usage of Events with Internet Explorer
;
; See also: http://msdn.microsoft.com/workshop/browser/webbrowser/reference/objects/internetexplorer.asp

; Create a simple GUI for our output

#include "GUIConstants.au3"

$GUIMain=GUICreate ( "Event Test", 640, 480 )
$GUIEdit=GUICtrlCreateEdit ( "Test Log:" & @CRLF, 10, 10 , 600 , 400 )
GUISetState ()       ;Show GUI


$oMyError=ObjEvent("AutoIt.Error","MyErrFunc")

$oIE=ObjCreate("InternetExplorer.Application.1")

if @error then
  Msgbox(0,"","Error opening Internet Explorer: " & @error)
  exit
endif

$oIE.Visible=1
$oIE.RegisterAsDropTarget = 1
$oIE.RegisterAsBrowser = 1

; The Event interfaces of the Internet Explorer are defined in: SHDOCVW.DLL
;
;	HTMLElementEvents2
;	DWebBrowserEvents 
;	DWebBrowserEvents2 
; -> NOTE1: If you have installed VC6 (DevStudio8) this one is renamed to: DWebBrowserEvent2Sink !
; -> NOTE2: If you have installed the Adobe Acrobat Reader 6.0 IE plugin then the type library of this 
;           interface is modified to "AcroIEHelper 1.0 Type Library"


$SinkObject=ObjEvent($oIE,"IEEvent_","DWebBrowserEvents")
if @error then 
   Msgbox(0,"AutoIt COM Test","ObjEvent: Can't use interface 'DWebBrowserEvents'. error code: " & hex(@error,8))
   exit
endif


ProgressOn ( "Internet Explorer Event test", "Loading web page","",-1,-1, 16 )

$URL = "http://www.AutoItScript.com/"
$oIE.Navigate( $URL )

sleep(5000)			; Give it the time to load the web page

$SinkObject=0		; Stop IE Events
$oIE.Quit			; Quit IE
$oIE=0

ProgressOff()

GUISwitch ( $GUIMain )	; In case IE stealed the focus

GUICtrlSetData ( $GUIEdit, @CRLF & "End of Internet Explorer Events test." & @CRLF , "append" )
GUICtrlSetData ( $GUIEdit, "You may close this window now !" & @CRLF , "append" )
  
; Waiting for user to close the window
While 1
   $msg = GUIGetMsg()
   If $msg = $GUI_EVENT_CLOSE Then ExitLoop
Wend

GUIDelete ()

exit


; a few Internet Explorer Event Functions
; ---------------------------------------

Func IEEvent_ProgressChange($Progress,$ProgressMax)

	ProgressSet ( ($Progress * 100) / $ProgressMax , ($Progress * 100) / $ProgressMax & " percent to go." , "loading web page" )

EndFunc



Func IEEvent_StatusTextChange($Text)

	GUICtrlSetData ( $GUIEdit, "IE Status text changed to: " & $Text & @CRLF  , "append" )

EndFunc


Func IEEvent_PropertyChange( $szProperty)

	GUICtrlSetData ( $GUIEdit, "IE Changed the value of the property: " & $szProperty & @CRLF  , "append" )

EndFunc


Func IEEvent_DownloadBegin()

	GUICtrlSetData ( $GUIEdit, "IE has started a navigation operation" & @CRLF  , "append" )

EndFunc


Func IEEvent_DownloadComplete()

	GUICtrlSetData ( $GUIEdit, "IE has finished a navigation operation" & @CRLF  , "append" )

EndFunc



Func IEEvent_NavigateComplete2($oWebBrowser,$URL)  

;    IDispatch *pDisp,
;    VARIANT *URL

	GUICtrlSetData ( $GUIEdit, "IE has finished loading URL: " & $URL & @CRLF  , "append" )

EndFunc




; AutoIt Error Event Function
; ---------------------------

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
