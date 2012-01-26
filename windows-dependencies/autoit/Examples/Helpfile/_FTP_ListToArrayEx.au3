#include <FTPEx.au3>
#include <Array.au3>

Local $server = 'ftp.csx.cam.ac.uk'
Local $username = ''
Local $pass = ''

Local $Open = _FTP_Open('MyFTP Control')
Local $Conn = _FTP_Connect($Open, $server, $username, $pass)

Local $aFile = _FTP_ListToArrayEx($Conn, 0)
_ArrayDisplay($aFile)

Local $Ftpc = _FTP_Close($Open)
