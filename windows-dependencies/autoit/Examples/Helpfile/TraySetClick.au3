#NoTrayIcon

Opt("TrayMenuMode", 1) ; Default tray menu items (Script Paused/Exit) will not be shown.

Local $settingsitem = TrayCreateMenu("Settings")
TrayCreateItem("Display", $settingsitem)
TrayCreateItem("Printer", $settingsitem)
TrayCreateItem("")
Local $aboutitem = TrayCreateItem("About")
TrayCreateItem("")
Local $exititem = TrayCreateItem("Exit")

TraySetState()
TraySetClick(16)

While 1
	Local $msg = TrayGetMsg()
	Select
		Case $msg = 0
			ContinueLoop
		Case $msg = $aboutitem
			MsgBox(64, "About:", "AutoIt3-Tray-sample")
		Case $msg = $exititem
			ExitLoop
	EndSelect
WEnd

Exit
