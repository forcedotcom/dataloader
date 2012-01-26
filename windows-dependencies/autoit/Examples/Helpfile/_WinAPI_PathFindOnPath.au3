#include <WinAPI.au3>

MsgBox(0, "PathFindOnPath Example", _
		StringFormat("Full path of notepad.exe:\n%s\n\n" & _
		"Find ntuser.dat in profile folder, using custom paths:\n%s", _
		_WinAPI_PathFindOnPath("notepad.exe"), _WinAPI_PathFindOnPath("ntuser.dat", @UserProfileDir) _
		))
