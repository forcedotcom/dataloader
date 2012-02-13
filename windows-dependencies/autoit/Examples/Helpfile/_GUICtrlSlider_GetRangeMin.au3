#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hSlider

	; Create GUI
	GUICreate("Slider Get Range Min", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	GUISetState()

	; Get Range Min
	MsgBox(4160, "Information", "Range Min: " & _GUICtrlSlider_GetRangeMin($hSlider))

	; Set Range Min
	_GUICtrlSlider_SetRangeMin($hSlider, 20)

	; Get Range Min
	MsgBox(4160, "Information", "Range Min: " & _GUICtrlSlider_GetRangeMin($hSlider))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
