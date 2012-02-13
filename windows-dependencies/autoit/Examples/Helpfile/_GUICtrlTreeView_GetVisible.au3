#include <GUIConstantsEx.au3>
#include <GuiTreeView.au3>
#include <WindowsConstants.au3>

$Debug_TV = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()

	Local $hItem[100], $iRand, $hTreeView
	Local $iStyle = BitOR($TVS_EDITLABELS, $TVS_HASBUTTONS, $TVS_HASLINES, $TVS_LINESATROOT, $TVS_DISABLEDRAGDROP, $TVS_SHOWSELALWAYS)

	GUICreate("TreeView Get Visible", 400, 300)

	$hTreeView = GUICtrlCreateTreeView(2, 2, 396, 268, $iStyle, $WS_EX_CLIENTEDGE)
	GUISetState()

	_GUICtrlTreeView_BeginUpdate($hTreeView)
	For $x = 0 To 99
		$hItem[$x] = _GUICtrlTreeView_Add($hTreeView, 0, StringFormat("[%02d] New Item", $x))
	Next
	_GUICtrlTreeView_EndUpdate($hTreeView)

	$iRand = Random(40, 99, 1)
	MsgBox(4160, "Information", StringFormat("Index %d Visible: %s", $iRand, _GUICtrlTreeView_GetVisible($hTreeView, $hItem[$iRand])))
	_GUICtrlTreeView_EnsureVisible($hTreeView, $hItem[$iRand])
	MsgBox(4160, "Information", StringFormat("Index %d Visible: %s", $iRand, _GUICtrlTreeView_GetVisible($hTreeView, $hItem[$iRand])))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
