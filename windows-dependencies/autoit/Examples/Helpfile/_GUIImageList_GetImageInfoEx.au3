#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <GuiImageList.au3>

Global $iMemo

_Main()

Func _Main()
	Local $hImage, $hGUI, $hDC, $tInfo

	$hGUI = GUICreate("ImageList Get Icon InfoEx", 400, 300)
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 266, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Load images
	$hImage = _GUIImageList_Create(32, 24)
	_GUIImageList_Add($hImage, _WinAPI_CreateSolidBitmap($hGUI, 0xFF0000, 32, 24))
	_GUIImageList_Add($hImage, _WinAPI_CreateSolidBitmap($hGUI, 0x00FF00, 32, 24))
	_GUIImageList_Add($hImage, _WinAPI_CreateSolidBitmap($hGUI, 0x0000FF, 32, 24))

	; Draw images
	$hDC = _WinAPI_GetDC($hGUI)
	_GUIImageList_Draw($hImage, 0, $hDC, 4, 4)
	_GUIImageList_Draw($hImage, 1, $hDC, 40, 4)
	_GUIImageList_Draw($hImage, 2, $hDC, 76, 4)

	_WinAPI_ReleaseDC($hGUI, $hDC)

	; Show information for second image
	$tInfo = _GUIImageList_GetImageInfoEx($hImage, 1)
	MemoWrite("Image handle .: 0x" & Hex(DllStructGetData($tInfo, "hBitmap")))
	MemoWrite("Mask handle ..: " & DllStructGetData($tInfo, "hMask"))
	MemoWrite("Image Left ...: " & DllStructGetData($tInfo, "Left"))
	MemoWrite("Image Top ....: " & DllStructGetData($tInfo, "Top"))
	MemoWrite("Image Right ..: " & DllStructGetData($tInfo, "Right"))
	MemoWrite("Image Bottom .: " & DllStructGetData($tInfo, "Bottom"))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
