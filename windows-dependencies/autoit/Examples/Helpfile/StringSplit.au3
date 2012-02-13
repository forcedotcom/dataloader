Local $days = StringSplit("Sun,Mon,Tue,Wed,Thu,Fri,Sat", ",")
;$days[1] contains "Sun" ... $days[7] contains "Sat"

Local $text = "This\nline\ncontains\nC-style breaks."
Local $array = StringSplit($text, '\n', 1)
