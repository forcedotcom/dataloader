; Write a single REG_SZ value
RegWrite("HKEY_CURRENT_USER\Software\Test", "TestKey", "REG_SZ", "Hello this is a test")

; Write the REG_MULTI_SZ value of "line1" and "line2"
RegWrite("HKEY_CURRENT_USER\Software\Test", "TestKey1", "REG_MULTI_SZ", "line1" & @LF & "line2")

; Write the REG_MULTI_SZ value of "line1"
RegWrite("HKEY_CURRENT_USER\Software\Test", "TestKey2", "REG_MULTI_SZ", "line1")

; always add and extra null string
RegWrite("HKEY_CURRENT_USER\Software\Test", "TestKey3", "REG_MULTI_SZ", "line1" & @LF & "line2" & @LF)
RegWrite("HKEY_CURRENT_USER\Software\Test", "TestKey4", "REG_MULTI_SZ", "line1" & @LF & @LF & "line2" & @LF)

; empty REG_MULTI_SZ
RegWrite("HKEY_CURRENT_USER\Software\Test", "TestKey5", "REG_MULTI_SZ", "")

; create just the key
RegWrite("HKEY_CURRENT_USER\Software\Test1")
