#include <GUIConstantsEx.au3>
#include <GuiTreeView.au3>
#include <WindowsConstants.au3>

$Debug_TV = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()

	Local $hItem[10], $iRand, $hTreeView
	Local $iStyle = BitOR($TVS_EDITLABELS, $TVS_HASBUTTONS, $TVS_HASLINES, $TVS_LINESATROOT, $TVS_DISABLEDRAGDROP, $TVS_SHOWSELALWAYS, $TVS_CHECKBOXES)

	GUICreate("TreeView Get Child Count", 400, 300)

	$hTreeView = GUICtrlCreateTreeView(2, 2, 396, 268, $iStyle, $WS_EX_CLIENTEDGE)
	GUISetState()

	_GUICtrlTreeView_BeginUpdate($hTreeView)
	For $x = 0 To 9
		$hItem[$x] = GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", $x), $hTreeView)
		For $y = 0 To Random(1, 20, 1)
			GUICtrlCreateTreeViewItem(StringFormat("[%02d] New Item", $y), $hItem[$x])
		Next
	Next
	_GUICtrlTreeView_EndUpdate($hTreeView)

	$iRand = Random(0, 9)
	MsgBox(4160, "Information", StringFormat("Child Count for item index %d: %d", $iRand, _GUICtrlTreeView_GetChildCount($hTreeView, $hItem[$iRand])))
	_GUICtrlTreeView_Expand($hTreeView, $hItem[$iRand])

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
