; AutoIt GUI Example
; Created: 17/01/2005 - CyberSlug
; Modifed: 05/01/2011 - guinness

#include <GuiConstantsEx.au3>
#include <AVIConstants.au3>
#include <TreeViewConstants.au3>

; GUI
GUICreate("Sample GUI", 400, 400)
GUISetIcon(@SystemDir & "\mspaint.exe", 0)


; MENU
GUICtrlCreateMenu("Menu&One")
GUICtrlCreateMenu("Menu&Two")
GUICtrlCreateMenu("MenuTh&ree")
GUICtrlCreateMenu("Menu&Four")

; CONTEXT MENU
Local $iContextMenu = GUICtrlCreateContextMenu()
GUICtrlCreateMenuItem("Context Menu", $iContextMenu)
GUICtrlCreateMenuItem("", $iContextMenu) ; Separator
GUICtrlCreateMenuItem("&Properties", $iContextMenu)

; PIC
GUICtrlCreatePic("logo4.gif", 0, 0, 169, 68)
GUICtrlCreateLabel("Sample Pic", 75, 1, 53, 15)
GUICtrlSetBkColor(-1, $GUI_BKCOLOR_TRANSPARENT)
GUICtrlSetColor(-1, 0xFFFFFF)


; AVI
GUICtrlCreateAvi("SampleAVI.avi", 0, 180, 10, 32, 32, $ACS_AUTOPLAY)
GUICtrlCreateLabel("Sample avi", 175, 50)


; TAB
GUICtrlCreateTab(240, 0, 150, 70)
GUICtrlCreateTabItem("One")
GUICtrlCreateLabel("Sample Tab with tabItems", 250, 40)
GUICtrlCreateTabItem("Two")
GUICtrlCreateTabItem("Three")
GUICtrlCreateTabItem("")

; COMBO
GUICtrlCreateCombo("Sample Combo", 250, 80, 120, 100)

; PROGRESS
GUICtrlCreateProgress(60, 80, 150, 20)
GUICtrlSetData(-1, 60)
GUICtrlCreateLabel("Progress:", 5, 82)

; EDIT
GUICtrlCreateEdit(@CRLF & "  Sample Edit Control", 10, 110, 150, 70)

; LIST
GUICtrlCreateList("", 5, 190, 100, 90)
GUICtrlSetData(-1, "A.Sample|B.List|C.Control|D.Here", "B.List")

; ICON
GUICtrlCreateIcon("explorer.exe", 0, 175, 120)
GUICtrlCreateLabel("Icon", 180, 160, 50, 20)

; LIST VIEW
Local $iListView = GUICtrlCreateListView("Sample|ListView|", 110, 190, 110, 80)
GUICtrlCreateListViewItem("A|One", $iListView)
GUICtrlCreateListViewItem("B|Two", $iListView)
GUICtrlCreateListViewItem("C|Three", $iListView)

; GROUP WITH RADIO BUTTONS
GUICtrlCreateGroup("Sample Group", 230, 120)
GUICtrlCreateRadio("Radio One", 250, 140, 80)
GUICtrlSetState(-1, $GUI_CHECKED)
GUICtrlCreateRadio("Radio Two", 250, 165, 80)
GUICtrlCreateGroup("", -99, -99, 1, 1) ;close group

; UPDOWN
GUICtrlCreateLabel("UpDown", 350, 115)
GUICtrlCreateInput("42", 350, 130, 40, 20)
GUICtrlCreateUpdown(-1)

; LABEL
GUICtrlCreateLabel("Green" & @CRLF & "Label", 350, 165, 40, 40)
GUICtrlSetBkColor(-1, 0x00FF00)

; SLIDER
GUICtrlCreateLabel("Slider:", 235, 215)
GUICtrlCreateSlider(270, 210, 120, 30)
GUICtrlSetData(-1, 30)

; INPUT
GUICtrlCreateInput("Sample Input Box", 235, 255, 130, 20)

; DATE
GUICtrlCreateDate("", 5, 280, 200, 20)
GUICtrlCreateLabel("(Date control expands into a calendar)", 10, 305, 200, 20)

; BUTTON
GUICtrlCreateButton("Sample Button", 10, 330, 100, 30)

; CHECKBOX
GUICtrlCreateCheckbox("Checkbox", 130, 335, 80, 20)
GUICtrlSetState(-1, $GUI_CHECKED)

; TREEVIEW ONE
Local $iTreeView_1 = GUICtrlCreateTreeView(210, 290, 80, 80)
Local $iTreeItem = GUICtrlCreateTreeViewItem("TreeView", $iTreeView_1)
GUICtrlCreateTreeViewItem("Item1", $iTreeItem)
GUICtrlCreateTreeViewItem("Item2", $iTreeItem)
GUICtrlCreateTreeViewItem("Foo", -1)
GUICtrlSetState($iTreeItem, $GUI_EXPAND)

; TREEVIEW TWO
Local $iTreeView_2 = GUICtrlCreateTreeView(295, 290, 103, 80, $TVS_CHECKBOXES)
GUICtrlCreateTreeViewItem("TreeView", $iTreeView_2)
GUICtrlCreateTreeViewItem("With", $iTreeView_2)
GUICtrlCreateTreeViewItem("$TVS_CHECKBOXES", $iTreeView_2)
GUICtrlSetState(-1, $GUI_CHECKED)
GUICtrlCreateTreeViewItem("Style", $iTreeView_2)


; GUI MESSAGE LOOP
GUISetState(@SW_SHOW)
While 1
	Switch GUIGetMsg()
		Case $GUI_EVENT_CLOSE
			Exit

	EndSwitch
WEnd
