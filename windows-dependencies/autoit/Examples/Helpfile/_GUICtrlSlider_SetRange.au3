#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $aRange, $hSlider

	; Create GUI
	GUICreate("Slider Set Range", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	GUISetState()

	; Get Range
	$aRange = _GUICtrlSlider_GetRange($hSlider)
	MsgBox(4160, "Information", StringFormat("Range: %d - %d", $aRange[0], $aRange[1]))

	; Set Range
	_GUICtrlSlider_SetRange($hSlider, 20, 50)

	; Get Range
	$aRange = _GUICtrlSlider_GetRange($hSlider)
	MsgBox(4160, "Information", StringFormat("Range: %d - %d", $aRange[0], $aRange[1]))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
