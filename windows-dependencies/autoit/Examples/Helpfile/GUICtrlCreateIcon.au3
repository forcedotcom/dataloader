#include <GUIConstantsEx.au3>

Example1()
Example2()


;example1 ---------------------------
Func Example1()
	GUICreate(" My GUI Icons", 250, 250)

	GUICtrlCreateIcon("shell32.dll", 10, 20, 20)
	GUICtrlCreateIcon(@WindowsDir & "\cursors\horse.ani", -1, 20, 40, 32, 32)
	GUICtrlCreateIcon("shell32.dll", 7, 20, 75, 32, 32)
	GUISetState()

	; Run the GUI until the dialog is closed
	While 1
		Local $msg = GUIGetMsg()

		If $msg = $GUI_EVENT_CLOSE Then ExitLoop
	WEnd
	GUIDelete()
EndFunc   ;==>Example1



; example2 ---------------------------
Func Example2()
	Local $iOldOpt, $n1, $n2, $a, $b
	$iOldOpt = Opt("GUICoordMode", 1)

	GUICreate("My GUI icon Race", 350, 74, -1, -1)
	GUICtrlCreateLabel("", 331, 0, 1, 74, 5)
	$n1 = GUICtrlCreateIcon(@WindowsDir & "\cursors\dinosaur.ani", -1, 0, 0, 32, 32)
	$n2 = GUICtrlCreateIcon(@WindowsDir & "\cursors\horse.ani", -1, 0, 40, 32, 32)

	GUISetState(@SW_SHOW)

	$a = 0
	$b = 0
	While ($a < 300) And ($b < 300)
		$a = $a + Int(Random(0, 1) + 0.5)
		$b = $b + Int(Random(0, 1) + 0.5)
		GUICtrlSetPos($n1, $a, 0)
		GUICtrlSetPos($n2, $b, 40)
		Sleep(20)
	WEnd
	Sleep(1000)
	Opt("GUICoordMode", $iOldOpt)
EndFunc   ;==>Example2
