#include <Misc.au3>

Local $i_Count, $i_Index

For $i_Index = 1 To 2
	MsgBox(4096, "Count", "This dialog has displayed " & String($i_Index) & _
			" time" & _Iif($i_Index > 1, "s", "") & " so far")
Next

Exit
