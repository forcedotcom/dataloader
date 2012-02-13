#include <GUIConstantsEx.au3>
#include <GuiAVI.au3>

$Debug_AVI = False ; Check ClassName being passed to AVI functions, set to True and use a handle to another control to see it work

Global $hAVI

_Example_Internal()
_Example_External()

Func _Example_Internal()
	; Create GUI
	GUICreate("(Internal) AVI Seek", 300, 100)
	$hAVI = GUICtrlCreateAvi(@SystemDir & "\shell32.dll", 160, 10, 10)
	GUISetState()

	; Loop until user exits
	Do
		Sleep(100)
		; Seek to a random frame in the AVI Clip
		_GUICtrlAVI_Seek($hAVI, Random(1, 30, 1))
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	; Close AVI clip
	_GUICtrlAVI_Close($hAVI)


	GUIDelete()
EndFunc   ;==>_Example_Internal

Func _Example_External()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("(External) AVI Seek", 300, 100)
	$hAVI = _GUICtrlAVI_Create($hGUI, @SystemDir & "\Shell32.dll", 160, 10, 10)
	GUISetState()

	; Loop until user exits
	Do
		Sleep(100)
		; Seek to a random frame in the AVI Clip
		_GUICtrlAVI_Seek($hAVI, Random(1, 30, 1))
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

	; Close AVI clip
	_GUICtrlAVI_Close($hAVI)


	GUIDelete()
EndFunc   ;==>_Example_External
