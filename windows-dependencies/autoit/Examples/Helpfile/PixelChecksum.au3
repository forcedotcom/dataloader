; Wait until something changes in the region 0,0 to 50,50

; Get initial checksum
Local $checksum = PixelChecksum(0, 0, 50, 50)

; Wait for the region to change, the region is checked every 100ms to reduce CPU load
While $checksum = PixelChecksum(0, 0, 50, 50)
	Sleep(100)
WEnd

MsgBox(0, "", "Something in the region has changed!")
