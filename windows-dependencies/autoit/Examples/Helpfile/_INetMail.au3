#include <Inet.au3>

Local $Address = InputBox('Address', 'Enter the E-Mail address to send message to')
Local $Subject = InputBox('Subject', 'Enter a subject for the E-Mail')
Local $Body = InputBox('Body', 'Enter the body (message) of the E-Mail')
MsgBox(0, 'E-Mail has been opened', 'The E-Mail has been opened and process identifier for the E-Mail client is ' & _INetMail($Address, $Subject, $Body))
