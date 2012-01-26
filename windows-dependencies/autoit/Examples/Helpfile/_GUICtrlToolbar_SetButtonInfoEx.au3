#include <GuiToolbar.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>

$Debug_TB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work
Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $hToolbar, $tButton
	Local Enum $idNew = 1000, $idOpen, $idSave, $idHelp

	; Create GUI
	$hGUI = GUICreate("Toolbar", 400, 300)
	$hToolbar = _GUICtrlToolbar_Create($hGUI)
	$iMemo = GUICtrlCreateEdit("", 2, 36, 396, 262, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 10, 400, 0, "Courier New")
	GUISetState()

	; Add standard system bitmaps
	Switch _GUICtrlToolbar_GetBitmapFlags($hToolbar)
		Case 0
			_GUICtrlToolbar_AddBitmap($hToolbar, 1, -1, $IDB_STD_SMALL_COLOR)
		Case 2
			_GUICtrlToolbar_AddBitmap($hToolbar, 1, -1, $IDB_STD_LARGE_COLOR)
	EndSwitch

	; Add buttons
	_GUICtrlToolbar_AddButton($hToolbar, $idNew, $STD_FILENEW)
	_GUICtrlToolbar_AddButton($hToolbar, $idOpen, $STD_FILEOPEN)
	_GUICtrlToolbar_AddButton($hToolbar, $idSave, $STD_FILESAVE)
	_GUICtrlToolbar_AddButtonSep($hToolbar)
	_GUICtrlToolbar_AddButton($hToolbar, $idHelp, $STD_HELP)

	; Set Save button information
	$tButton = DllStructCreate($tagTBBUTTONINFO)
	DllStructSetData($tButton, "Mask", BitOR($TBIF_IMAGE, $TBIF_STATE, $TBIF_SIZE, $TBIF_LPARAM))
	DllStructSetData($tButton, "State", BitOR($TBSTATE_PRESSED, $TBSTATE_ENABLED))
	DllStructSetData($tButton, "Image", $STD_PRINT)
	DllStructSetData($tButton, "CX", 100)
	DllStructSetData($tButton, "Param", 1234)
	_GUICtrlToolbar_SetButtonInfoEx($hToolbar, $idSave, $tButton)

	; Show Save button information
	$tButton = _GUICtrlToolbar_GetButtonInfoEx($hToolbar, $idSave)
	MemoWrite("Image index ....: " & DllStructGetData($tButton, "Image"))
	MemoWrite("State flags ....: " & DllStructGetData($tButton, "State"))
	MemoWrite("Button width ...: " & DllStructGetData($tButton, "CX"))
	MemoWrite("Param ..........: " & DllStructGetData($tButton, "Param"))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write message to memo
Func MemoWrite($sMessage = "")
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
