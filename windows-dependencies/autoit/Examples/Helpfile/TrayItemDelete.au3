#NoTrayIcon

Opt("TrayMenuMode", 1) ; Default tray menu items (Script Paused/Exit) will not be shown.

Local $delitem = TrayCreateItem("Delete")
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
		Case $msg = $aboutitem
			MsgBox(64, "About:", "AutoIt3-Tray-sample")
		Case $msg = $delitem
			TrayItemDelete($delitem)
		Case $msg = $exititem
			ExitLoop
	EndSelect
WEnd

Exit
