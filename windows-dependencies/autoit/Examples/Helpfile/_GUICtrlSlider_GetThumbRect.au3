#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $aRect, $hSlider

	; Create GUI
	GUICreate("Slider Get Thumb Rect", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 396, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_ENABLESELRANGE))
	GUISetState()

	; Get Thumb Rect
	$aRect = _GUICtrlSlider_GetThumbRect($hSlider)

	MsgBox(4160, "Information", StringFormat("[%d][%d][%d][%d]", $aRect[0], $aRect[1], $aRect[2], $aRect[3]))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
