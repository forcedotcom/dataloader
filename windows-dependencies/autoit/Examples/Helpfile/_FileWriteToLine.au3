#include <File.au3>
;Example: Write to line 3 of c:\test.txt REPLACING line 3
_FileWriteToLine("c:\test.txt", 3, "my replacement for line 3", 1)
;Example: Write to line 3 of c:\test.txt NOT REPLACING line 3
_FileWriteToLine("c:\test.txt", 3, "my insertion", 0)