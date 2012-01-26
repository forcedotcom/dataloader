#include <GUIConstantsEx.au3>
#include <StaticConstants.au3>

Example()

Func Example()
	Local $defaultstatus = "Ready", $filemenu, $fileitem
	Local $helpmenu, $infoitem, $exititem, $recentfilesmenu
	Local $viewmenu, $viewstatusitem, $cancelbutton
	Local $statuslabel, $msg, $file

	GUICreate("My GUI menu", 300, 200)


	$filemenu = GUICtrlCreateMenu("&File")
	$fileitem = GUICtrlCreateMenuItem("Open", $filemenu)
	GUICtrlSetState(-1, $GUI_DEFBUTTON)
	$helpmenu = GUICtrlCreateMenu("?")
	GUICtrlCreateMenuItem("Save", $filemenu)
	GUICtrlSetState(-1, $GUI_DISABLE)
	$infoitem = GUICtrlCreateMenuItem("Info", $helpmenu)
	$exititem = GUICtrlCreateMenuItem("Exit", $filemenu)
	$recentfilesmenu = GUICtrlCreateMenu("Recent Files", $filemenu, 1)

	GUICtrlCreateMenuItem("", $filemenu, 2) ; create a separator line

	$viewmenu = GUICtrlCreateMenu("View", -1, 1) ; is created before "?" menu
	$viewstatusitem = GUICtrlCreateMenuItem("Statusbar", $viewmenu)
	GUICtrlSetState(-1, $GUI_CHECKED)
	GUICtrlCreateButton("OK", 50, 130, 70, 20)
	GUICtrlSetState(-1, $GUI_FOCUS)
	$cancelbutton = GUICtrlCreateButton("Cancel", 180, 130, 70, 20)

	$statuslabel = GUICtrlCreateLabel($defaultstatus, 0, 165, 300, 16, BitOR($SS_SIMPLE, $SS_SUNKEN))

	GUISetState()
	While 1
		$msg = GUIGetMsg()

		If $msg = $fileitem Then
			$file = FileOpenDialog("Choose file...", @TempDir, "All (*.*)")
			If @error <> 1 Then GUICtrlCreateMenuItem($file, $recentfilesmenu)
		EndIf
		If $msg = $viewstatusitem Then
			If BitAND(GUICtrlRead($viewstatusitem), $GUI_CHECKED) = $GUI_CHECKED Then
				GUICtrlSetState($viewstatusitem, $GUI_UNCHECKED)
				GUICtrlSetState($statuslabel, $GUI_HIDE)
			Else
				GUICtrlSetState($viewstatusitem, $GUI_CHECKED)
				GUICtrlSetState($statuslabel, $GUI_SHOW)
			EndIf
		EndIf
		If $msg = $GUI_EVENT_CLOSE Or $msg = $cancelbutton Or $msg = $exititem Then ExitLoop
		If $msg = $infoitem Then MsgBox(0, "Info", "Only a test...")
	WEnd
	GUIDelete()
EndFunc   ;==>Example
