#NoTrayIcon

Opt("TrayMenuMode", 1) ; Default tray menu items (Script Paused/Exit) will not be shown.

Local $exititem = TrayCreateItem("Exit")

TraySetState()

Local $start = 0
While 1
	Local $msg = TrayGetMsg()
	If $msg = $exititem Then ExitLoop
	Local $diff = TimerDiff($start)
	If $diff > 1000 Then
		Local $num = -Random(0, 100, 1) ; negative to use ordinal numbering
		ToolTip("#icon=" & $num)
		TraySetIcon("Shell32.dll", $num)
		$start = TimerInit()
	EndIf
WEnd

Exit
