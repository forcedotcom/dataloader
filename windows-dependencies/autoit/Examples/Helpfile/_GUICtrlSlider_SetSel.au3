#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $aSel, $hSlider

	; Create GUI
	GUICreate("Slider Set Sel", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	GUISetState()

	; Set Sel
	_GUICtrlSlider_SetSel($hSlider, 10, 50)

	; Get Sel
	$aSel = _GUICtrlSlider_GetSel($hSlider)
	MsgBox(4160, "Information", StringFormat("Sel: %d - %d", $aSel[0], $aSel[1]))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
