#include <GUIConstantsEx.au3>
#include <GuiTreeView.au3>
#include <WindowsConstants.au3>

$Debug_TV = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()

	Local $hItem[10], $hFirstChild, $hTreeView
	Local $iStyle = BitOR($TVS_EDITLABELS, $TVS_HASBUTTONS, $TVS_HASLINES, $TVS_LINESATROOT, $TVS_DISABLEDRAGDROP, $TVS_SHOWSELALWAYS, $TVS_CHECKBOXES)

	GUICreate("TreeView Get Next Child", 400, 300)

	$hTreeView = GUICtrlCreateTreeView(2, 2, 396, 268, $iStyle, $WS_EX_CLIENTEDGE)
	GUISetState()

	_GUICtrlTreeView_BeginUpdate($hTreeView)
	For $x = 0 To 3
		$hItem[$x] = GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", $x), $hTreeView)
		For $y = 1 To Random(2, 10, 1)
			GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Child", $y), $hItem[$x])
		Next
	Next
	$hItem[4] = GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", 4), $hTreeView)
	For $x = 5 To 9
		$hItem[$x] = GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", $x), $hTreeView)
		For $y = 1 To Random(2, 10, 1)
			GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Child", $y), $hItem[$x])
		Next
	Next
	_GUICtrlTreeView_EndUpdate($hTreeView)

	$hFirstChild = _GUICtrlTreeView_GetFirstChild($hTreeView, $hItem[2])
	_GUICtrlTreeView_SelectItem($hTreeView, $hFirstChild)
	MsgBox(4160, "Information", "Index 2, Next Child: " & _GUICtrlTreeView_GetNextChild($hTreeView, $hFirstChild))
	_GUICtrlTreeView_SelectItem($hTreeView, _GUICtrlTreeView_GetNextChild($hTreeView, $hFirstChild))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
