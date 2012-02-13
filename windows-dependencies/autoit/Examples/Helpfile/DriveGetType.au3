Local $sType = DriveGetType(@HomeDrive & "\") ; Find the status of the home drive, generally this is the C:\ drive.
MsgBox(4096, "Drive Type:", $sType)
