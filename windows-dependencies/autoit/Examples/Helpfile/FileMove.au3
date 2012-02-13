FileMove("C:\foo.au3", "D:\mydir\bak.au3")

; Second example:
;	uses flags '1' (owerwriting) and '8' (autocreating target dir structure) together
;	moves all txt-files from temp to txtfiles and prechecks if
;	target directory structure exists, if not then automatically creates it
FileMove(@TempDir & "\*.txt", @TempDir & "\TxtFiles\", 9)
