#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $msg

	GUICreate("My GUI Tab") ; will create a dialog box that when displayed is centered

	GUISetBkColor(0x00E0FFFF)
	GUISetFont(9, 300)

	GUICtrlCreateTab(10, 10, 200, 100)

	GUICtrlCreateTabItem("tab0")
	GUICtrlCreateLabel("label0", 30, 80, 50, 20)
	GUICtrlCreateButton("OK0", 20, 50, 50, 20)
	GUICtrlCreateInput("default", 80, 50, 70, 20)

	GUICtrlCreateTabItem("tab----1")
	GUICtrlCreateLabel("label1", 30, 80, 50, 20)
	GUICtrlCreateCombo("", 20, 50, 60, 120)
	GUICtrlSetData(-1, "Trids|CyberSlug|Larry|Jon|Tylo", "Jon") ; default Jon
	GUICtrlCreateButton("OK1", 80, 50, 50, 20)

	GUICtrlCreateTabItem("tab2")
	GUICtrlSetState(-1, $GUI_SHOW) ; will be display first
	GUICtrlCreateLabel("label2", 30, 80, 50, 20)
	GUICtrlCreateButton("OK2", 140, 50, 50)

	GUICtrlCreateTabItem("") ; end tabitem definition

	GUICtrlCreateLabel("label3", 20, 130, 50, 20)

	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>Example
