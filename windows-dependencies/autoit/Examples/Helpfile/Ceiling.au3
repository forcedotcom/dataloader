Local $msg

$msg = ""
$msg = $msg & "Ceiling(4.8) = " & Ceiling(4.8) & @CR
$msg = $msg & "Ceiling(4.5) = " & Ceiling(4.5) & @CR
$msg = $msg & "Ceiling(4.3) = " & Ceiling(4.3) & @CR
$msg = $msg & "Ceiling(4) = " & Ceiling(4) & @CR
$msg = $msg & "Ceiling(-4.3) = " & Ceiling(-4.3) & @CR
$msg = $msg & "Ceiling(-4.5) = " & Ceiling(-4.5) & @CR
$msg = $msg & "Ceiling(-4.8) = " & Ceiling(-4.8) & @CR
$msg = $msg & "Ceiling(-4) = " & Ceiling(-4) & @CR

MsgBox(64, "Testing", $msg)
