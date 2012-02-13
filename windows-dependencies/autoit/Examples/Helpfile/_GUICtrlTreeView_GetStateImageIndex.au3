#include <GUIConstantsEx.au3>
#include <GuiTreeView.au3>
#include <GuiImageList.au3>
#include <WindowsConstants.au3>

$Debug_TV = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

Global $hImage

_Main()

Func _Main()

	Local $hItem[10], $hChildItem[30], $iYItem = 0, $hTreeView
	Local $iStyle = BitOR($TVS_EDITLABELS, $TVS_HASBUTTONS, $TVS_HASLINES, $TVS_LINESATROOT, $TVS_DISABLEDRAGDROP, $TVS_SHOWSELALWAYS)

	GUICreate("TreeView Get State Image Index", 400, 300)

	$hTreeView = GUICtrlCreateTreeView(2, 2, 396, 268, $iStyle, $WS_EX_CLIENTEDGE)
	GUISetState()

	_CreateStateImageList()
	_GUICtrlTreeView_SetStateImageList($hTreeView, $hImage)

	_GUICtrlTreeView_BeginUpdate($hTreeView)
	For $x = 0 To 9
		$hItem[$x] = _GUICtrlTreeView_Add($hTreeView, 0, StringFormat("[%02d] New Item", $x))
		_GUICtrlTreeView_SetStateImageIndex($hTreeView, $hItem[$x], 1)
		For $y = 1 To 3
			$hChildItem[$iYItem] = _GUICtrlTreeView_AddChild($hTreeView, $hItem[$x], StringFormat("[%02d] New Child", $y))
			_GUICtrlTreeView_SetStateImageIndex($hTreeView, $hChildItem[$iYItem], 1)
			$iYItem += 1
		Next
	Next
	_GUICtrlTreeView_EndUpdate($hTreeView)

	_GUICtrlTreeView_SelectItem($hTreeView, $hItem[0])
	_GUICtrlTreeView_SetStateImageIndex($hTreeView, $hItem[0], 2)

	MsgBox(4160, "Information", "State Image Index for Item 0: " & _GUICtrlTreeView_GetStateImageIndex($hTreeView, $hItem[0]))
	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

Func _CreateStateImageList()
	$hImage = _GUIImageList_Create(16, 16, 5, 3)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 3)
	_GUIImageList_AddIcon($hImage, "shell32.dll", 4)
EndFunc   ;==>_CreateStateImageList
