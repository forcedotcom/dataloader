#include <GUIConstantsEx.au3>
#include <GuiTreeView.au3>
#include <WindowsConstants.au3>

$Debug_TV = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()

	Local $hItem[11], $htmp_item, $hTreeView
	Local $iStyle = BitOR($TVS_EDITLABELS, $TVS_HASBUTTONS, $TVS_HASLINES, $TVS_LINESATROOT, $TVS_DISABLEDRAGDROP, $TVS_SHOWSELALWAYS, $TVS_CHECKBOXES)

	GUICreate("TreeView Set Children", 400, 300)

	$hTreeView = GUICtrlCreateTreeView(2, 2, 396, 268, $iStyle, $WS_EX_CLIENTEDGE)
	GUISetState()

	_GUICtrlTreeView_BeginUpdate($hTreeView)
	For $x = 0 To 3
		$hItem[$x] = GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", $x), $hTreeView)
		For $y = 0 To 2
			$htmp_item = GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", $y), $hItem[$x])
		Next
	Next
	$hItem[4] = GUICtrlCreateTreeViewItem(StringFormat("Looking for me?", $x), $htmp_item)
	For $x = 5 To 9
		$hItem[$x] = GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", $x), $hTreeView)
		For $y = 0 To 2
			GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", $y), $hItem[$x])
		Next
	Next
	$hItem[10] = GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", 10), $hTreeView)
	_GUICtrlTreeView_EndUpdate($hTreeView)

	MsgBox(4160, "Information", "Item 0 has Children? " & _GUICtrlTreeView_GetChildren($hTreeView, $hItem[0]))
	MsgBox(4160, "Information", "Item 10 has Children? " & _GUICtrlTreeView_GetChildren($hTreeView, $hItem[10]))
	_GUICtrlTreeView_SetChildren($hTreeView, $hItem[10])
	MsgBox(4160, "Information", "Item 10 has Children? " & _GUICtrlTreeView_GetChildren($hTreeView, $hItem[10]))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
