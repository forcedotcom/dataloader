#include <GuiToolbar.au3>
#include <GuiToolTip.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>

$Debug_TB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work
Global Enum $idNew = 1000, $idOpen, $idSave, $idHelp

_Main()

Func _Main()
	Local $hGUI, $hToolbar, $hToolTip

	; Create GUI
	$hGUI = GUICreate("Toolbar", 400, 300)
	$hToolbar = _GUICtrlToolbar_Create($hGUI)
	GUISetState()

	; Create ToolTip
	$hToolTip = _GUIToolTip_Create($hToolbar)
	_GUICtrlToolbar_SetToolTips($hToolbar, $hToolTip)

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

	; Show ToolTip handle
	MsgBox(4096, "Information", "ToolTip handle .: 0x" & Hex(_GUICtrlToolbar_GetToolTips($hToolbar)))

	; Loop until user exits
	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Handle WM_NOTIFY messages
Func WM_NOTIFY($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $iwParam
	Local $tInfo, $iID, $iCode

	$tInfo = DllStructCreate($tagNMTTDISPINFO, $ilParam)
	$iCode = DllStructGetData($tInfo, "Code")
	If $iCode = $TTN_GETDISPINFOW Then
		$iID = DllStructGetData($tInfo, "IDFrom")
		Switch $iID
			Case $idNew
				DllStructSetData($tInfo, "aText", "New")
			Case $idOpen
				DllStructSetData($tInfo, "aText", "Open")
			Case $idSave
				DllStructSetData($tInfo, "aText", "Save")
			Case $idHelp
				DllStructSetData($tInfo, "aText", "Help")
		EndSwitch
	EndIf
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_NOTIFY
