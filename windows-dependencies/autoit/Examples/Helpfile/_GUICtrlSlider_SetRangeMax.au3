#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hSlider

	; Create GUI
	GUICreate("Slider Set Range Max", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	GUISetState()

	; Get Range Max
	MsgBox(4160, "Information", "Range Max: " & _GUICtrlSlider_GetRangeMax($hSlider))

	; Set Range Max
	_GUICtrlSlider_SetRangeMax($hSlider, 50)

	; Get Range Max
	MsgBox(4160, "Information", "Range Max: " & _GUICtrlSlider_GetRangeMax($hSlider))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
