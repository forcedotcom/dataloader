#RequireAdmin
;
; AutoIt Version: 3.0
; Language:       English
; Platform:       Win9x/NT
; Author:         Jonathan Bennett <jon at hiddensoft com>
;
; Script Function:
;	Syntax highlighting files installation.
;

; Prompt the user to run the script - use a Yes/No prompt (4 - see help file)
Local $answer = MsgBox(4, "Crimson Editor", "This script will attempt to automatically install syntax highlighting files for Crimson Editor.  Run?")
If $answer = 7 Then Exit

; Find an verify the installation directory
Local $installdir = RegRead("HKEY_LOCAL_MACHINE\Software\Crimson System\Crimson Editor", "InstallDir")
If @error Then Error()

Local $spec = $installdir & "\spec\"
Local $link = $installdir & "\link\"

; Check that both directories exist
If Not FileExists($spec) Or Not FileExists($link) Then Error()

If Not FileCopy("autoit3.spc", $spec, 1) Or Not FileCopy("autoit3.key", $spec, 1) Then Error()
If Not FileCopy("extension.au3", $link, 1) Then Error()

MsgBox(4096, "Crimson Editor", "Installation complete!")

; End of script


Func Error()
	MsgBox(4096, "Error", "Unable to find Crimson Editor or error installing the syntax files.  Please try a manual installation.")
	Exit
EndFunc
