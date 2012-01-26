#NoTrayIcon

Opt("TrayMenuMode", 1) ; Default tray menu items (Script Paused/Exit) will not be shown.

Local $exititem = TrayCreateItem("Exit")

TraySetIcon("warning")
TraySetToolTip("SOS")

TraySetState() ; Show the tray icon

Local $toggle = 0

While 1
	Local $msg = TrayGetMsg()
	Select
		Case $msg = 0
			Sleep(1000)
			If $toggle = 0 Then
				TraySetState() ; Show the tray icon
				$toggle = 1
			Else
				TraySetState(2) ; Hide the tray icon
				$toggle = 0
			EndIf
		Case $msg = $exititem
			ExitLoop
	EndSelect

WEnd

Exit
