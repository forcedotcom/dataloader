#include <GuiMenu.au3>
#include <GUIConstantsEx.au3>

_Main()

Func _Main()
	Local $hGUI, $hFile, $hEdit, $hHelp, $hMain
	Local Enum $idNew = 1000, $idOpen, $idSave, $idExit, $idCut, $idCopy, $idPaste, $idAbout

	; Create GUI
	$hGUI = GUICreate("Menu", 400, 300)

	; Create File menu
	$hFile = _GUICtrlMenu_CreateMenu()
	_GUICtrlMenu_AddMenuItem($hFile, "&New", $idNew)
	_GUICtrlMenu_AddMenuItem($hFile, "&Open", $idOpen)
	_GUICtrlMenu_AddMenuItem($hFile, "&Save", $idSave)
	_GUICtrlMenu_AddMenuItem($hFile, "", 0)
	_GUICtrlMenu_AddMenuItem($hFile, "E&xit", $idExit)

	; Create Edit menu
	$hEdit = _GUICtrlMenu_CreateMenu()
	_GUICtrlMenu_AddMenuItem($hEdit, "&Cut", $idCut)
	_GUICtrlMenu_AddMenuItem($hEdit, "C&opy", $idCopy)
	_GUICtrlMenu_AddMenuItem($hEdit, "&Paste", $idPaste)

	; Create Help menu
	$hHelp = _GUICtrlMenu_CreateMenu()
	_GUICtrlMenu_AddMenuItem($hHelp, "&About", $idAbout)

	; Create Main menu
	$hMain = _GUICtrlMenu_CreateMenu()
	_GUICtrlMenu_AddMenuItem($hMain, "&File", 0, $hFile)
	_GUICtrlMenu_AddMenuItem($hMain, "&Edit", 0, $hEdit)
	_GUICtrlMenu_AddMenuItem($hMain, "&Help", 0, $hHelp)

	; Set window menu
	_GUICtrlMenu_SetMenu($hGUI, $hMain)
	GUISetState()

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main
