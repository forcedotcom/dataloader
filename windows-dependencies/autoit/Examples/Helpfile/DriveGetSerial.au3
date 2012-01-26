Local $sSerial = DriveGetSerial(@HomeDrive & "\") ; Find the serial number of the home drive, generally this is the C:\ drive.
MsgBox(4096, "Serial Number: ", $sSerial)
