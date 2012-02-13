#include <FTPEx.au3>

Local $server = 'ftp.csx.cam.ac.uk'
Local $username = ''
Local $pass = ''

Local $Open = _FTP_Open('MyFTP Control')
Local $Conn = _FTP_Connect($Open, $server, $username, $pass)

Local $h_Handle
Local $aFile = _FTP_FindFileFirst($Conn, "/pub/software/programming/pcre/", $h_Handle)

Local $FindClose = _FTP_FindFileClose($h_Handle)
ConsoleWrite('$FindClose = ' & $FindClose & '  -> Error code: ' & @error & @CRLF)

Local $Ftpc = _FTP_Close($Open)
