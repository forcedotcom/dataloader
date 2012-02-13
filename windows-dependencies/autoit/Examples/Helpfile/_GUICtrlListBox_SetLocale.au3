#include <GuiListBox.au3>
#include <WinAPI.au3>
#include <GUIConstantsEx.au3>
#include <Constants.au3>

$Debug_LB = False ; Check ClassName being passed to ListBox functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $iLocale, $hListBox

	; Create GUI
	GUICreate("List Box Set Locale", 400, 296)
	$hListBox = GUICtrlCreateList("", 2, 2, 396, 296)
	GUISetState()

	$iLocale = _WinAPI_MAKELCID(_WinAPI_MAKELANGID($LANG_DUTCH, $SUBLANG_DUTCH), $SORT_DEFAULT)

	MsgBox(4160, "Information", "Previous Locale: " & _GUICtrlListBox_SetLocale($hListBox, $iLocale))

	$iLocale = _WinAPI_MAKELCID(_WinAPI_MAKELANGID($LANG_ENGLISH, $SUBLANG_ENGLISH_US), $SORT_DEFAULT)

	MsgBox(4160, "Information", "Previous Locale: " & _GUICtrlListBox_SetLocale($hListBox, $iLocale))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
