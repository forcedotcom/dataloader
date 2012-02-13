#include <GUIConstantsEx.au3>
#include <GuiButton.au3>
#include <WindowsConstants.au3>

Global $iMemo

_Main()

Func _Main()
	Local $btn, $chk, $rdo, $Msg
	Local $Wow64 = ""
	If @AutoItX64 Then $Wow64 = "\Wow6432Node"
	Local $sPath = RegRead("HKEY_LOCAL_MACHINE\SOFTWARE" & $Wow64 & "\AutoIt v3\AutoIt", "InstallDir") & "\Examples\GUI\Advanced\Images"

	GUICreate("Buttons", 300, 300)
	$iMemo = GUICtrlCreateEdit("", 2, 60, 296, 236, $WS_VSCROLL)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()

	$btn = GUICtrlCreateButton("Button1", 10, 10, 40, 40, $BS_BITMAP)
	_GUICtrlButton_SetImage($btn, $sPath & "\blue.bmp")

	$chk = GUICtrlCreateCheckbox("Check1", 60, 10, 50, 32, $BS_ICON)
	_GUICtrlButton_SetImage($chk, "shell32.dll", 14, True)

	$rdo = GUICtrlCreateRadio("Radio1", 120, 10, 50, 32, $BS_ICON)
	_GUICtrlButton_SetImage($rdo, "shell32.dll", 21, True)

	MemoWrite("Button1 Image Handle: " & _GUICtrlButton_GetImage($btn))
	MemoWrite("Check1 Image Handle: " & _GUICtrlButton_GetImage($chk))
	MemoWrite("Radio1 Image Handle: " & _GUICtrlButton_GetImage($rdo))

	While 1
		$Msg = GUIGetMsg()
		If $Msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd

	Exit
EndFunc   ;==>_Main

; Write a line to the memo control
Func MemoWrite($sMessage)
	GUICtrlSetData($iMemo, $sMessage & @CRLF, 1)
EndFunc   ;==>MemoWrite
