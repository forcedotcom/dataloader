; AutoItCOM 3.1.1.x beta
;
; COM Test file
;
; Test usage of Events with SAPI
;
; NOTE: To be able to run this example, you must first
;       download and install the Microsoft SAPI SDK 5.1
;		http://www.microsoft.com/speech/download/sdk51/
;
; See also: http://www.microsoft.com/speech/techinfo/apioverview/
;
; And..READ the documentation carefully! Speech recognition is very complex stuff !

#include "GUIConstants.au3"

; Create a simple GUI for our output
GUICreate ( "Event Speech API Test", 640, 480 )
$GUIEdit=GUICtrlCreateEdit ( "Debug Log:" & @CRLF, 10, 10 , 600 , 400 )
GUISetState ()       ;Show GUI

$RecoContext=ObjCreate("SAPI.SpSharedRecoContext")
if @error then
	Msgbox(0,"","Error opening the 'SAPI.SpSharedRecoContext' object. Error number: " & hex(@error,8))
	exit
endif



; Initialize our Event Handler
; Note: The default outgoing event interface will be: _ISpeechRecoContextEvents
$SinkObject=ObjEvent($RecoContext,"MYEvent_")
if @error then 
  GUICtrlSetData ( $GUIEdit, "ObjEvent error: " & @error & @CRLF  , "append" )
else
  GUICtrlSetData ( $GUIEdit, "ObjEvent created Successfully!" & @CRLF  , "append" )

  ;Imported from: SAPI.H
  $SPRS_INACTIVE	= 0
  $SPRS_ACTIVE	= 1
  $SGDSActive=$SPRS_ACTIVE
  $SGDSInactive=$SPRS_INACTIVE

  $Grammar = $RecoContext.CreateGrammar(1)
  $Grammar.DictationLoad
  $Grammar.DictationSetState($SGDSActive)

  ; Dictation starts here...you may speak now ! 

  GUICtrlSetData ( $GUIEdit, "You have 10 seconds speaking time now...open your microphone and say something !" & @CRLF  , "append" )
  sleep(10000) 

  ; Stop dictation
  $Grammar.DictationSetState($SGDSInactive)

endif

  sleep (5000) ; Some events arrive late...

  GUICtrlSetData ( $GUIEdit, @CRLF & "End of dictation time." & @CRLF , "append" )
  GUICtrlSetData ( $GUIEdit, "You may close this window now !" & @CRLF , "append" )
  
  ; Waiting for user to close the window
  While 1
    $msg = GUIGetMsg()
    If $msg = $GUI_EVENT_CLOSE Then ExitLoop
  Wend

  GUIDelete ()

  exit




;--------------------
; SAPI Event functions

Func MYEvent_StartStream($StreamNumber, $StreamPosition)
;     StreamNumber As Long,
;     StreamPosition As Variant

 GUICtrlSetData ( $GUIEdit, "StartStream(): StreamNumber is:" & $StreamNumber & @CRLF  , "append" )

EndFunc


Func MYEvent_Hypothesis($StreamNumber,$StreamPosition,$Result )
;     StreamNumber As Long,
;     StreamPosition As Variant,
;     Result As ISpeechRecoResult

GUICtrlSetData ( $GUIEdit, "Hypothesis(): Hypothized text is: " & $Result.PhraseInfo.GetText & @CRLF  , "append" )

EndFunc


Func MYEvent_Interference($StreamNumber,$StreamPosition,$Interference)
;     StreamNumber As Long,
;     StreamPosition As Variant,
;     Interference As SpeechInterference

GUICtrlSetData ( $GUIEdit, "Interference(): StreamNumber is:" & $StreamNumber & @CRLF  , "append" )

EndFunc


Func MYEvent_Recognition($StreamNumber,$StreamPosition,$RecognitionType,$Result)
;    StreamNumber As Long,
;    StreamPosition As Variant,
;    RecognitionType As SpeechRecognitionType,
;    Result As ISpeechRecoResult

GUICtrlSetData ( $GUIEdit, "Recognition(): Recognized text is: " & $Result.PhraseInfo.GetText & @CRLF  , "append" )

EndFunc



; SAPI has MANY more Events, but we won't use these here

Func MYEvent_SoundEnd($StreamNumber,$StreamPosition)
;     StreamNumber As Long,
;     StreamPosition As Variant

;GUICtrlSetData ( $GUIEdit, "SoundEnd(): StreamNumber is:" & $StreamNumber & @CRLF  , "append" )

EndFunc

Func MYEvent_EndStream($StreamNumber,$StreamPosition,$StreamReleased)
;     StreamNumber As Long,
;     StreamPosition As Variant,
;     StreamReleased As Boolean

; GUICtrlSetData ( $GUIEdit, "EndStream(): StreamNumber is:" & $StreamNumber & @CRLF  , "append" )
; GUICtrlSetData ( $GUIEdit, "EndStream(): StreamReleased is:" & $StreamReleased & @CRLF  , "append" )

EndFunc


Func MYEvent_SoundStart($StreamNumber,$StreamPosition)
;     StreamNumber As Long,
;     StreamPosition As Variant

;GUICtrlSetData ( $GUIEdit, "SoundStart(), StreamNumber is: " & $StreamNumber & @CRLF  , "append" )
;GUICtrlSetData ( $GUIEdit, "SoundStart(): StreamPosition is:" & $StreamPosition & @CRLF  , "append" )

EndFunc


Func MYEvent_PhraseStart($StreamNumber,$StreamPosition)
;     StreamNumber As Long,
;     StreamPosition As Variant

;GUICtrlSetData ( $GUIEdit, "PhraseStart(): StreamNumber is:" & $StreamNumber & @CRLF  , "append" )

EndFunc

