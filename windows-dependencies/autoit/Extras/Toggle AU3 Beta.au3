#RequireAdmin
; ----------------------------------------------------------------------------
;
; AutoIt Version: 3.1.1
; Author:         Ejoc/JPM/Jon
;
; Script Function:
;	Toggle AutoIt beta.
;
; ----------------------------------------------------------------------------

Opt("MustDeclareVars",1)

Local $InstallDir = RegRead("HKLM\Software\AutoIt v3\AutoIt","InstallDir")
If $InstallDir = "" Then $InstallDir = RegRead("HKLM\Software\Wow6432Node\AutoIt v3\AutoIt","InstallDir")
If $InstallDir = "" Then
	MsgBox(0,'Error', 'Cannot find AutoIt Installation directory')
	Exit
EndIf

Const $Key = "HKCR\.au3"
Local $CurrentAssoc = RegRead($Key,"") 

If $CurrentAssoc = "AutoIt3ScriptBeta" Then
	; Already using beta switch to prod
	RegWrite($Key, "", "REG_SZ", "AutoIt3Script")
	; make sure that the right AutoItX.dll is installed
	RunWait('regsvr32 /s "' & $InstallDir & '\AutoItX\AutoItX3.dll"')
	MsgBox(0, "Beta Toggle","Now using RELEASE version v" & FileGetVersion($InstallDir & '\AutoIt3.exe') & " of AutoIt", 2)
ElseIf $CurrentAssoc = "AutoIt3Script" Then
	; Using prod, change to beta
	RegWrite($Key, "", "REG_SZ", "AutoIt3ScriptBeta")
	; make sure that the right AutoItX.dll is installed
	RunWait('regsvr32 /s "' & $InstallDir & '\beta\AutoItX\AutoItX3.dll"')
	MsgBox(0, "Beta Toggle","Now using BETA version v" & FileGetVersion($InstallDir & '\Beta\AutoIt3.exe') & " of AutoIt", 2)
Else
	MsgBox(0, "Beta Toggle","AutoIt installation appears to be customised - please manually edit file associations.")
EndIf