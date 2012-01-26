#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hInput, $hInput2, $hSlider

	; Create GUI
	GUICreate("Slider Get Buddy", 400, 296)
	$hSlider = GUICtrlCreateSlider(95, 2, 205, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	$hInput = GUICtrlCreateInput("0", 2, 25, 90, 20)
	$hInput2 = GUICtrlCreateInput("0", 2, 25, 90, 20)
	GUISetState()

	; Set buddy to left
	_GUICtrlSlider_SetBuddy($hSlider, True, $hInput)
	; Set buddy to right
	_GUICtrlSlider_SetBuddy($hSlider, False, $hInput2)

	; Get Buddy from the left
	MsgBox(4160, "Information", "Buddy Handle: " & _GUICtrlSlider_GetBuddy($hSlider, True))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
