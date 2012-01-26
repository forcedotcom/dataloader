#include <Inet.au3>

Local $s_SmtpServer = "mysmtpserver.com.au"
Local $s_FromName = "My Name"
Local $s_FromAddress = "From eMail Address"
Local $s_ToAddress = "To eMail Address"
Local $s_Subject = "My Test UDF"
Local $as_Body[2]
$as_Body[0] = "Testing the new email udf"
$as_Body[1] = "Second Line"
Local $Response = _INetSmtpMail($s_SmtpServer, $s_FromName, $s_FromAddress, $s_ToAddress, $s_Subject, $as_Body)
Local $err = @error
If $Response = 1 Then
	MsgBox(0, "Success!", "Mail sent")
Else
	MsgBox(0, "Error!", "Mail failed with error code " & $err)
EndIf
