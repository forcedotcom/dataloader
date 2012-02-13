Local $sFileSystem = DriveGetFileSystem(@HomeDrive & "\") ; Find the file system type of the home drive, generally this is the C:\ drive.
MsgBox(4096, "File System Type:", $sFileSystem)
