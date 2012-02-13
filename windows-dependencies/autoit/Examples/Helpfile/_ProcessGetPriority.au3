#include<Process.au3>

Local $i_Priority_Level, $i_Notepad_PID, $i_ArrayItem
Local $a_RunLevels[3] = [0, 2, 4] ;low, normal, high priorities
;Get Priority Level of this instance of AutoIt Scripting Engine
$i_Priority_Level = _ProcessGetPriority(@AutoItPID)
MsgBox(0, "AutoIt Script", "Should be 2: " & $i_Priority_Level)
$i_Notepad_PID = Run(@ComSpec & ' /k notepad.exe', '', @SW_HIDE)
For $i_ArrayItem = 0 To 2
	ProcessSetPriority($i_Notepad_PID, $a_RunLevels[$i_ArrayItem])
	$i_Priority_Level = _ProcessGetPriority($i_Notepad_PID)
	MsgBox(0, "Notepad Priority", "Should be " & $a_RunLevels[$i_ArrayItem] & ": " & $i_Priority_Level)
Next
ProcessClose('notepad.exe')
