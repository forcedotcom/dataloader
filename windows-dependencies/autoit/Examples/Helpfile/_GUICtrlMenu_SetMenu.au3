#include <GuiMenu.au3>
#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <WindowsConstants.au3>

Global $iMemo
Global Enum $idNew = 1000, $idOpen, $idSave, $idExit, $idCut, $idCopy, $idPaste, $idAbout

_Main()

Func _Main()
	Local $hGUI, $hFile, $hEdit, $hHelp, $hMain

	; Create GUI
	$hGUI = GUICreate("Menu", 400, 300)

	; Create File menu
	$hFile = _GUICtrlMenu_CreateMenu()
	_GUICtrlMenu_InsertMenuItem($hFile, 0, "&New", $idNew)
	_GUICtrlMenu_InsertMenuItem($hFile, 1, "&Open", $idOpen)
	_GUICtrlMenu_InsertMenuItem($hFile, 2, "&Save", $idSave)
	_GUICtrlMenu_InsertMenuItem($hFile, 3, "", 0)
	_GUICtrlMenu_InsertMenuItem($hFile, 4, "E&xit", $idExit)

	; Create Edit menu
	$hEdit = _GUICtrlMenu_CreateMenu()
	_GUICtrlMenu_InsertMenuItem($hEdit, 0, "&Cut", $idCut)
	_GUICtrlMenu_InsertMenuItem($hEdit, 1, "C&opy", $idCopy)
	_GUICtrlMenu_InsertMenuItem($hEdit, 2, "&Paste", $idPaste)

	; Create Help menu
	$hHelp = _GUICtrlMenu_CreateMenu()
	_GUICtrlMenu_InsertMenuItem($hHelp, 0, "&About", $idAbout)

	; Create Main menu
	$hMain = _GUICtrlMenu_CreateMenu()
	_GUICtrlMenu_InsertMenuItem($hMain, 0, "&File", 0, $hFile)
	_GUICtrlMenu_InsertMenuItem($hMain, 1, "&Edit", 0, $hEdit)
	_GUICtrlMenu_InsertMenuItem($hMain, 2, "&Help", 0, $hHelp)

	; Set window menu
	_GUICtrlMenu_SetMenu($hGUI, $hMain)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 276, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Loop until user exits
	GUIRegisterMsg($WM_COMMAND, "WM_COMMAND")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Handle menu commands
Func WM_COMMAND($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $ilParam
	Switch _WinAPI_LoWord($iwParam)
		Case $idNew
			MemoWrite("New")
		Case $idOpen
			MemoWrite("Open")
		Case $idSave
			MemoWrite("Save")
		Case $idExit
			Exit
		Case $idCut
			MemoWrite("Cut")
		Case $idCopy
			MemoWrite("Copy")
		Case $idPaste
			MemoWrite("Paste")
		Case $idAbout
			MemoWrite("About")
	EndSwitch
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_COMMAND

; Write message to memo
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
