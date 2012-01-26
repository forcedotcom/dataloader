#include <Constants.au3>

Opt("TrayMenuMode", 1) ; Don't show the default tray context menu

Global Const $MIM_APPLYTOSUBMENUS = 0x80000000
Global Const $MIM_BACKGROUND = 0x00000002

TraySetIcon("shell32.dll", 21)
TraySetToolTip("This is just a small example to show that colored tray menus" & @LF & "are easy possible under Windows 2000 and higher.")

Local $OptionsMenu = TrayCreateMenu("Options")
TrayCreateItem("Always On Top", $OptionsMenu)
TrayItemSetState(-1, $TRAY_CHECKED)
TrayCreateItem("Repeat Always", $OptionsMenu)
TrayCreateItem("")
Local $AboutItem = TrayCreateItem("About")
TrayCreateItem("")
Local $ExitItem = TrayCreateItem("Exit Sample")

SetMenuColor(0, 0xEEBB99) ; BGR color value, '0' means the tray context menu handle itself
SetMenuColor($OptionsMenu, 0x66BB99); BGR color value

While 1
	Local $Msg = TrayGetMsg()

	Switch $Msg
		Case $ExitItem
			ExitLoop

		Case $AboutItem
			MsgBox(64, "About...", "Colored tray menu sample")
	EndSwitch
WEnd

Exit


; Apply the color to the menu
Func SetMenuColor($nMenuID, $nColor)
	Local $hMenu = TrayItemGetHandle($nMenuID) ; Get the internal menu handle

	Local $hBrush = DllCall("gdi32.dll", "hwnd", "CreateSolidBrush", "int", $nColor)
	$hBrush = $hBrush[0]

	Local $stMenuInfo = DllStructCreate("dword;dword;dword;uint;ptr;dword;ptr")
	DllStructSetData($stMenuInfo, 1, DllStructGetSize($stMenuInfo))
	DllStructSetData($stMenuInfo, 2, BitOR($MIM_APPLYTOSUBMENUS, $MIM_BACKGROUND))
	DllStructSetData($stMenuInfo, 5, $hBrush)

	DllCall("user32.dll", "int", "SetMenuInfo", "hwnd", $hMenu, "ptr", DllStructGetPtr($stMenuInfo))
EndFunc   ;==>SetMenuColor
