#include <FTPEx.au3>

Local $Open = _FTP_Open('MyFTP Control')
; ...
Local $Ftpc = _FTP_Close($Open)
