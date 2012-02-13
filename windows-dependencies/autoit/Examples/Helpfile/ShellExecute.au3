; Open Notepad
ShellExecute("notepad.exe")

; Open a .txt file with it's default editor
ShellExecute("myfile.txt", "", @ScriptDir, "edit")
