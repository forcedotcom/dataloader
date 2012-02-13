#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $tRect, $hSlider

	; Create GUI
	GUICreate("Slider Get Channel RectEx", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	GUISetState()

	; Get Channel Rect
	$tRect = _GUICtrlSlider_GetChannelRectEx($hSlider)

	MsgBox(4160, "Information", StringFormat("[%d][%d][%d][%d]", _
			DllStructGetData($tRect, "Left"), DllStructGetData($tRect, "Top"), _
			DllStructGetData($tRect, "Right"), DllStructGetData($tRect, "Bottom")))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
