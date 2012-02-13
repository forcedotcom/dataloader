Local $msg

$msg = ""

$msg = $msg & "Floor(4.8) = " & Floor(4.8) & @CR
$msg = $msg & "Floor(4.5) = " & Floor(4.5) & @CR
$msg = $msg & "Floor(4.3) = " & Floor(4.3) & @CR
$msg = $msg & "Floor(4) = " & Floor(4) & @CR
$msg = $msg & "Floor(-4.3) = " & Floor(-4.3) & @CR
$msg = $msg & "Floor(-4.5) = " & Floor(-4.5) & @CR
$msg = $msg & "Floor(-4.8) = " & Floor(-4.8) & @CR
$msg = $msg & "Floor(-4) = " & Floor(-4) & @CR

MsgBox(64, "Testing", $msg)
