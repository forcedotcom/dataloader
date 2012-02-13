
; Map X drive to \\myserver\stuff using current user
DriveMapAdd("X:", "\\myserver\stuff")

; Get details of the mapping
MsgBox(0, "Drive X: is mapped to", DriveMapGet("X:"))

