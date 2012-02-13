Local $iTotalSpace = DriveSpaceTotal(@HomeDrive & "\") ; Find the total disk space of the home drive, generally this is the C:\ drive.
MsgBox(4096, "Total Space:", $iTotalSpace & " MB")
