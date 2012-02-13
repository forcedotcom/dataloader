#include <FTPEx.au3>

Local $server = 'ftp.csx.cam.ac.uk'
Local $username = ''
Local $pass = ''

Local $Open = _FTP_Open('MyFTP Control')
Local $Conn = _FTP_Connect($Open, $server, $username, $pass)

Local $h_Handle
Local $aFile = _FTP_FindFileFirst($Conn, "/pub/software/programming/pcre/", $h_Handle)
ConsoleWrite('$Filename = ' & $aFile[10] & ' attribute = ' & $aFile[1] & '  -> Error code: ' & @error & ' extended: ' & @extended & @CRLF)

Local $dirset = _FTP_DirSetCurrent($Conn, "/pub/software/programming/pcre/")
ConsoleWrite('$dirset = ' & $dirset & '  -> Error code: ' & @error & ' extended: ' & @extended & @CRLF)

Local $FileSize = _FTP_FileGetSize($Conn, $aFile[10])
ConsoleWrite('$Filename = ' & $aFile[10] & ' size = ' & $FileSize & '  -> Error code: ' & @error & ' extended: ' & @extended & @CRLF)

Local $Err, $Message
$FileSize = _FTP_GetLastResponseInfo($Err, $Message) ; error =  Contrib: Not a regular file
ConsoleWrite('$Message = ' & $Message & ' err = ' & $Err & '  -> Error code: ' & @error & ' extended: ' & @extended & @CRLF)

$aFile = _FTP_FindFileNext($h_Handle)
ConsoleWrite('$FilenameNext1 = ' & $aFile[10] & ' attribute = ' & $aFile[1] & '  -> Error code: ' & @error & ' extended: ' & @extended & @CRLF)

$FileSize = _FTP_FileGetSize($Conn, $aFile[10])
ConsoleWrite('$FilenameNext1 = ' & $aFile[10] & ' size = ' & $FileSize & '  -> Error code: ' & @error & ' extended: ' & @extended & @CRLF)

$FileSize = _FTP_GetLastResponseInfo($Err, $Message) ; no error
ConsoleWrite('$Message = ' & $Message & ' err = ' & $Err & '  -> Error code: ' & @error & ' extended: ' & @extended & @CRLF)

Local $FindClose = _FTP_FindFileClose($h_Handle)

Local $Ftpc = _FTP_Close($Open)
