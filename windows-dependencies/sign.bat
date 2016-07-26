@ECHO OFF
SET SUBJECT=%1
SET FILE=%~2
ECHO %*

SET ROOTDIR="%programfiles(x86)%"
IF NOT EXIST %ROOTDIR% (
	SET ROOTDIR="%programfiles%"
)
CALL :UNQUOTE ROOTDIR %ROOTDIR%

SET SIGNTOOL="%ROOTDIR%\Windows Kits\10\Bin\signtool.exe"
IF NOT EXIST %SIGNTOOL% (
	SET SIGNTOOL="%ROOTDIR%\Windows Kits\8.1\Bin\signtool.exe"
)
IF NOT EXIST %SIGNTOOL% (
	SET SIGNTOOL="%ROOTDIR%\Windows Kits\8.0\Bin\signtool.exe"
)
IF NOT EXIST %SIGNTOOL% (
	SET SIGNTOOL="%ROOTDIR%\Microsoft SDKs\Windows\v7.1\Bin\signtool.exe"
)

IF EXIST %SIGNTOOL% IF [%FILE%] NEQ [] IF ["%SUBJECT%"] NEQ ["DONTSIGN"] IF [%SUBJECT%] NEQ [] (
	%SIGNTOOL% sign /n "%SUBJECT%" /fd sha256 /tr http://sha256timestamp.ws.symantec.com/sha256/timestamp "%FILE%"
)

GOTO :EOF

:UNQUOTE
  set %1=%~2
  goto :EOF