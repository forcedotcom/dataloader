; Include a  bitmap found in "C:\test.bmp" with the compiled program and put it in "D:\mydir\test.bmp" when it is run
Local $b = True
If $b = True Then FileInstall("C:\test.bmp", "D:\mydir\test.bmp")
