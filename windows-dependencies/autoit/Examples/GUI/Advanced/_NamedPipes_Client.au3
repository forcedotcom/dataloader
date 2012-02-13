#include <NamedPipes.au3>
#include <WinAPI.au3>
#include <WindowsConstants.au3>
#include <StaticConstants.au3>
#include <GuiConstantsEx.au3>

; ===============================================================================================================================
; Description ...: This is the client side of the pipe demo
; Author ........: Paul Campbell (PaulIA)
; Notes .........:
; ===============================================================================================================================

; ===============================================================================================================================
; Global constants
; ===============================================================================================================================

Global Const $BUFSIZE = 4096
Global Const $DEFCMD = "cmd.exe /c dir c:\windows /s"
Global Const $PIPE_NAME = "\\$\\pipe\\AutoIt3"
Global Const $ERROR_MORE_DATA = 234

; ===============================================================================================================================
; Global variables
; ===============================================================================================================================

Global $iEdit, $iMemo, $iSend, $iServer, $hPipe

; ===============================================================================================================================
; Main
; ===============================================================================================================================

CreateGUI()
MsgLoop()

; ===============================================================================================================================
; Creates a GUI for the client
; ===============================================================================================================================
Func CreateGUI()
	Local $hGUI = GUICreate("Pipe Client", 500, 400, -1, -1, $WS_SIZEBOX)
	GUICtrlCreateLabel("Server:", 2, 14, 52, 20, $SS_RIGHT)
	$iServer = GUICtrlCreateEdit("<local>", 56, 10, 200, 20, $SS_LEFT)
	GUICtrlCreateLabel("Command:", 2, 36, 52, 20, $SS_RIGHT)
	$iEdit = GUICtrlCreateEdit($DEFCMD, 56, 32, 370, 20, $SS_LEFT)
	$iSend = GUICtrlCreateButton("Send", 430, 32, 60, 20)
	$iMemo = GUICtrlCreateEdit("", 0, 62, _WinAPI_GetClientWidth($hGUI), 332)
	GUICtrlSetFont($iMemo, 9, 400, 0, "Courier New")
	GUISetState()
EndFunc   ;==>CreateGUI

; ===============================================================================================================================
; Logs an error message to the display
; ===============================================================================================================================
Func LogError($sMessage)
	$sMessage &= " (" & _WinAPI_GetLastErrorMessage() & ")"
	GUICtrlSetData($iMemo, GUICtrlRead($iMemo) & $sMessage & @CRLF)
EndFunc   ;==>LogError

; ===============================================================================================================================
; Logs a message to the display
; ===============================================================================================================================
Func LogMsg($sMessage)
	GUICtrlSetData($iMemo, GUICtrlRead($iMemo) & $sMessage & @CRLF)
EndFunc   ;==>LogMsg

; ===============================================================================================================================
; MsgLoop
; ===============================================================================================================================
Func MsgLoop()
	While True
		Switch GUIGetMsg()
			Case $iSend
				SendCmd()
			Case $GUI_EVENT_CLOSE
				Exit
		EndSwitch
	WEnd
EndFunc   ;==>MsgLoop

; ===============================================================================================================================
; This function opens a pipe to the server
; ===============================================================================================================================
Func OpenPipe()
	Local $sName, $sPipe
	; Get pipe handle
	$sName = GUICtrlRead($iServer)
	If $sName = "<local>" Then $sName = "."
	$sPipe = StringReplace($PIPE_NAME, "$", $sName)
	$hPipe = _WinAPI_CreateFile($sPipe, 2, 6)
	If $hPipe <> -1 Then Return True
	LogError("OpenPipe failed")
	Return False
EndFunc   ;==>OpenPipe

; ===============================================================================================================================
; This function reads a message from the pipe
; ===============================================================================================================================
Func ReadMsg()
	Local $bSuccess, $iRead, $pBuffer, $tBuffer

	$tBuffer = DllStructCreate("char Text[4096]")
	$pBuffer = DllStructGetPtr($tBuffer)
	GUICtrlSetData($iMemo, "")
	While 1
		$bSuccess = _WinAPI_ReadFile($hPipe, $pBuffer, $BUFSIZE, $iRead, 0)
		If $iRead = 0 Then ExitLoop
		If Not $bSuccess Or (_WinAPI_GetLastError() = $ERROR_MORE_DATA) Then ExitLoop
		GUICtrlSetData($iMemo, StringLeft(DllStructGetData($tBuffer, "Text"), $iRead), 1)
	WEnd
EndFunc   ;==>ReadMsg

; ===============================================================================================================================
; This function sends a command to the server
; ===============================================================================================================================
Func SendCmd()
	If OpenPipe() Then
		SetReadMode()
		WriteMsg(GUICtrlRead($iEdit))
		ReadMsg()
		_WinAPI_CloseHandle($hPipe)
	EndIf
EndFunc   ;==>SendCmd

; ===============================================================================================================================
; This function sets the pipe read mode
; ===============================================================================================================================
Func SetReadMode()
	If Not _NamedPipes_SetNamedPipeHandleState($hPipe, 1, 0, 0, 0) Then
		LogError("SetReadMode: _NamedPipes_SetNamedPipeHandleState failed")
	EndIf
EndFunc   ;==>SetReadMode

; ===============================================================================================================================
; This function writes a message to the pipe
; ===============================================================================================================================
Func WriteMsg($sMessage)
	Local $iWritten, $iBuffer, $pBuffer, $tBuffer

	$iBuffer = StringLen($sMessage) + 1
	$tBuffer = DllStructCreate("char Text[" & $iBuffer & "]")
	$pBuffer = DllStructGetPtr($tBuffer)
	DllStructSetData($tBuffer, "Text", $sMessage)
	If Not _WinAPI_WriteFile($hPipe, $pBuffer, $iBuffer, $iWritten, 0) Then
		LogError("WriteMsg: _WinAPI_WriteFile failed")
	EndIf
EndFunc   ;==>WriteMsg
