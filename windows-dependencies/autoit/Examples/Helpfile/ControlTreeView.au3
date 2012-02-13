; Author:         Zedna

#include <GUIConstantsEx.au3>
#include <TreeViewConstants.au3>
#include <WindowsConstants.au3>

Local $gui = GUICreate("ControlTreeview test", 212, 212)
Local $treeview = GUICtrlCreateTreeView(6, 6, 200, 160, BitOR($TVS_HASBUTTONS, $TVS_HASLINES, $TVS_LINESATROOT, $TVS_CHECKBOXES), $WS_EX_CLIENTEDGE)
Local $h_tree = ControlGetHandle($gui, "", $treeview)

Local $root = GUICtrlCreateTreeViewItem("Root", $treeview)
GUICtrlCreateTreeViewItem("Item 1", $root)
GUICtrlCreateTreeViewItem("Item 2", $root)
GUICtrlCreateTreeViewItem("Item 3", $root)
Local $item4 = GUICtrlCreateTreeViewItem("Item 4", $root)
GUICtrlCreateTreeViewItem("Item 41", $item4)
GUICtrlCreateTreeViewItem("Item 42", $item4)
GUICtrlCreateTreeViewItem("Item 5", $root)

GUISetState(@SW_SHOW)

; some examples
ControlTreeView($gui, "", $h_tree, "Expand", "Root")

ControlTreeView($gui, "", $h_tree, "Exists", "Root|Item 4")
ControlTreeView($gui, "", $h_tree, "Check", "Root|Item 4")
ControlTreeView($gui, "", $h_tree, "Select", "Root|Item 4")
ControlTreeView($gui, "", $h_tree, "Expand", "Root|Item 4")

While 1
	Local $msg = GUIGetMsg()
	Select
		Case $msg = $GUI_EVENT_CLOSE
			ExitLoop
	EndSelect
WEnd
