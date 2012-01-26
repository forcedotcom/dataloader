#include <GUIConstantsEx.au3>
#include <GuiSlider.au3>

$Debug_S = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $rdoBottom, $rdoLeft, $rdoRight, $rdoTop, $hSlider, $hSlider2

	; Create GUI
	GUICreate("Slider Set Tip Side", 400, 296)
	$hSlider = GUICtrlCreateSlider(2, 2, 375, 20, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS))
	$hSlider2 = GUICtrlCreateSlider(380, 2, 20, 292, BitOR($TBS_TOOLTIPS, $TBS_AUTOTICKS, $TBS_VERT))
	GUISetState()

	GUICtrlCreateGroup("Tip Side Horiz", 2, 25, 120, 120)
	$rdoBottom = GUICtrlCreateRadio("Bottom", 5, 40, 108, 20)
	$rdoTop = GUICtrlCreateRadio("Top", 5, 115, 108, 20)
	GUICtrlCreateGroup("", -99, -99, 1, 1)
	GUICtrlSetState($rdoTop, $GUI_CHECKED)

	GUICtrlCreateGroup("Tip Side Vert", 130, 25, 120, 120)
	$rdoLeft = GUICtrlCreateRadio("Left", 132, 65, 108, 20)
	$rdoRight = GUICtrlCreateRadio("Right", 132, 90, 108, 20)
	GUICtrlCreateGroup("", -99, -99, 1, 1)
	GUICtrlSetState($rdoLeft, $GUI_CHECKED)

	; Loop until user exits
	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
			Case $rdoBottom
				_GUICtrlSlider_SetTipSide($hSlider, $TBTS_BOTTOM)
			Case $rdoLeft
				_GUICtrlSlider_SetTipSide($hSlider2, $TBTS_LEFT)
			Case $rdoRight
				_GUICtrlSlider_SetTipSide($hSlider2, $TBTS_RIGHT)
			Case $rdoTop
				_GUICtrlSlider_SetTipSide($hSlider, $TBTS_TOP)
		EndSwitch
	WEnd
	GUIDelete()
EndFunc   ;==>_Main
