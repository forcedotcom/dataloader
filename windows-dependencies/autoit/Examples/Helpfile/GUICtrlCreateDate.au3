#include <GUIConstantsEx.au3>
#include <DateTimeConstants.au3>

Example1()
Example2()
Example3()
Example4()

; example1
Func Example1()
	Local $date, $msg

	GUICreate("My GUI get date", 200, 200, 800, 200)
	$date = GUICtrlCreateDate("1953/04/25", 10, 10, 185, 20)
	GUISetState()

	; Run the GUI until the dialog is closed
	Do
		$msg = GUIGetMsg()
	Until $msg = $GUI_EVENT_CLOSE

	MsgBox(0, "Date", GUICtrlRead($date))
	GUIDelete()
EndFunc   ;==>Example1

; example2
Func Example2()
	Local $n, $msg

	GUICreate("My GUI get date", 200, 200, 800, 200)
	$n = GUICtrlCreateDate("", 10, 10, 100, 20, $DTS_SHORTDATEFORMAT)
	GUISetState()

	; Run the GUI until the dialog is closed
	Do
		$msg = GUIGetMsg()
	Until $msg = $GUI_EVENT_CLOSE

	MsgBox(0, "Date", GUICtrlRead($n))
	GUIDelete()
EndFunc   ;==>Example2

; example3
Func Example3()
	Local $date, $DTM_SETFORMAT_, $style

	GUICreate("My GUI get date", 200, 200, 800, 200)
	$date = GUICtrlCreateDate("1953/04/25", 10, 10, 185, 20)

	; to select a specific default format
	$DTM_SETFORMAT_ = 0x1032 ; $DTM_SETFORMATW
	$style = "yyyy/MM/dd HH:mm:ss"
	GUICtrlSendMsg($date, $DTM_SETFORMAT_, 0, $style)

	GUISetState()
	While GUIGetMsg() <> $GUI_EVENT_CLOSE
	WEnd

	MsgBox(0, "Time", GUICtrlRead($date))
EndFunc   ;==>Example3

; example4
Func Example4()
	Local $n, $msg

	GUICreate("My GUI get time", 200, 200, 800, 200)
	$n = GUICtrlCreateDate("", 20, 20, 100, 20, $DTS_TIMEFORMAT)
	GUISetState()

	; Run the GUI until the dialog is closed
	Do
		$msg = GUIGetMsg()
	Until $msg = $GUI_EVENT_CLOSE

	MsgBox(0, "Time", GUICtrlRead($n))
	GUIDelete()
EndFunc   ;==>Example4
