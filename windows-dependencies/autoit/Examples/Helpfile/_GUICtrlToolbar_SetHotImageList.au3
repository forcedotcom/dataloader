#include <GuiToolbar.au3>
#include <GuiImageList.au3>
#include <WinAPI.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

$Debug_TB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work
Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $hToolbar, $hNormal, $hDisabled, $hHot
	Local Enum $idRed = 1000, $idGreen, $idBlue

	; Create GUI
	$hGUI = GUICreate("Toolbar", 400, 300)
	GUISetBkColor(0xffff00)
	$hToolbar = _GUICtrlToolbar_Create($hGUI)
	$iMemo = GUICtrlCreateEdit("", 2, 36, 396, 262, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 10, 400, 0, "Courier New")
	GUISetState()

	; Create normal image list
	$hNormal = _GUIImageList_Create(32, 24)
	_GUIImageList_Add($hNormal, _WinAPI_CreateSolidBitmap($hGUI, 0xFF0000, 32, 24))
	_GUIImageList_Add($hNormal, _WinAPI_CreateSolidBitmap($hGUI, 0x00FF00, 32, 24))
	_GUIImageList_Add($hNormal, _WinAPI_CreateSolidBitmap($hGUI, 0x0000FF, 32, 24))
	_GUICtrlToolbar_SetImageList($hToolbar, $hNormal)

	; Create disabled image list
	$hDisabled = _GUIImageList_Create(32, 24)
	_GUIImageList_Add($hDisabled, _WinAPI_CreateSolidBitmap($hGUI, 0xCCCCCC, 32, 24))
	_GUIImageList_Add($hDisabled, _WinAPI_CreateSolidBitmap($hGUI, 0xCCCCCC, 32, 24))
	_GUIImageList_Add($hDisabled, _WinAPI_CreateSolidBitmap($hGUI, 0xCCCCCC, 32, 24))
	_GUICtrlToolbar_SetDisabledImageList($hToolbar, $hDisabled)

	; Create hot image list
	$hHot = _GUIImageList_Create(32, 24)
	_GUIImageList_Add($hHot, _WinAPI_CreateSolidBitmap($hGUI, 0x111111, 32, 24))
	_GUIImageList_Add($hHot, _WinAPI_CreateSolidBitmap($hGUI, 0x888888, 32, 24))
	_GUIImageList_Add($hHot, _WinAPI_CreateSolidBitmap($hGUI, 0xAAAAAA, 32, 24))
	Local $hPrevImageList = _GUICtrlToolbar_SetHotImageList($hToolbar, $hHot)
	MemoWrite("Previous Hot image list handle .: 0x" & Hex($hPrevImageList))
	MemoWrite("IsPtr = " & IsPtr($hPrevImageList) & " IsHWnd = " & IsHWnd($hPrevImageList))


	; Add buttons
	_GUICtrlToolbar_AddButton($hToolbar, $idRed, 0)
	_GUICtrlToolbar_AddButton($hToolbar, $idGreen, 1)
	_GUICtrlToolbar_AddButton($hToolbar, $idBlue, 2)

	; Disable Blue button
	_GUICtrlToolbar_EnableButton($hToolbar, $idBlue, False)

	; Show image list handles
	MemoWrite("Disabled list handle .: 0x" & Hex(_GUICtrlToolbar_GetDisabledImageList($hToolbar)))
	MemoWrite("Hot list handle ......: 0x" & Hex(_GUICtrlToolbar_GetHotImageList($hToolbar)))
	MemoWrite("Normal list handle ...: 0x" & Hex(_GUICtrlToolbar_GetImageList($hToolbar)))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main

; Write message to memo
Func MemoWrite($sMessage = "")
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
