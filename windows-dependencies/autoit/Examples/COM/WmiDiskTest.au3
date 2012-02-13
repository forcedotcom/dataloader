; AutoItCOM 3.1.0
;
; Test File
;
; Use WMI to collect logical drive information


$objcol=ObjGet("winmgmts:")

$instance=$objcol.instancesof("Win32_LogicalDisk")

if @error then
	Msgbox (0,"","error getting object. Error code: " & @error)
	exit
endif

$string = "size:" & @TAB & "driveletter:" & @CRLF

FOR $Drive IN $instance
	$string = $string & $drive.size & @TAB & $drive.deviceid & @CRLF
NEXT

msgbox(0,"Drive test","Drive information: " & @CRLF & $string)
