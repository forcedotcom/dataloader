#include <GUIConstantsEx.au3>
#include <GuiAVI.au3>

$Debug_AVI = False ; Check ClassName being passed to AVI functions, set to True and use a handle to another control to see it work

Global $hAVI

_Main()

Func _Main()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("(External) AVI OpenEx", 300, 100)
	$hAVI = _GUICtrlAVI_Create($hGUI, "", -1, 10, 10)
	GUISetState()

	; Play the sample AutoIt AVI
	_GUICtrlAVI_OpenEx($hAVI, @SystemDir & "\Shell32.dll", 150)

	; Play the sample AutoIt AVI
	_GUICtrlAVI_Play($hAVI)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	; Close AVI clip
	_GUICtrlAVI_Close($hAVI)


	GUIDelete()
EndFunc   ;==>_Main
