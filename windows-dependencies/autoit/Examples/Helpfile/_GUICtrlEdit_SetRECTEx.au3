#include <GuiEdit.au3>
#include <GuiStatusBar.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>

$Debug_Ed = False ; Check ClassName being passed to Edit functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $StatusBar, $hEdit, $hGUI
	Local $Wow64 = ""
	If @AutoItX64 Then $Wow64 = "\Wow6432Node"
	Local $sFile = RegRead("HKEY_LOCAL_MACHINE\SOFTWARE" & $Wow64 & "\AutoIt v3\AutoIt", "InstallDir") & "\include\changelog.txt"
	$sFile = RegRead("HKEY_LOCAL_MACHINE\SOFTWARE\AutoIt v3\AutoIt", "InstallDir") & "\include\changelog.txt"
	Local $aPartRightSide[6] = [50, 130, 210, 290, 378, -1], $tRect

	; Create GUI
	$hGUI = GUICreate("Edit Set RECTEx", 400, 300)
	$hEdit = GUICtrlCreateEdit("", 2, 2, 394, 268, BitOR($ES_WANTRETURN, $WS_VSCROLL))
	$StatusBar = _GUICtrlStatusBar_Create($hGUI, $aPartRightSide)
	_GUICtrlStatusBar_SetIcon($StatusBar, 5, 97, "shell32.dll")
	_GUICtrlStatusBar_SetText($StatusBar, "Rect")
	GUISetState()

	; Get RectEx
	$tRect = _GUICtrlEdit_GetRECTEx($hEdit)
	DllStructSetData($tRect, "Left", DllStructGetData($tRect, "Left") + 10)
	DllStructSetData($tRect, "Top", DllStructGetData($tRect, "Top") + 10)
	DllStructSetData($tRect, "Right", DllStructGetData($tRect, "Right") - 10)
	DllStructSetData($tRect, "Bottom", DllStructGetData($tRect, "Bottom") - 10)

	; Add Text
	_GUICtrlEdit_AppendText($hEdit, FileRead($sFile))
	_GUICtrlEdit_LineScroll($hEdit, 0, _GUICtrlEdit_GetLineCount($hEdit) * - 1)

	; Set RectEx
	_GUICtrlEdit_SetRECTEx($hEdit, $tRect)

	; Get RectEx
	$tRect = _GUICtrlEdit_GetRECTEx($hEdit)
	_GUICtrlStatusBar_SetText($StatusBar, "Left: " & DllStructGetData($tRect, "Left"), 1)
	_GUICtrlStatusBar_SetText($StatusBar, "Topt: " & DllStructGetData($tRect, "Top"), 2)
	_GUICtrlStatusBar_SetText($StatusBar, "Right: " & DllStructGetData($tRect, "Right"), 3)
	_GUICtrlStatusBar_SetText($StatusBar, "Bottom: " & DllStructGetData($tRect, "Bottom"), 4)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
