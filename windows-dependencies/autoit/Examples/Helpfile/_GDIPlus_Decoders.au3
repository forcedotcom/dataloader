#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>
#include <ScreenCapture.au3>
#include <WindowsConstants.au3>

Global $iMemo, $aEncoder, $hImage

_Main()

Func _Main()
	Local $hBitmap

	; Create GUI
	GUICreate("GDI+", 600, 400)
	$iMemo = GUICtrlCreateEdit("", 2, 2, 596, 396, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Initialize GDI+ library
	_GDIPlus_Startup()

	; Create an image to use for paramater lists
	$hBitmap = _ScreenCapture_Capture("", 0, 0, 1, 1)
	$hImage = _GDIPlus_BitmapCreateFromHBITMAP($hBitmap)

	; Show encoder parameters
	$aEncoder = _GDIPlus_Encoders()
	ShowEncoder("Encoder")

	; Show decoder parameters
	$aEncoder = _GDIPlus_Decoders()
	ShowEncoder("Decoder")

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

; Show encoder information
Func ShowEncoder($sTitle)
	Local $iI, $iJ, $iK, $sCLSID, $tData, $tParam, $tParams

	For $iI = 1 To $aEncoder[0][0]
		$sCLSID = _GDIPlus_EncodersGetCLSID($aEncoder[$iI][5])
		MemoWrite("Image " & $sTitle & " " & $iI)
		MemoWrite("  Codec GUID ............: " & $aEncoder[$iI][1])
		MemoWrite("  File format GUID ......: " & $aEncoder[$iI][2])
		MemoWrite("  Codec name ............: " & $aEncoder[$iI][3])
		MemoWrite("  Codec Dll file name ...: " & $aEncoder[$iI][4])
		MemoWrite("  Codec file format .....: " & $aEncoder[$iI][5])
		MemoWrite("  File name extensions ..: " & $aEncoder[$iI][6])
		MemoWrite("  Mime type .............: " & $aEncoder[$iI][7])
		MemoWrite("  Flags .................: 0x" & Hex($aEncoder[$iI][8]))
		MemoWrite("  Version ...............: " & $aEncoder[$iI][9])
		MemoWrite("  Signature count .......: " & $aEncoder[$iI][10])
		MemoWrite("  Signature size ........: " & $aEncoder[$iI][11])
		MemoWrite("  Signature pattern ptr .: 0x" & Hex($aEncoder[$iI][12]))
		MemoWrite("  Signature mask ptr ....: 0x" & Hex($aEncoder[$iI][13]))
		MemoWrite("  Paramater list size ...: " & _GDIPlus_EncodersGetParamListSize($hImage, $sCLSID))
		MemoWrite()

		$tParams = _GDIPlus_EncodersGetParamList($hImage, $sCLSID)
		If @error Then ContinueLoop

		For $iJ = 0 To DllStructGetData($tParams, "Count") - 1
			MemoWrite("  Image " & $sTitle & " Parameter " & $iJ)
			$tParam = DllStructCreate($tagGDIPENCODERPARAM, DllStructGetPtr($tParams, "Params") + ($iJ * 28))
			MemoWrite("    Parameter GUID ......: " & _WinAPI_StringFromGUID(DllStructGetPtr($tParam, "GUID")))
			MemoWrite("    Number of values ....: " & DllStructGetData($tParam, "Count"))
			MemoWrite("    Parameter type.......: " & DllStructGetData($tParam, "Type"))
			MemoWrite("    Parameter pointer ...: 0x" & Hex(DllStructGetData($tParam, "Values")))
			Switch DllStructGetData($tParam, "Type")
				Case 4
					$tData = DllStructCreate("int Data[" & DllStructGetData($tParam, "Count") & "]", DllStructGetData($tParam, "Values"))
					For $iK = 1 To DllStructGetData($tParam, "Count")
						MemoWrite("      Value .............: " & DllStructGetData($tData, 1, $iK))
					Next
				Case 6
					$tData = DllStructCreate("int Low;int High", DllStructGetData($tParam, "Values"))
					MemoWrite("      Low range .........: " & DllStructGetData($tData, "Low"))
					MemoWrite("      High range ........: " & DllStructGetData($tData, "High"))
			EndSwitch
			MemoWrite()
		Next
	Next
EndFunc   ;==>ShowEncoder
