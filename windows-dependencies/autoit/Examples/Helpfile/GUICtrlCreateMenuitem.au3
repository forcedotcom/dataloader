#include <GUIConstantsEx.au3>
#include <StaticConstants.au3>

_Main()

Func _Main()
	Local $iCancel, $iExit, $iFileItem, $iFileMenu, $iHelpMenu, $iInfoItem
	Local $iRecentFilesMenu, $iStatusLabel, $iViewMenu, $iViewStatusItem, $sFilePath, $sStatus = "Ready"

	GUICreate("My GUI menu", 300, 200)

	$sStatus = "Ready"

	$iFileMenu = GUICtrlCreateMenu("&File")
	$iFileItem = GUICtrlCreateMenuItem("Open", $iFileMenu)
	GUICtrlSetState(-1, $GUI_DEFBUTTON)
	$iHelpMenu = GUICtrlCreateMenu("?")
	GUICtrlCreateMenuItem("Save", $iFileMenu)
	GUICtrlSetState(-1, $GUI_DISABLE)
	$iInfoItem = GUICtrlCreateMenuItem("Info", $iHelpMenu)
	$iExit = GUICtrlCreateMenuItem("Exit", $iFileMenu)
	$iRecentFilesMenu = GUICtrlCreateMenu("Recent Files", $iFileMenu, 1)

	GUICtrlCreateMenuItem("", $iFileMenu, 2) ; Create a separator line

	$iViewMenu = GUICtrlCreateMenu("View", -1, 1) ; Is created before "?" menu
	$iViewStatusItem = GUICtrlCreateMenuItem("Statusbar", $iViewMenu)
	GUICtrlSetState(-1, $GUI_CHECKED)
	GUICtrlCreateButton("OK", 50, 130, 70, 20)
	GUICtrlSetState(-1, $GUI_FOCUS)
	$iCancel = GUICtrlCreateButton("Cancel", 180, 130, 70, 20)

	$iStatusLabel = GUICtrlCreateLabel($sStatus, 0, 165, 300, 16, BitOR($SS_SIMPLE, $SS_SUNKEN))

	GUISetState(@SW_SHOW)

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE, $iCancel, $iExit
				Exit

			Case $iInfoItem
				MsgBox(64, "Info", "Only a test...")

			Case $iFileItem
				$sFilePath = FileOpenDialog("Choose a file...", @TempDir, "All (*.*)")
				If @error Then
					ContinueLoop
				EndIf
				GUICtrlCreateMenuItem($sFilePath, $iRecentFilesMenu)

			Case $iViewStatusItem
				If BitAND(GUICtrlRead($iViewStatusItem), $GUI_CHECKED) = $GUI_CHECKED Then
					GUICtrlSetState($iViewStatusItem, $GUI_UNCHECKED)
					GUICtrlSetState($iStatusLabel, $GUI_HIDE)
				Else
					GUICtrlSetState($iViewStatusItem, $GUI_CHECKED)
					GUICtrlSetState($iStatusLabel, $GUI_SHOW)
				EndIf
		EndSwitch
	WEnd
EndFunc   ;==>_Main
