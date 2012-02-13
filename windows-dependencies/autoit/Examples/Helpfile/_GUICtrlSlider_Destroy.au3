#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hGUI, $hSlider

	; Create GUI
	$hGUI = GUICreate("(UDF Created) Slider Destroy", 400, 296)
	$hSlider = _GUICtrlSlider_Create($hGUI, 2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS))
	GUISetState()

	MsgBox(4160, "Information", "Destroy Slider")
	_GUICtrlSlider_Destroy($hSlider)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
