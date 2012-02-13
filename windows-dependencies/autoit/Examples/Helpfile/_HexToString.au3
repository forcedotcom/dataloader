#include <String.au3>

Local $String = "I like AutoIt3"
Local $Hex = _StringToHex($String)
MsgBox(0, "Hex", "Original String: " & $String & @LF & " Hex: " & $Hex)

$Hex = "49206C696B65204175746F497433"
$String = _HexToString($Hex)
MsgBox(0, "Hex", "Original Hex: " & $Hex & @LF & " String: " & $String)
