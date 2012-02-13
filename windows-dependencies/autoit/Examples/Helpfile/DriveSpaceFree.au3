Local $iFreeSpace = DriveSpaceFree(@HomeDrive & "\") ; Find the free disk space of the home drive, generally this is the C:\ drive.
MsgBox(4096, "Free Space:", $iFreeSpace & " MB")
