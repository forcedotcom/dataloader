#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <GuiImageList.au3>

_Main()

Func _Main()
	Local $hImage, $hGUI, $hDC

	$hGUI = GUICreate("ImageList Draw", 400, 300)
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

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
