#include <GUIConstantsEx.au3>
#include <GuiButton.au3>
#include <GuiImageList.au3>

_Main()

Func _Main()
	Local $hImage, $y = 70, $iIcon = 125, $btn[6], $rdo[6], $chk[6], $hImageSmall

	GUICreate("Buttons", 510, 400)
	GUISetState()

	$hImage = _GUIImageList_Create(32, 32, 5, 3, 6)
	For $x = 6 To 11
		_GUIImageList_AddIcon($hImage, "shell32.dll", $x, True)
	Next

	$hImageSmall = _GUIImageList_Create(16, 16, 5, 3, 6)
	For $x = 6 To 11
		_GUIImageList_AddIcon($hImageSmall, "shell32.dll", $x)
	Next

	$btn[0] = GUICtrlCreateButton("Button1", 10, 10, 90, 50)
	_GUICtrlButton_SetImageList($btn[0], $hImage)

	$rdo[0] = GUICtrlCreateRadio("Radio Button1", 120, 10, 120, 25)
	_GUICtrlButton_SetImageList($rdo[0], $hImageSmall)

	$chk[0] = GUICtrlCreateCheckbox("Check Button1", 260, 10, 120, 25)
	_GUICtrlButton_SetImageList($chk[0], $hImageSmall)


	For $x = 1 To 5
		$btn[$x] = GUICtrlCreateButton("Button" & $x + 1, 10, $y, 90, 50)
		_GUICtrlButton_SetImageList($btn[$x], _GetImageListHandle("shell32.dll", $iIcon + $x, True), $x)
		$rdo[$x] = GUICtrlCreateRadio("Radio Button" & $x + 1, 120, $y, 120, 25)
		_GUICtrlButton_SetImageList($rdo[$x], _GetImageListHandle("shell32.dll", $iIcon + $x), $x)
		$chk[$x] = GUICtrlCreateCheckbox("Check Button" & $x + 1, 260, $y, 120, 25)
		_GUICtrlButton_SetImageList($chk[$x], _GetImageListHandle("shell32.dll", $iIcon + $x), $x)
		$y += 60
	Next

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
		EndSwitch
	WEnd

	Exit
EndFunc   ;==>_Main

; using image list to set 1 image and have text on button
Func _GetImageListHandle($sFile, $nIconID = 0, $fLarge = False)
	Local $iSize = 16
	If $fLarge Then $iSize = 32

	Local $hImage = _GUIImageList_Create($iSize, $iSize, 5, 3)
	If StringUpper(StringMid($sFile, StringLen($sFile) - 2)) = "BMP" Then
		_GUIImageList_AddBitmap($hImage, $sFile)
	Else
		_GUIImageList_AddIcon($hImage, $sFile, $nIconID, $fLarge)
	EndIf
	Return $hImage
EndFunc   ;==>_GetImageListHandle
