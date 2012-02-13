#include <GuiMenu.au3>
#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <WindowsConstants.au3>

Global Enum $idOpen = 1000, $idSave, $idInfo

_Main()

Func _Main()
	; Create GUI
	GUICreate("Menu", 400, 300)
	GUISetState()

	; Register message handlers
	GUIRegisterMsg($WM_COMMAND, "WM_COMMAND")
	GUIRegisterMsg($WM_CONTEXTMENU, "WM_CONTEXTMENU")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Handle WM_COMMAND messages
Func WM_COMMAND($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $ilParam
	Switch $iwParam
		Case $idOpen
			_WinAPI_ShowMsg("Open")
		Case $idSave
			_WinAPI_ShowMsg("Save")
		Case $idInfo
			_WinAPI_ShowMsg("Info")
	EndSwitch
EndFunc   ;==>WM_COMMAND

; Handle WM_CONTEXTMENU messages
Func WM_CONTEXTMENU($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $ilParam
	Local $hMenu

	$hMenu = _GUICtrlMenu_CreatePopup()
	_GUICtrlMenu_InsertMenuItem($hMenu, 0, "Open", $idOpen)
	_GUICtrlMenu_InsertMenuItem($hMenu, 1, "Save", $idSave)
	_GUICtrlMenu_InsertMenuItem($hMenu, 3, "", 0)
	_GUICtrlMenu_InsertMenuItem($hMenu, 3, "Info", $idInfo)
	_GUICtrlMenu_TrackPopupMenu($hMenu, $iwParam)
	_GUICtrlMenu_DestroyMenu($hMenu)
	Return True
EndFunc   ;==>WM_CONTEXTMENU
