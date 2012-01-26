#include <Timers.au3>

; Mouse/Keyboard action during this 10 sec delay will change reported idle time
Sleep(10 * 1000); 10sec

Global $iIdleTime = _Timer_GetIdleTime()

MsgBox(64, "_Timer_GetIdleTime", "Idle time = " & $iIdleTime & "ms")
