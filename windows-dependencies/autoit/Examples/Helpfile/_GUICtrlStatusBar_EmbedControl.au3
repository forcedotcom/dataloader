#include <GUIConstantsEx.au3>
#include <GuiStatusBar.au3>
#include <ProgressConstants.au3>
#include <SendMessage.au3>

$Debug_SB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()

	Local $hGUI, $hProgress, $hInput, $input, $progress, $hStatus
	Local $aParts[4] = [80, 160, 300, -1]

	; Create GUI
	$hGUI = GUICreate("StatusBar Embed Control", 400, 300)

	;===============================================================================
	; defaults to 1 part, no text
	$hStatus = _GUICtrlStatusBar_Create($hGUI)
	_GUICtrlStatusBar_SetMinHeight($hStatus, 20)

	;===============================================================================
	GUISetState()

	; Initialize parts
	_GUICtrlStatusBar_SetParts($hStatus, $aParts)
	_GUICtrlStatusBar_SetText($hStatus, "Part 1")
	_GUICtrlStatusBar_SetText($hStatus, "Part 2", 1)

	; Embed a progress bar
	If @OSType = "WIN32_WINDOWS" Then
		$progress = GUICtrlCreateProgress(0, 0, -1, -1, $PBS_SMOOTH)
		$hProgress = GUICtrlGetHandle($progress)
		_GUICtrlStatusBar_EmbedControl($hStatus, 2, $hProgress)
	Else
		$progress = GUICtrlCreateProgress(0, 0, -1, -1, $PBS_MARQUEE) ; marquee works on Win XP and above
		$hProgress = GUICtrlGetHandle($progress)
		_GUICtrlStatusBar_EmbedControl($hStatus, 2, $hProgress)
		_SendMessage($hProgress, $PBM_SETMARQUEE, True, 200) ; marquee works on Win XP and above
	EndIf

	$input = GUICtrlCreateInput("This is Embeded", 0, 0)
	$hInput = GUICtrlGetHandle($input)
	_GUICtrlStatusBar_EmbedControl($hStatus, 3, $hInput, 3)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
