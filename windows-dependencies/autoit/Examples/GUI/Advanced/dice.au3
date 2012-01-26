; autoit version: 3.0
; language:       English
; author:         Larry Bailey
; email:          psichosis@tvn.net
; Date: November 15, 2004
;
; Script Function
; Creates a GUI based dice rolling program
; using the Random function

#include <GUIConstantsEx.au3>
#include <ButtonConstants.au3>
#include <StaticConstants.au3>

_Main()

Func _Main()
	Local $button1, $button2, $button3, $button4, $button5
	Local $button6, $button7, $button8, $button9, $button10
	Local $output, $die, $msg, $results
	GUICreate("Dice Roller", 265, 150, -1, -1)

	$button1 = GUICtrlCreateButton("D2", 5, 25, 50, 30)
	$button2 = GUICtrlCreateButton("D3", 65, 25, 50, 30)
	$button3 = GUICtrlCreateButton("D4", 125, 25, 50, 30)
	$button4 = GUICtrlCreateButton("D6", 5, 65, 50, 30)
	$button5 = GUICtrlCreateButton("D8", 65, 65, 50, 30)
	$button6 = GUICtrlCreateButton("D10", 125, 65, 50, 30)
	$button7 = GUICtrlCreateButton("D12", 5, 105, 50, 30)
	$button8 = GUICtrlCreateButton("D20", 65, 105, 50, 30)
	$button9 = GUICtrlCreateButton("D100", 125, 105, 50, 30)
	$button10 = GUICtrlCreateButton("Clear Dice", 185, 105, 65, 30)
	$output = GUICtrlCreateLabel("", 185, 45, 70, 50, BitOR($BS_PUSHLIKE, $SS_CENTER))
	$die = GUICtrlCreateLabel("", 185, 25, 70, 20, 0x1000)
	GUICtrlSetFont($output, 24, 800, "", "Comic Sans MS")

	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		$msg = GUIGetMsg()
		Select
			Case $msg = $button1
				$results = Random(1, 2, 1)
				GUICtrlSetData($output, $results)
				GUICtrlSetData($die, "2 Sided Die")
			Case $msg = $button2
				$results = Random(1, 3, 1)
				GUICtrlSetData($output, $results)
				GUICtrlSetData($die, "3 Sided Die")
			Case $msg = $button3
				$results = Random(1, 4, 1)
				GUICtrlSetData($output, $results)
				GUICtrlSetData($die, "4 Sided Die")
			Case $msg = $button4
				$results = Random(1, 6, 1)
				GUICtrlSetData($output, $results)
				GUICtrlSetData($die, "6 Sided Die")
			Case $msg = $button5
				$results = Random(1, 8, 1)
				GUICtrlSetData($output, $results)
				GUICtrlSetData($die, "8 Sided Die")
			Case $msg = $button6
				$results = Random(1, 10, 1)
				GUICtrlSetData($output, $results)
				GUICtrlSetData($die, "10 Sided Die")
			Case $msg = $button7
				$results = Random(1, 12, 1)
				GUICtrlSetData($output, $results)
				GUICtrlSetData($die, "12 Sided Die")
			Case $msg = $button8
				$results = Random(1, 20, 1)
				GUICtrlSetData($output, $results)
				GUICtrlSetData($die, "20 Sided Die")
			Case $msg = $button9
				$results = Random(1, 100, 1)
				GUICtrlSetData($output, $results)
				GUICtrlSetData($die, "100 Sided Die")
			Case $msg = $button10
				GUICtrlSetData($output, "")
				GUICtrlSetData($die, "")
		EndSelect
		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
EndFunc   ;==>_Main
