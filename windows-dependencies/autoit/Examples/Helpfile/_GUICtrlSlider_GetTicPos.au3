#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $iTic = Random(1, 99, 1), $hSlider

	; Create GUI
	GUICreate("Slider Get Tic Pos", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	GUISetState()

	; Get Tic Pos
	MsgBox(4160, "Information", StringFormat("Tic %d: X Pos: %d", $iTic, _GUICtrlSlider_GetTicPos($hSlider, $iTic)))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
