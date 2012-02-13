#include <GUIConstantsEx.au3>
#include <GuiHeader.au3>

$Debug_HDR = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $hHeader

	; Create GUI
	$hGUI = GUICreate("Header", 400, 300)
	$hHeader = _GUICtrlHeader_Create($hGUI)
	$iMemo = GUICtrlCreateEdit("", 2, 24, 396, 274, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Add columns
	_GUICtrlHeader_AddItem($hHeader, "Column 1", 100)
	_GUICtrlHeader_AddItem($hHeader, "Column 2", 100)
	_GUICtrlHeader_AddItem($hHeader, "Column 3", 100)
	_GUICtrlHeader_AddItem($hHeader, "Column 4", 100)

	; Show the index for order 3
	MemoWrite("Order 3 index: " & _GUICtrlHeader_OrderToIndex($hHeader, 2))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
