#NoTrayIcon

Opt("TrayOnEventMode", 1)
Opt("TrayMenuMode", 1) ; Default tray menu items (Script Paused/Exit) will not be shown.

TraySetClick(16) ; Only secondary mouse button will show the tray menu.

TrayCreateItem("Info")
TrayItemSetOnEvent(-1, "ShowInfo")

TrayCreateItem("")

TrayCreateItem("Exit")
TrayItemSetOnEvent(-1, "ExitScript")

TraySetState()

While 1
	Sleep(10) ; Idle loop
WEnd

Exit


; Functions
Func ShowInfo()
	MsgBox(0, "Info", "Tray OnEvent Demo")
EndFunc   ;==>ShowInfo


Func ExitScript()
	Exit
EndFunc   ;==>ExitScript
