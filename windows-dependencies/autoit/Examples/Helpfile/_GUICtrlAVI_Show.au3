#include <GUIConstantsEx.au3>
#include <GuiAVI.au3>

$Debug_AVI = False ; Check ClassName being passed to AVI functions, set to True and use a handle to another control to see it work

Global $hAVI

_Example_Internal()
_Example_External()

Func _Example_Internal()
	Local $btn_start, $btn_stop, $btn_show

	; Create GUI
	GUICreate("(Internal) AVI Play", 350, 200)
	$hAVI = GUICtrlCreateAvi(@SystemDir & "\shell32.dll", 160, 10, 10)
	$btn_start = GUICtrlCreateButton("start", 25, 150, 70, 22)
	$btn_stop = GUICtrlCreateButton("stop", 125, 150, 70, 22)
	$btn_show = GUICtrlCreateButton("hide", 225, 150, 70, 22)
	GUISetState()

	; Loop until user exits
	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
			Case $btn_start
				; Play part of the AVI clip
				_GUICtrlAVI_Play($hAVI)
			Case $btn_stop
				; Stop AVI clip
				_GUICtrlAVI_Stop($hAVI)
			Case $btn_show
				If GUICtrlRead($btn_show) = "hide" Then
					_GUICtrlAVI_Show($hAVI, @SW_HIDE)
					GUICtrlSetData($btn_show, "show")
				Else
					_GUICtrlAVI_Show($hAVI, @SW_SHOW)
					GUICtrlSetData($btn_show, "hide")
				EndIf
		EndSwitch
	WEnd

	; Close AVI clip
	_GUICtrlAVI_Close($hAVI)

	GUIDelete()
EndFunc   ;==>_Example_Internal

Func _Example_External()
	Local $hGUI, $btn_start, $btn_stop, $btn_show

	; Create GUI
	$hGUI = GUICreate("(External) AVI Play", 350, 200)
	$hAVI = _GUICtrlAVI_Create($hGUI, @SystemDir & "\Shell32.dll", 160, 10, 10)
	$btn_start = GUICtrlCreateButton("start", 25, 150, 70, 22)
	$btn_stop = GUICtrlCreateButton("stop", 125, 150, 70, 22)
	$btn_show = GUICtrlCreateButton("hide", 225, 150, 70, 22)
	GUISetState()

	; Loop until user exits
	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
			Case $btn_start
				; Play part of the AVI clip
				_GUICtrlAVI_Play($hAVI)
			Case $btn_stop
				; Stop AVI clip
				_GUICtrlAVI_Stop($hAVI)
			Case $btn_show
				If GUICtrlRead($btn_show) = "hide" Then
					_GUICtrlAVI_Show($hAVI, @SW_HIDE)
					GUICtrlSetData($btn_show, "show")
				Else
					_GUICtrlAVI_Show($hAVI, @SW_SHOW)
					GUICtrlSetData($btn_show, "hide")
				EndIf
		EndSwitch
	WEnd

	; Close AVI clip
	_GUICtrlAVI_Close($hAVI)

	GUIDelete()
EndFunc   ;==>_Example_External
