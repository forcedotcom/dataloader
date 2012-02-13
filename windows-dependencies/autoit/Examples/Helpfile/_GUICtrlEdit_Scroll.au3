#include <GuiEdit.au3>
#include <GuiStatusBar.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <ScrollBarConstants.au3>

$Debug_Ed = False ; Check ClassName being passed to Edit functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $StatusBar, $hEdit, $hGUI
	Local $Wow64 = ""
	If @AutoItX64 Then $Wow64 = "\Wow6432Node"
	Local $sFile = RegRead("HKEY_LOCAL_MACHINE\SOFTWARE" & $Wow64 & "\AutoIt v3\AutoIt", "InstallDir") & "\include\changelog.txt"
	Local $aPartRightSide[3] = [200, 378, -1], $iLen

	; Create GUI
	$hGUI = GUICreate("Edit Scroll", 400, 300)
	$hEdit = GUICtrlCreateEdit("", 2, 2, 394, 268, BitOR($ES_WANTRETURN, $WS_VSCROLL))
	$StatusBar = _GUICtrlStatusBar_Create($hGUI, $aPartRightSide)
	_GUICtrlStatusBar_SetIcon($StatusBar, 2, 97, "shell32.dll")
	GUISetState()

	; Set Margins
	_GUICtrlEdit_SetMargins($hEdit, BitOR($EC_LEFTMARGIN, $EC_RIGHTMARGIN), 10, 10)

	; Set Text
	_GUICtrlEdit_SetText($hEdit, FileRead($sFile))

	MsgBox(4160, "Information", "Scroll Line Down")
	_GUICtrlEdit_Scroll($hEdit, $SB_LINEDOWN)

	MsgBox(4160, "Information", "Scroll Line Up")
	_GUICtrlEdit_Scroll($hEdit, $SB_LINEUP)

	MsgBox(4160, "Information", "Scroll Page Down")
	_GUICtrlEdit_Scroll($hEdit, $SB_PAGEDOWN)

	MsgBox(4160, "Information", "Scroll Page Up")
	_GUICtrlEdit_Scroll($hEdit, $SB_PAGEUP)

	$iLen = _GUICtrlEdit_GetTextLen($hEdit)
	_GUICtrlEdit_SetSel($hEdit, $iLen, $iLen)

	MsgBox(4160, "Information", "Scroll Caret")
	_GUICtrlEdit_Scroll($hEdit, $SB_SCROLLCARET)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
