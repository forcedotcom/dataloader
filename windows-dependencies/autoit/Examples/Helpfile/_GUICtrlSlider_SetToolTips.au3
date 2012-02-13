#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hWndTT, $hSlider

	; Create GUI
	GUICreate("Slider Set Tool Tips", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	GUISetState()

	; Get Tool Tips
	$hWndTT = _GUICtrlSlider_GetToolTips($hSlider)
	MsgBox(4160, "Information", "Tool Tip Handle: " & $hWndTT)

	; Set Tool Tips
	_GUICtrlSlider_SetToolTips($hSlider, $hWndTT)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
