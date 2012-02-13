#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <GuiListView.au3>
#include <GuiImageList.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $listview, $hImage
	Local $Wow64 = ""
	If @AutoItX64 Then $Wow64 = "\Wow6432Node"
	Local $AutoItDir = RegRead("HKEY_LOCAL_MACHINE\SOFTWARE" & $Wow64 & "\AutoIt v3\AutoIt", "InstallDir")

	GUICreate("ImageList AddIcon", 410, 300)
	$listview = GUICtrlCreateListView("", 2, 2, 404, 268, BitOR($LVS_SHOWSELALWAYS, $LVS_NOSORTHEADER, $LVS_REPORT))
	GUISetState()

	; Create an image list with images
	$hImage = _GUIImageList_Create(11, 11)
	AddIcon($hImage, $AutoItDir & "\Icons\filetype1.ico")
	AddIcon($hImage, $AutoItDir & "\Icons\filetype2.ico")
	AddIcon($hImage, $AutoItDir & "\Icons\filetype3.ico")
	_GUICtrlListView_SetImageList($listview, $hImage, 1)

	; Add columns
	_GUICtrlListView_AddColumn($listview, "Column 1", 100, 0, 0)
	_GUICtrlListView_AddColumn($listview, "Column 2", 100, 1, 1)
	_GUICtrlListView_AddColumn($listview, "Column 3", 100, 2, 2)
	_GUICtrlListView_AddColumn($listview, "Column 4", 100)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

; This is the long way to add an icon. Use _GUIImageList_AddIcon instead
Func AddIcon($hWnd, $sFile, $iIndex = 0)
	Local $pIcon, $tIcon, $hIcon

	$tIcon = DllStructCreate("int Icon")
	$pIcon = DllStructGetPtr($tIcon)
	_WinAPI_ExtractIconEx($sFile, $iIndex, 0, $pIcon, 1)
	$hIcon = DllStructGetData($tIcon, "Icon")
	_GUIImageList_ReplaceIcon($hWnd, -1, $hIcon)
	_WinAPI_DestroyIcon($hIcon)
EndFunc   ;==>AddIcon
