#include <GUIConstantsEx.au3>
#include <GuiHeader.au3>
#include <WinAPI.au3>

$Debug_HDR = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $hHeader, $tRect, $tPos

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

	; Get header layout
	$tRect = _WinAPI_GetClientRect($hGUI)
	$tPos = _GUICtrlHeader_Layout($hHeader, $tRect)

	; Show header layout
	MemoWrite("Window handle .....: " & DllStructGetData($tPos, "hWnd"))
	MemoWrite("Z order handle ....: " & DllStructGetData($tPos, "InsertAfter"))
	MemoWrite("Horizontal position: " & DllStructGetData($tPos, "X"))
	MemoWrite("Vertical position .: " & DllStructGetData($tPos, "Y"))
	MemoWrite("Width .............: " & DllStructGetData($tPos, "CX"))
	MemoWrite("Height ............: " & DllStructGetData($tPos, "CY"))
	MemoWrite("Flags .............: " & DllStructGetData($tPos, "Flags"))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
