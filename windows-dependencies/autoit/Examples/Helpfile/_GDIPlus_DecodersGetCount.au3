#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	; Create GUI
	GUICreate("GDI+", 600, 400)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 596, 396, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Initialize GDI+ library
	_GDIPlus_Startup()

	; Show number of decoders/encoders
	MemoWrite("Decoder count : " & _GDIPlus_DecodersGetCount());
	MemoWrite("Decoder size .: " & _GDIPlus_DecodersGetSize());
	MemoWrite("Encoder count : " & _GDIPlus_EncodersGetCount());
	MemoWrite("Encoder size .: " & _GDIPlus_EncodersGetSize());

	; Shut down GDI+ library
	_GDIPlus_Shutdown()

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage = '')
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
