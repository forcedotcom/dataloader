#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <GDIPlus.au3>
#include <ScreenCapture.au3>
#include <WinAPI.au3>


Global $iMemo

_Main()

Func _Main()
	Local $hBitmap, $hImage, $iImageType, $sImageType

	; Create GUI
	GUICreate("GDI+", 600, 400)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 596, 396, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Initialize GDI+ library
	_GDIPlus_Startup()

	; Capture 32 bit bitmap
	$hBitmap = _ScreenCapture_Capture("")
	$hImage = _GDIPlus_BitmapCreateFromHBITMAP($hBitmap)

	$iImageType = _GDIPlus_ImageGetType($hImage)
	Switch $iImageType
		Case $GDIP_IMAGETYPE_UNKNOWN
			$sImageType = "Unrecognized bitmap format or not image file"
		Case $GDIP_IMAGETYPE_BITMAP ; BMP, PNG, GIF, JPEG, TIFF, ICO, EXIF
			$sImageType = "Bitmap"
		Case $GDIP_IMAGETYPE_METAFILE ; EMF, WMF
			$sImageType = "Metafile"
	EndSwitch

	; Show image type: Unidentified = 0, Bitmap = 1, Metafile = 2)
	MemoWrite("Image type: " & $sImageType);

	; Clean up resources
	_GDIPlus_ImageDispose($hImage)
	_WinAPI_DeleteObject($hBitmap)

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
