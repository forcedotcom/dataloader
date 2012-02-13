#include <GUIConstantsEx.au3>
#include <Clipboard.au3>
#include <WinAPI.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $iFormat, $iCount

	; Create GUI
	$hGUI = GUICreate("Clipboard", 600, 400)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 596, 396, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Open the clipboard
	If Not _ClipBoard_Open($hGUI) Then _WinAPI_ShowError("_ClipBoard_Open failed")

	; Show clipboard formats available
	MemoWrite("Clipboard formats ..: " & _ClipBoard_CountFormats())

	; Enumerate clipboard formats
	Do
		$iFormat = _ClipBoard_EnumFormats($iFormat)
		If $iFormat <> 0 Then
			$iCount += 1
			MemoWrite("Clipboard format " & $iCount & " .: " & _ClipBoard_FormatStr($iFormat))
		EndIf
	Until $iFormat = 0

	; Close the clipboard
	_ClipBoard_Close()

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write message to memo
Func MemoWrite($sMessage = "")
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
