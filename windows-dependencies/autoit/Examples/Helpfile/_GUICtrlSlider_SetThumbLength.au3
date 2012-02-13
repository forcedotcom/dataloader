#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hSlider

	; Create GUI
	GUICreate("Slider Set Thumb Length", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 25, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_FIXEDLENGTH))
	GUISetState()

	; Get Thumb Length
	MsgBox(4160, "Information", "Thumb Length: " & _GUICtrlSlider_GetThumbLength($hSlider))

	; Set Thumb Length
	_GUICtrlSlider_SetThumbLength($hSlider, 10)

	; Get Thumb Length
	MsgBox(4160, "Information", "Thumb Length: " & _GUICtrlSlider_GetThumbLength($hSlider))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
