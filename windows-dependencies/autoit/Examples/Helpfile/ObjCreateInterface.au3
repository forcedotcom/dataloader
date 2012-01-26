Example()

Func Example()
	; Declare the CLSID, IID and interface description for ITaskbarList.
	; It is not necessary to describe the members of IUnknown.
	Local Const $sCLSID_TaskbarList = "{56FDF344-FD6D-11D0-958A-006097C9A090}"
	Local Const $sIID_ITaskbarList = "{56FDF342-FD6D-11D0-958A-006097C9A090}"
	Local Const $sTagITaskbarList = "HrInit hresult(); AddTab hresult(hwnd); DeleteTab hresult(hwnd); ActivateTab hresult(hwnd); SetActiveAlt hresult(hwnd);"

	; Create the object.
	Local $oTaskbarList = ObjCreateInterface($sCLSID_TaskbarList, $sIID_ITaskbarList, $sTagITaskbarList)

	; Initialize the iTaskbarList object.
	$oTaskbarList.HrInit()

	; Run Notepad.
	Run("notepad.exe")

	; Wait for the Notepad window to appear and get a handle to it.
	Local $hNotepad = WinWait("[CLASS:Notepad]")


	; Tell the user what to look for.
	MsgBox(4096, "", "Look in the Taskbar and you should see an entry for Notepad." & @CRLF & @CRLF & "Press OK to continue.")

	; Delete the Notepad entry from the Taskbar.
	$oTaskbarList.DeleteTab($hNotepad)

	; Tell the user to look again.
	MsgBox(4096, "", "Look in the Taskbar.  There should no longer be a Notepad entry but Notepad is still running." & @CRLF & @CRLF & "Press OK to continue.")

	; Close Notepad.
	WinClose($hNotepad)
EndFunc   ;==>Example
