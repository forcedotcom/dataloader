#include <GUIConstantsEx.au3>
#include <StaticConstants.au3>

Global $MAXGr = 6, $del
Global $a[$MAXGr + 1] ; 0 and $MAXGr entries not used to allow GUICtrlDelete result

Example()

Func Example()
	Local $msg, $inc, $i

	CreateChild()

	$i = 1
	$inc = 1
	Do
		$msg = GUIGetMsg()

		If $msg = $del Then
			GUICtrlDelete($a[$i])
			$i = $i + $inc
			If $i < 0 Or $i > $MAXGr Then Exit
		EndIf
		If $msg > 0 Then MsgBox(0, "clicked", $msg & @LF & $a[5], 2)
	Until $msg = $GUI_EVENT_CLOSE
EndFunc   ;==>Example


Func CreateChild()
	GUICreate("My Draw")
	$del = GUICtrlCreateButton("Delete", 50, 165, 50)


	$a[1] = GUICtrlCreateGraphic(20, 50, 100, 100)
	GUICtrlSetBkColor(-1, 0xffffff)
	GUICtrlSetColor(-1, 0)

	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0xff0000, 0xff0000)
	GUICtrlSetGraphic(-1, $GUI_GR_PIE, 50, 50, 40, 30, 270)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0x00ff00, 0xffffff)
	GUICtrlSetGraphic(-1, $GUI_GR_PIE, 58, 50, 40, -60, 90)

	GUICtrlSetGraphic(-1, $GUI_GR_ELLIPSE, 100, 100, 50, 80)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0x00ff00, 0xc0c0ff)
	GUICtrlSetGraphic(-1, $GUI_GR_RECT, 350, 200, 50, 80)
	GUICtrlCreateLabel("label", 65, 100, 30)
	GUICtrlSetColor(-1, 0xff)


	$a[2] = GUICtrlCreateGraphic(220, 50, 100, 100)
	GUICtrlSetStyle(-1, $SS_NOTIFY)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0, 0xff)
	GUICtrlSetGraphic(-1, $GUI_GR_PIE, 50, 50, 40, 30, 270)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0x00ff00, 0xffffff)
	GUICtrlSetGraphic(-1, $GUI_GR_PIE, 58, 50, 40, -60, 90)

	$a[3] = GUICtrlCreateGraphic(220, 150, 100, 100, 0)
	GUICtrlSetBkColor(-1, 0xf08080)
	GUICtrlSetColor(-1, 0xff)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0xff00)
	GUICtrlSetGraphic(-1, $GUI_GR_RECT, 50, 50, 80, 80)

	$a[4] = GUICtrlCreateGraphic(20, 200, 80, 80)
	GUICtrlSetState(-1, $GUI_DISABLE)
	GUICtrlSetBkColor(-1, 0xffffff)
	GUICtrlSetGraphic(-1, $GUI_GR_MOVE, 10, 10)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0xff)
	GUICtrlSetGraphic(-1, $GUI_GR_LINE, 30, 40)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0xff00)
	GUICtrlSetGraphic(-1, $GUI_GR_LINE, 70, 70)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0xff0000)
	GUICtrlSetGraphic(-1, $GUI_GR_LINE, 10, 50)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0xffff00)
	GUICtrlSetGraphic(-1, $GUI_GR_LINE, 10, 10)

	$a[5] = GUICtrlCreateGraphic(150, 10, 50, 50, 0)
	GUICtrlSetBkColor(-1, 0xa0ffa0)
	GUICtrlSetGraphic(-1, $GUI_GR_MOVE, 20, 20) ; start point
	; it is better to draw line and after point
	; to avoid to switch color at each drawing
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0x0000ff)
	GUICtrlSetGraphic(-1, $GUI_GR_DOT, 30, 30)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0)
	GUICtrlSetGraphic(-1, $GUI_GR_LINE, 20, 40)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0xff0000)
	GUICtrlSetGraphic(-1, $GUI_GR_DOT, 25, 25)
	GUICtrlSetGraphic(-1, $GUI_GR_COLOR, 0)
	GUICtrlSetGraphic(-1, $GUI_GR_LINE, 40, 40)
	GUICtrlSetGraphic(-1, $GUI_GR_DOT, 40, 40)

	GUISetState()
EndFunc   ;==>CreateChild
