#NoTrayIcon

TraySetPauseIcon("shell32.dll", 12)
TraySetState()

While 1
	Local $msg = TrayGetMsg()
WEnd

Exit
