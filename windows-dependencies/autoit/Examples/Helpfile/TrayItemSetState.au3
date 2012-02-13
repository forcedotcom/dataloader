#include <Constants.au3>
#NoTrayIcon

Opt("TrayMenuMode", 1) ; Default tray menu items (Script Paused/Exit) will not be shown.

Local $chkitem = TrayCreateItem("Check it")
TrayCreateItem("")
Local $checkeditem = TrayCreateItem("Checked")
TrayCreateItem("")
Local $exititem = TrayCreateItem("Exit")

TraySetState()

While 1
	Local $msg = TrayGetMsg()
	Select
		Case $msg = 0
			ContinueLoop
		Case $msg = $chkitem
			TrayItemSetState($checkeditem, $TRAY_CHECKED)
		Case $msg = $exititem
			ExitLoop
	EndSelect
WEnd

Exit
