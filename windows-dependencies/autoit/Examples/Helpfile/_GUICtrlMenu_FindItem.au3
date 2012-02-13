#include <GuiMenu.au3>
#include <GUIConstantsEx.au3>

_Main()

Func _Main()
	Local $hGUI, $hFile, $hEdit, $hHelp, $hMain, $iIndex
	Local Enum $idNew = 1000, $idOpen, $idSave, $idExit, $idCut, $idCopy, $idPaste, $idAbout

	; Create GUI
	$hGUI = GUICreate("Menu", 400, 300)
	GUISetState()

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

	; Click Help menu item
	$iIndex = _GUICtrlMenu_FindItem($hMain, "Help")
	MsgBox(4096, "Information", "Index for Help Menu: " & $iIndex)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main
