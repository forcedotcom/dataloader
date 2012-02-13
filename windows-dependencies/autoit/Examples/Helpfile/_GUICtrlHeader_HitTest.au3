#include <GUIConstantsEx.au3>
#include <GuiHeader.au3>

$Debug_HDR = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $hHeader, $aHT

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

	; Do a hit test on column 2
	$aHT = _GUICtrlHeader_HitTest($hHeader, 110, 10)
	MemoWrite("Item index ...................: " & $aHT[0])
	MemoWrite("In client window .............: " & $aHT[1])
	MemoWrite("In control rectangle .........: " & $aHT[2])
	MemoWrite("On divider ...................: " & $aHT[3])
	MemoWrite("On zero width divider ........: " & $aHT[4])
	MemoWrite("Over filter area .............: " & $aHT[5])
	MemoWrite("Over filter button ...........: " & $aHT[6])
	MemoWrite("Above bounding rectangle .....: " & $aHT[7])
	MemoWrite("Below bounding rectangle .....: " & $aHT[8])
	MemoWrite("To right of bounding rectangle: " & $aHT[9])
	MemoWrite("To left of bounding rectangle : " & $aHT[10])

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
