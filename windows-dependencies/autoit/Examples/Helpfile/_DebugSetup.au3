#cs ----------------------------------------------------------------------------

	AutoIt Version: 3.2.8.1
	Author:         David Nuttall

	Script Function:
	Base script to show functionality of Debug functions.

#ce ----------------------------------------------------------------------------

#include <Debug.au3>

_DebugSetup("Check Excel", True) ; start displaying debug environment
For $i = 1 To 4
	WinActivate("Microsoft Excel")
	; interact with Excel
	Send("{Down}")
	_DebugOut("Moved Mouse Down") ; forces debug notepad window to take control
Next
