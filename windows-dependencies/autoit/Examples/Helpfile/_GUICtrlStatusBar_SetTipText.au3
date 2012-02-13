#include <GUIConstantsEx.au3>
#include <GuiStatusBar.au3>
#include <WinAPI.au3>
#include <WindowsConstants.au3>

$Debug_SB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $iMemo

Example1()
Example2()

Func Example1()

	Local $hGUI, $hIcon, $hStatus
	Local $aParts[4] = [75, 150, 300, 400]

	; Create GUI
	$hGUI = GUICreate("(Example 1) StatusBar Set Tip Text", 400, 300)
	$hStatus = _GUICtrlStatusBar_Create($hGUI, -1, "", $SBARS_TOOLTIPS)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Set parts
	_GUICtrlStatusBar_SetParts($hStatus, $aParts)
	_GUICtrlStatusBar_SetText($hStatus, "Force tip to be shown when text is more than fits in the box", 1)

	; Set icon
	$hIcon = _WinAPI_LoadShell32Icon(23)
	_GUICtrlStatusBar_SetIcon($hStatus, 0, $hIcon)

	; Set text tips
	_GUICtrlStatusBar_SetTipText($hStatus, 0, "Tip works when only icon in part or text exceeds part")
	_GUICtrlStatusBar_SetTipText($hStatus, 1, "Force tip to be shown when text is more than fits in the box")

	MemoWrite("Hold Mouse Cursor over part to see tip." & @CRLF)

	; Show text tips
	MemoWrite("Text tip 1 .: " & _GUICtrlStatusBar_GetTipText($hStatus, 0) & @CRLF)
	MemoWrite("Text tip 2 .: " & _GUICtrlStatusBar_GetTipText($hStatus, 1))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	; Free icons
	_WinAPI_DestroyIcon($hIcon)
	GUIDelete()
EndFunc   ;==>Example1

Func Example2()

	Local $hGUI, $hStatus
	Local $aParts[4] = [75, 150, 300, 400]

	; Create GUI
	$hGUI = GUICreate("(Example 2) StatusBar Set Tip Text", 400, 300)
	$hStatus = _GUICtrlStatusBar_Create($hGUI, -1, "", $SBARS_TOOLTIPS)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 274, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Set parts
	_GUICtrlStatusBar_SetParts($hStatus, $aParts)
	_GUICtrlStatusBar_SetText($hStatus, "Force tip to be shown when text is more than fits in the box", 1)

	; Set icon
	_GUICtrlStatusBar_SetIcon($hStatus, 0, 23, "shell32.dll")

	; Set text tips
	_GUICtrlStatusBar_SetTipText($hStatus, 0, "Tip works when only icon in part or text exceeds part")
	_GUICtrlStatusBar_SetTipText($hStatus, 1, "Force tip to be shown when text is more than fits in the box")

	MemoWrite("Hold Mouse Cursor over part to see tip." & @CRLF)

	; Show text tips
	MemoWrite("Text tip 1 .: " & _GUICtrlStatusBar_GetTipText($hStatus, 0) & @CRLF)
	MemoWrite("Text tip 2 .: " & _GUICtrlStatusBar_GetTipText($hStatus, 1))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>Example2

; Write message to memo
Func MemoWrite($sMessage = "")
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
