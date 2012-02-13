; ****************
; * First sample *
; ****************

#NoTrayIcon

Opt("TrayMenuMode", 1) ; Default tray menu items (Script Paused/Exit) will not be shown.

Local $prefsitem = TrayCreateItem("Preferences")
TrayCreateItem("")
Local $aboutitem = TrayCreateItem("About")
TrayCreateItem("")
Local $exititem = TrayCreateItem("Exit")

TraySetState()

While 1
	Local $msg = TrayGetMsg()
	Select
		Case $msg = 0
			ContinueLoop
		Case $msg = $prefsitem
			MsgBox(64, "Preferences:", "OS:" & @OSVersion)
		Case $msg = $aboutitem
			MsgBox(64, "About:", "AutoIt3-Tray-sample.")
		Case $msg = $exititem
			ExitLoop
	EndSelect
WEnd

Exit


; *****************
; * Second sample *
; *****************

#include <Constants.au3>
#NoTrayIcon

Opt("TrayMenuMode", 1) ; Default tray menu items (Script Paused/Exit) will not be shown.

; Let's create 2 radio menuitem groups
Local $radio1 = TrayCreateItem("Radio1", -1, -1, 1)
TrayItemSetState(-1, $TRAY_CHECKED)
TrayCreateItem("Radio2", -1, -1, 1)
TrayCreateItem("Radio3", -1, -1, 1)

TrayCreateItem("") ; Radio menuitem groups can be separated by a separator line or another norma menuitem

TrayCreateItem("Radio4", -1, -1, 1)
TrayCreateItem("Radio5", -1, -1, 1)
TrayItemSetState(-1, $TRAY_CHECKED)
TrayCreateItem("Radio6", -1, -1, 1)

TrayCreateItem("")

$aboutitem = TrayCreateItem("About")
TrayCreateItem("")
$exititem = TrayCreateItem("Exit")

TraySetState()

While 1
	$msg = TrayGetMsg()
	Select
		Case $msg = 0
			ContinueLoop
		Case $msg = $aboutitem
			MsgBox(64, "About:", "AutoIt3-Tray-sample with radio menuitem groups.")
		Case $msg = $exititem
			ExitLoop
	EndSelect
WEnd

Exit
