#include <GUIConstantsEx.au3>

_Main()

Func _Main()
	Local $filemenu, $fileitem, $recentfilesmenu, $separator1
	Local $exititem, $helpmenu, $aboutitem, $okbutton, $cancelbutton
	Local $msg, $file
	#forceref $separator1

	GUICreate("GUI menu", 300, 200)

	$filemenu = GUICtrlCreateMenu("File")
	$fileitem = GUICtrlCreateMenuItem("Open...", $filemenu)
	$recentfilesmenu = GUICtrlCreateMenu("Recent Files", $filemenu)
	$separator1 = GUICtrlCreateMenuItem("", $filemenu)
	$exititem = GUICtrlCreateMenuItem("Exit", $filemenu)
	$helpmenu = GUICtrlCreateMenu("?")
	$aboutitem = GUICtrlCreateMenuItem("About", $helpmenu)

	$okbutton = GUICtrlCreateButton("OK", 50, 130, 70, 20)

	$cancelbutton = GUICtrlCreateButton("Cancel", 180, 130, 70, 20)

	GUISetState()

	While 1
		$msg = GUIGetMsg()


		Select
			Case $msg = $GUI_EVENT_CLOSE Or $msg = $cancelbutton
				ExitLoop

			Case $msg = $fileitem
				$file = FileOpenDialog("Choose file...", @TempDir, "All (*.*)")
				If @error <> 1 Then GUICtrlCreateMenuItem($file, $recentfilesmenu)

			Case $msg = $exititem
				ExitLoop

			Case $msg = $okbutton
				MsgBox(0, "Click", "You clicked OK!")

			Case $msg = $aboutitem
				MsgBox(0, "About", "GUI Menu Test")
		EndSelect
	WEnd

	GUIDelete()

	Exit
EndFunc   ;==>_Main
