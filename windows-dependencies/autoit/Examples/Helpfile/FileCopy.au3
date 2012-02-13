FileCopy("C:\*.au3", "D:\mydir\*.*")

; Method to copy a folder (with its contents)
DirCreate("C:\new")
FileCopy("C:\old\*.*", "C:\new\")

FileCopy("C:\Temp\*.txt", "C:\Temp\TxtFiles\", 8)
; RIGHT - 'TxtFiles' is now the target directory and the file names are given by the source names

FileCopy("C:\Temp\*.txt", "C:\Temp\TxtFiles\", 9) ; Flag = 1 + 8 (overwrite + create target directory structure)
; Copy the txt-files from source to target and overwrite target files with same name
