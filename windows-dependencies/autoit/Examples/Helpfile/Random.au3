;Flip of coin
Local $Msg
If Random() < 0.5 Then ; Returns a value between 0 and 1.
	$Msg = "Heads. 50% Win"
Else
	$Msg = "Tails. 50% Loss"
EndIf
MsgBox(0, "Coin toss", $Msg)


;Roll of a die
MsgBox(0, "Roll of die", "You rolled a " & Random(1, 6, 1))

Local $StockPrice = 98
;In the middle of a stock market simulation
Local $StockPriceChange = Random(-10, 10, 1) ; generate an integer between -10 and 10
$StockPrice = $StockPrice + $StockPriceChange
If $StockPriceChange < 0 Then
	MsgBox(4096, "Stock Change", "Your stock dropped to $" & $StockPrice)
ElseIf $StockPriceChange > 0 Then
	MsgBox(4096, "Stock Change", "Your stock rose to $" & $StockPrice)
Else
	MsgBox(4096, "Stock Change", "Your stock stayed at $" & $StockPrice)
EndIf


;Random letter
Local $Letter
If Random() < 0.5 Then
	;Capitals
	$Letter = Chr(Random(Asc("A"), Asc("Z"), 1))
Else
	;Lower case
	$Letter = Chr(Random(Asc("a"), Asc("z"), 1))
EndIf
