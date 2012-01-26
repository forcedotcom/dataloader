#NoTrayIcon

Opt("TrayMenuMode", 1) ; Default tray menu items (Script Paused/Exit) will not be shown.

Local $getitem = TrayCreateItem("Get State")
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
		Case $msg = $getitem
			MsgBox(64, "State", TrayItemGetState($aboutitem))
		Case $msg = $aboutitem
			MsgBox(64, "About:", "AutoIt3-Tray-sample")
		Case $msg = $exititem
			ExitLoop
	EndSelect
WEnd

Exit
