#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <GuiButton.au3>
#include <GuiImageList.au3>

Global $iMemo

_Main()

Func _Main()
	Local $hImage, $btn, $aIdealSize

	GUICreate("Buttons", 400, 400)
	$iMemo = GUICtrlCreateEdit("", 119, 10, 276, 374, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")

	$hImage = _GUIImageList_Create(32, 32, 5, 3, 6)
	For $x = 6 To 11
		_GUIImageList_AddIcon($hImage, "shell32.dll", $x, True)
	Next

	$btn = GUICtrlCreateButton("Button1", 10, 10, 90, 50)
	_GUICtrlButton_SetImageList($btn, $hImage)

	GUISetState()

	$aIdealSize = _GUICtrlButton_GetIdealSize($btn)
	MemoWrite("Button1 Ideal width: " & $aIdealSize[0] & " height: " & $aIdealSize[1])

	Sleep(3000)

	MemoWrite(StringFormat("Set Size: %s", _GUICtrlButton_SetSize($btn, $aIdealSize[0], $aIdealSize[1])))

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
		EndSwitch
	WEnd

	Exit
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
