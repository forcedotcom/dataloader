#include <GUIConstantsEx.au3>
#include <GuiTreeView.au3>
#include <GuiImageList.au3>
#include <WindowsConstants.au3>

$Debug_TV = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()

	Local $hItem[10], $hImage, $iImage, $iSImage, $hTreeView
	Local $iStyle = BitOR($TVS_EDITLABELS, $TVS_HASBUTTONS, $TVS_HASLINES, $TVS_LINESATROOT, $TVS_DISABLEDRAGDROP, $TVS_SHOWSELALWAYS, $TVS_CHECKBOXES)

	GUICreate("TreeView Set Selected Image Index", 400, 300)

	$hTreeView = GUICtrlCreateTreeView(2, 2, 396, 268, $iStyle, $WS_EX_CLIENTEDGE)
	GUISetState()

	$hImage = _GUIImageList_Create(16, 16, 5, 3)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 110)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 131)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 165)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 168)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 137)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 146)
	_GUICtrlTreeView_SetNormalImageList($hTreeView, $hImage)

	_GUICtrlTreeView_BeginUpdate($hTreeView)
	For $x = 0 To 5
		$hItem[$x] = _GUICtrlTreeView_Add($hTreeView, 0, StringFormat("[%02d] New Item", $x), $x, $x)
		For $y = 1 To Random(2, 10, 1)
			$iImage = Random(0, 5, 1)
			$iSImage = Random(0, 5, 1)
			_GUICtrlTreeView_AddChild($hTreeView, $hItem[$x], StringFormat("[%02d] New Child", $y), $iImage, $iSImage)
		Next
	Next
	_GUICtrlTreeView_EndUpdate($hTreeView)

	_GUICtrlTreeView_SelectItem($hTreeView, $hItem[2])
	MsgBox(4160, "Information", StringFormat("Selected Image Index for Item Index 2? %d", _GUICtrlTreeView_GetSelectedImageIndex($hTreeView, $hItem[2])))
	_GUICtrlTreeView_SetSelectedImageIndex($hTreeView, $hItem[2], 1)
	MsgBox(4160, "Information", StringFormat("Selected Image Index for Item Index 2? %d", _GUICtrlTreeView_GetSelectedImageIndex($hTreeView, $hItem[2])))
	_GUICtrlTreeView_SelectItem($hTreeView, $hItem[2])

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
