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
	InsertItem($hFile, 0, "&New", $idNew)
	InsertItem($hFile, 1, "&Open", $idOpen)
	InsertItem($hFile, 2, "&Save", $idSave)
	InsertItem($hFile, 3, "", 0)
	InsertItem($hFile, 4, "E&xit", $idExit)

	; Create Edit menu
	$hEdit = _GUICtrlMenu_CreateMenu()
	InsertItem($hEdit, 0, "&Cut", $idCut)
	InsertItem($hEdit, 1, "C&opy", $idCopy)
	InsertItem($hEdit, 2, "&Paste", $idPaste)

	; Create Help menu
	$hHelp = _GUICtrlMenu_CreateMenu()
	InsertItem($hHelp, 0, "&About", $idAbout)

	; Create Main menu
	$hMain = _GUICtrlMenu_CreateMenu()
	InsertItem($hMain, 0, "&File", 0, $hFile)
	InsertItem($hMain, 1, "&Edit", 0, $hEdit)
	InsertItem($hMain, 2, "&Help", 0, $hHelp)

	; Set window menu
	_GUICtrlMenu_SetMenu($hGUI, $hMain)
	GUISetState()

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>_Main

; Insert menu item (the hard way)
Func InsertItem($hMenu, $iIndex, $sText, $iCmdID = 0, $hSubMenu = 0)
	Local $tMenu, $tText

	$tMenu = DllStructCreate($tagMENUITEMINFO)
	DllStructSetData($tMenu, "Size", DllStructGetSize($tMenu))
	DllStructSetData($tMenu, "Mask", BitOR($MIIM_ID, $MIIM_STRING, $MIIM_SUBMENU))
	DllStructSetData($tMenu, "ID", $iCmdID)
	DllStructSetData($tMenu, "SubMenu", $hSubMenu)
	If $sText = "" Then
		DllStructSetData($tMenu, "Mask", $MIIM_FTYPE)
		DllStructSetData($tMenu, "Type", $MFT_SEPARATOR)
	Else
		DllStructSetData($tMenu, "Mask", BitOR($MIIM_ID, $MIIM_STRING, $MIIM_SUBMENU))
		$tText = DllStructCreate("wchar Text[" & StringLen($sText) + 1 & "]")
		DllStructSetData($tText, "Text", $sText)
		DllStructSetData($tMenu, "TypeData", DllStructGetPtr($tText))
	EndIf
	_GUICtrlMenu_InsertMenuItemEx($hMenu, $iIndex, $tMenu)
EndFunc   ;==>InsertItem
