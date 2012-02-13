#include <GUIConstantsEx.au3>

Example()

Func Example()
	Local $FileMenu, $OptionsMenu
	Local $ExitItem, $HelpMenu, $AboutItem
	Local $EndBtn, $Msg


	GUICreate("My GUI", 300, 200)

	$FileMenu = GUICtrlCreateMenu("&File")
	GUICtrlCreateMenuItem("&Open", $FileMenu)
	GUICtrlCreateMenuItem("&Save", $FileMenu)
	GUICtrlCreateMenuItem("", $FileMenu)

	$OptionsMenu = GUICtrlCreateMenu("O&ptions", $FileMenu)
	GUICtrlCreateMenuItem("View", $OptionsMenu)
	GUICtrlCreateMenuItem("", $OptionsMenu)
	GUICtrlCreateMenuItem("Tools", $OptionsMenu)

	GUICtrlCreateMenuItem("", $FileMenu)
	$ExitItem = GUICtrlCreateMenuItem("&Exit", $FileMenu)

	$HelpMenu = GUICtrlCreateMenu("&?")
	$AboutItem = GUICtrlCreateMenuItem("&About", $HelpMenu)

	$EndBtn = GUICtrlCreateButton("End", 110, 140, 70, 20)

	SetMenuColor($FileMenu, 0xEEBB99) ; BGR color value
	SetMenuColor($OptionsMenu, 0x66BB99); BGR color value
	SetMenuColor($HelpMenu, 0x99BBEE) ; BGR color value

	GUISetState()

	While 1
		$Msg = GUIGetMsg()

		Switch $Msg
			Case $ExitItem, $EndBtn, $GUI_EVENT_CLOSE
				ExitLoop

			Case $AboutItem
				MsgBox(64, "About...", "Colored menu sample")
		EndSwitch
	WEnd
EndFunc   ;==>Example


; Apply the color to the menu
Func SetMenuColor($nMenuID, $nColor)
	Local $hMenu, $hBrush, $stMenuInfo
	Local Const $MIM_APPLYTOSUBMENUS = 0x80000000
	Local Const $MIM_BACKGROUND = 0x00000002

	$hMenu = GUICtrlGetHandle($nMenuID)

	$hBrush = DllCall("gdi32.dll", "hwnd", "CreateSolidBrush", "int", $nColor)
	$hBrush = $hBrush[0]

	$stMenuInfo = DllStructCreate("dword;dword;dword;uint;dword;dword;ptr")
	DllStructSetData($stMenuInfo, 1, DllStructGetSize($stMenuInfo))
	DllStructSetData($stMenuInfo, 2, BitOR($MIM_APPLYTOSUBMENUS, $MIM_BACKGROUND))
	DllStructSetData($stMenuInfo, 5, $hBrush)

	DllCall("user32.dll", "int", "SetMenuInfo", "hwnd", $hMenu, "ptr", DllStructGetPtr($stMenuInfo))

	; release Struct not really needed as it is a local
	$stMenuInfo = 0
EndFunc   ;==>SetMenuColor
