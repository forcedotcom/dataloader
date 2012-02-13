#include <GUIConstantsEx.au3>
#include <EditConstants.au3>

GUICreate("My GUI") ; will create a dialog box that when displayed is centered

Local $nEdit = GUICtrlCreateEdit("line 0", 10, 10)
GUICtrlCreateButton("Ok", 20, 200, 50)

GUISetState()

For $n = 1 To 5
	GUICtrlSetData($nEdit, @CRLF & "line " & $n)
Next


; Run the GUI until the dialog is closed
Do
	Local $msg = GUIGetMsg()
	If $msg > 0 Then
		Local $a = GUICtrlRecvMsg($nEdit, $EM_GETSEL)
		GUICtrlSetState($nEdit, $GUI_FOCUS) ; set focus back on edit control

		; will display the wParam and lParam values return by the control
		MsgBox(0, "Current selection", StringFormat("start=%d end=%d", $a[0], $a[1]))
	EndIf
Until $msg = $GUI_EVENT_CLOSE
