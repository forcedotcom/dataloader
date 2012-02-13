#include <GuiToolbar.au3>
#include <GUIConstantsEx.au3>

$Debug_TB = False ; Check ClassName being passed to functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hGUI, $hToolbar
	Local Enum $idRed = 1000, $idGreen, $idBlue

	; Create GUI
	$hGUI = GUICreate("Toolbar", 400, 300)
	$hToolbar = _GUICtrlToolbar_Create($hGUI)
	GUISetState()

	; Add bitmaps
	_GUICtrlToolbar_LoadBitmap($hToolbar, @ScriptDir & "\images\Red.bmp")
	_GUICtrlToolbar_LoadBitmap($hToolbar, @ScriptDir & "\Images\Green.bmp")
	_GUICtrlToolbar_LoadBitmap($hToolbar, @ScriptDir & "\Images\Blue.bmp")

	; Add buttons
	_GUICtrlToolbar_AddButton($hToolbar, $idRed, 0)
	_GUICtrlToolbar_AddButton($hToolbar, $idGreen, 1)
	_GUICtrlToolbar_AddButton($hToolbar, $idBlue, 2)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE

EndFunc   ;==>_Main
