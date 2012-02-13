#include <FTPEx.au3>

Local $server = 'ftp.csx.cam.ac.uk'
Local $username = ''
Local $pass = ''

Local $Open = _FTP_Open('MyFTP Control')
Local $Conn = _FTP_Connect($Open, $server, $username, $pass)

Local $aFile = _FTP_ListToArray2D($Conn, 0)
ConsoleWrite('$Filename = ' & $aFile[0][0] & '  -> Error code: ' & @error & @CRLF)
ConsoleWrite('$Filename = ' & $aFile[1][0] & ' size = ' & $aFile[1][1] & @error & @CRLF)
ConsoleWrite('$Filename = ' & $aFile[2][0] & ' size = ' & $aFile[2][1] & @CRLF)

Local $Ftpc = _FTP_Close($Open)
