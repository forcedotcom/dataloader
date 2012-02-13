#include <Color.au3>

Local $nColor = 0x8090ff

Local $aColor = _ColorGetCOLORREF($nColor)
MsgBox(4096, "AutoIt", "Color=" & Hex($nColor) & @CRLF & " Red=" & Hex($aColor[0], 2) & " Blue=" & Hex($aColor[1], 2) & " Green=" & Hex($aColor[2], 2))
