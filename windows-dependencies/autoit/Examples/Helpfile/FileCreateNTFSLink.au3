FileChangeDir(@ScriptDir)

DirCreate('dir')
FileWriteLine("test.txt", "test")
MsgBox(0, "Hardlink", FileCreateNTFSLink("test.txt", "dir\test.log", 1))
