; WMI example to enumerate services

$ObjWMI= ObjGet("winmgmts://" & @ComputerName)

$string = ""
for $item in $ObjWMI.ExecQuery("select * from win32_service")
 $string = $string & $item.name & @TAB
next

msgbox(0,"","Services on this computer: " & @CRLF & $string)