; AutoITCOM 3.1.0
;
;
; Testing ByRef variables on COM function calls
;
; Retrieving the Internet Explorer home page value using registry-read by WMI


$HKEY_CURRENT_USER = 0x80000001

$strComputer = @ComputerName

$objReg = ObjGet("winmgmts:\\" & $strComputer & "\root\default:StdRegProv")
$strKeyPath = "SOFTWARE\Microsoft\Internet Explorer\Main"
$ValueName = "Start Page"
$strvalue = ""
$return = $objReg.GetStringValue($HKEY_CURRENT_USER,$strKeyPath,$ValueName,$strValue)


If $strValue="" Then
    msgbox(0,"WMIGetStringTest", "The value of your IE start page is either Null or could not be found in the registry.")
Else
    msgbox(0,"WMIGetStringTest","Your Internet Explorer start page is: " & @CRLF & $strValue)
EndIf