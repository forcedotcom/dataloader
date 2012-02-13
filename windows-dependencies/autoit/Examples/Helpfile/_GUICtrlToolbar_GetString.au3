#include <GuiToolbar.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>

$Debug_TB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work
Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $hToolbar, $aStrings[4]
	Local Enum $idNew = 1000, $idOpen, $idSave, $idHelp

	; Create GUI
	$hGUI = GUICreate("Toolbar", 400, 300)
	$hToolbar = _GUICtrlToolbar_Create($hGUI)
	$iMemo = GUICtrlCreateEdit("", 2, 45, 396, 262, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 10, 400, 0, "Courier New")
	GUISetState()

	; Add standard system bitmaps
	Switch _GUICtrlToolbar_GetBitmapFlags($hToolbar)
		Case 0
			_GUICtrlToolbar_AddBitmap($hToolbar, 1, -1, $IDB_STD_SMALL_COLOR)
		Case 2
			_GUICtrlToolbar_AddBitmap($hToolbar, 1, -1, $IDB_STD_LARGE_COLOR)
	EndSwitch

	; Add strings
	$aStrings[0] = _GUICtrlToolbar_AddString($hToolbar, "&New")
	$aStrings[1] = _GUICtrlToolbar_AddString($hToolbar, "&Open")
	$aStrings[2] = _GUICtrlToolbar_AddString($hToolbar, "&Save")
	$aStrings[3] = _GUICtrlToolbar_AddString($hToolbar, "&Help")

	; Add buttons
	_GUICtrlToolbar_AddButton($hToolbar, $idNew, $STD_FILENEW, $aStrings[0])
	_GUICtrlToolbar_AddButton($hToolbar, $idOpen, $STD_FILEOPEN, $aStrings[1])
	_GUICtrlToolbar_AddButton($hToolbar, $idSave, $STD_FILESAVE, $aStrings[2])
	_GUICtrlToolbar_AddButtonSep($hToolbar)
	_GUICtrlToolbar_AddButton($hToolbar, $idHelp, $STD_HELP, $aStrings[3])

	; Get string 2 text
	MemoWrite("String 2 text .: " & _GUICtrlToolbar_GetString($hToolbar, 2))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write message to memo
Func MemoWrite($sMessage = "")
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
