; Create a shortcut on the desktop to explorer.exe and set the hotkey combination Ctrl+Alt+T or in AutoIt ^!t.
FileCreateShortcut(@WindowsDir & "\explorer.exe", @DesktopDir & "\Shortcut Example.lnk", @WindowsDir, "/e,c:\", "Tooltip description of the shortcut.", @SystemDir & "\shell32.dll", "^!t", "15", @SW_MINIMIZE)
