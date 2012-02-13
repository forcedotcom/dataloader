#include <GuiComboBoxEx.au3>
#include <GuiImageList.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $hImage, $aSel, $hCombo

	; Create GUI
	$hGUI = GUICreate("ComboBoxEx Get Edit Sel", 400, 300)
	$hCombo = _GUICtrlComboBoxEx_Create($hGUI, "", 2, 2, 394, 70, BitOR($CBS_SIMPLE, $WS_VSCROLL, $WS_BORDER))
	$iMemo = GUICtrlCreateEdit("", 2, 72, 396, 226, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	$hImage = _GUIImageList_Create(16, 16, 5, 3)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 110)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 131)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 165)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 168)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 137)
	_GUIImageList_AddIcon($hImage, @SystemDir & "\shell32.dll", 146)
	_GUIImageList_Add($hImage, _GUICtrlComboBoxEx_CreateSolidBitMap($hCombo, 0xFF0000, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlComboBoxEx_CreateSolidBitMap($hCombo, 0x00FF00, 16, 16))
	_GUIImageList_Add($hImage, _GUICtrlComboBoxEx_CreateSolidBitMap($hCombo, 0x0000FF, 16, 16))
	_GUICtrlComboBoxEx_SetImageList($hCombo, $hImage)

	; Add strings
	For $x = 0 To 8
		_GUICtrlComboBoxEx_AddString($hCombo, StringFormat("%03d : Random string", Random(1, 100, 1)), $x, $x)
	Next

	;Set Cur Sel
	_GUICtrlComboBoxEx_SetCurSel($hCombo, Random(0, 8, 1))

	;Set Edit Sel
	_GUICtrlComboBoxEx_SetEditSel($hCombo, 0, 4)

	;Get Edit Sel
	$aSel = _GUICtrlComboBoxEx_GetEditSel($hCombo)
	MemoWrite(StringFormat("Selected: [%d][%d]", $aSel[0], $aSel[1]))

	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
