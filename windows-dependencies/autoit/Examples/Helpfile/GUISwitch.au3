#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $parent1, $parent2, $tabitem, $msg

	$parent1 = GUICreate("Parent1")
	GUICtrlCreateTab(10, 10)
	$tabitem = GUICtrlCreateTabItem("tab1")
	GUICtrlCreateTabItem("tab2")
	GUICtrlCreateTabItem("")

	$parent2 = GUICreate("Parent2", -1, -1, 100, 100)

	GUISwitch($parent2)
	GUISetState()
	Do
		$msg = GUIGetMsg()
	Until $msg = $GUI_EVENT_CLOSE

	GUISwitch($parent1, $tabitem)
	GUICtrlCreateButton("OK", 50, 50, 50)
	GUICtrlCreateTabItem("")

	GUISetState(@SW_SHOW, $parent1)
	Do
		$msg = GUIGetMsg()
	Until $msg = $GUI_EVENT_CLOSE
EndFunc   ;==>Example
