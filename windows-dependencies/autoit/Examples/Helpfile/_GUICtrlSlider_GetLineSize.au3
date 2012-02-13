#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hSlider

	; Create GUI
	GUICreate("Slider Get Line Size", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	GUISetState()

	; Get Line Size
	MsgBox(4160, "Information", "Line Size: " & _GUICtrlSlider_GetLineSize($hSlider))

	; Set Line Size
	_GUICtrlSlider_SetLineSize($hSlider, 4)

	; Get Line Size
	MsgBox(4160, "Information", "Line Size: " & _GUICtrlSlider_GetLineSize($hSlider))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
