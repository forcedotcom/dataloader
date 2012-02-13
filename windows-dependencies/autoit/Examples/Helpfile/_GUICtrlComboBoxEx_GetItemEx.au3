#include <GuiComboBoxEx.au3>
#include <GuiImageList.au3>
#include <GUIConstantsEx.au3>

$Debug_CB = False ; Check ClassName being passed to ComboBox/ComboBoxEx functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $hImage, $tItem, $sText, $iLen, $hCombo

	; Create GUI
	$hGUI = GUICreate("ComboBoxEx Get ItemEx", 400, 300)
	$hCombo = _GUICtrlComboBoxEx_Create($hGUI, "", 2, 2, 394, 100)
	$iMemo = GUICtrlCreateEdit("", 2, 32, 396, 266, 0)
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

	For $x = 0 To 8
		_GUICtrlComboBoxEx_AddString($hCombo, StringFormat("%03d : Random string", Random(1, 100, 1)), $x, $x)
	Next

	;Set Item indent
	_GUICtrlComboBoxEx_SetItemIndent($hCombo, 1, 1)

	;Create Structure
	$tItem = DllStructCreate($tagCOMBOBOXEXITEM)
	;Set Mask for what to retrieve
	DllStructSetData($tItem, "Mask", BitOR($CBEIF_IMAGE, $CBEIF_INDENT, $CBEIF_LPARAM, $CBEIF_SELECTEDIMAGE, $CBEIF_OVERLAY))
	;Set Index of item to retrieve
	DllStructSetData($tItem, "Item", 1)

	_GUICtrlComboBoxEx_GetItemEx($hCombo, $tItem)
	$iLen = _GUICtrlComboBoxEx_GetItemText($hCombo, 1, $sText)
	MemoWrite("Item Text : " & $sText)
	MemoWrite("Item Len ..........................: " & $iLen)
	MemoWrite("# image widths to indent ..........: " & DllStructGetData($tItem, "Indent"))
	MemoWrite("Zero based item image index .......: " & DllStructGetData($tItem, "Image"))
	MemoWrite("Zero based item state image index .: " & DllStructGetData($tItem, "SelectedImage"))
	MemoWrite("Zero based item image overlay index: " & DllStructGetData($tItem, "OverlayImage"))
	MemoWrite("Item application defined value ....: " & DllStructGetData($tItem, "Param"))

	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
