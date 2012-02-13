#include <GuiMenu.au3>
#include <GUIConstantsEx.au3>

Global $iMemo

_Main()

Func _Main()
	Local $hGUI, $hFile, $hEdit, $hHelp, $hMain
	Local Enum $idNew = 1000, $idOpen, $idSave, $idExit, $idCut, $idCopy, $idPaste, $idAbout

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
	_GUICtrlMenu_InsertMenuItem($hMain, 2, "&Help", 0, 0)

	; Set window menu
	_GUICtrlMenu_SetMenu($hGUI, $hMain)

	; Create memo control
	$iMemo = GUICtrlCreateEdit("", 2, 2, 396, 276, 0)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	; Get/Set Help menu
	MemoWrite("Help submenu handle: " & _GUICtrlMenu_GetItemSubMenu($hMain, 2))
	_GUICtrlMenu_SetItemSubMenu($hMain, 2, $hHelp)
	MemoWrite("Help submenu handle: " & _GUICtrlMenu_GetItemSubMenu($hMain, 2))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Write message to memo
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
