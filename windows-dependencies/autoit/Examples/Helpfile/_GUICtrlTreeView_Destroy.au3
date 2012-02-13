#include <GUIConstantsEx.au3>
#include <GuiTreeView.au3>
#include <WindowsConstants.au3>

$Debug_TV = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()

	Local $GUI, $hItem, $hTreeView
	Local $iStyle = BitOR($TVS_EDITLABELS, $TVS_HASBUTTONS, $TVS_HASLINES, $TVS_LINESATROOT, $TVS_DISABLEDRAGDROP, $TVS_SHOWSELALWAYS, $TVS_CHECKBOXES)

	$GUI = GUICreate("(UDF Created) TreeView Destroy", 400, 300)

	$hTreeView = _GUICtrlTreeView_Create($GUI, 2, 2, 396, 268, $iStyle, $WS_EX_CLIENTEDGE)
	GUISetState()

	_GUICtrlTreeView_BeginUpdate($hTreeView)
	For $x = 1 To Random(2, 10, 1)
		$hItem = _GUICtrlTreeView_Add($hTreeView, 0, StringFormat("[%02d] New Item", $x))
		For $y = 1 To Random(2, 10, 1)
			_GUICtrlTreeView_AddChild($hTreeView, $hItem, StringFormat("[%02d] New Child", $y))
		Next
	Next
	_GUICtrlTreeView_EndUpdate($hTreeView)

	MsgBox(4160, "Information", "Destroy TreeView Control")
	_GUICtrlTreeView_Destroy($hTreeView)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
