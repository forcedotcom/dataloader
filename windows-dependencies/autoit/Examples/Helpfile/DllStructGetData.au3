; Create the DLL structure to use in the DLLCall function.
Local $tagOSVERSIONINFO = DllStructCreate('dword dwOSVersionInfoSize;dword dwMajorVersion;dword dwMinorVersion;dword dwBuildNumber;dword dwPlatformId;char szCSDVersion[128]')

; Update the 'size element' in the structure by using DllStructGetSize to retrieve the total size of the structure.
DllStructSetData($tagOSVERSIONINFO, 'dwOSVersionInfoSize', DllStructGetSize($tagOSVERSIONINFO))

; Call the API function 'GetVersionEx' using DLLCall and passing the structure.
Local $aReturn = DllCall('kernel32.dll', 'int', 'GetVersionEx', 'struct*', $tagOSVERSIONINFO)
If @error Or Not $aReturn[0] Then
	MsgBox(0, "DLLCall Error", "An error occurred when retrieving the Operating System information.")
EndIf

; Get specific data from the element strings.
Local $iMajorVersion = DllStructGetData($tagOSVERSIONINFO, 'dwMajorVersion')
Local $iMinorVersion = DllStructGetData($tagOSVERSIONINFO, 'dwMinorVersion')
Local $iBuildNumber = DllStructGetData($tagOSVERSIONINFO, 'dwBuildNumber')
Local $sServicePack = DllStructGetData($tagOSVERSIONINFO, 'szCSDVersion')

; Free the structure.
$tagOSVERSIONINFO = 0

MsgBox(0, "Operating System information", "Major version: " & $iMajorVersion & @CRLF & _
		"Minor version: " & $iMinorVersion & @CRLF & _
		"Build: " & $iBuildNumber & @CRLF & _
		"Service Pack: " & $sServicePack & @CRLF)
