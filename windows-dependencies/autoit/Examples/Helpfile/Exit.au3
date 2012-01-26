;First Example
Exit

;Second Example
; Terminate script if no command-line arguments
If $CmdLine[0] = 0 Then Exit (1)

;Third Example
; Open file specified as first command-line argument
Local $file = FileOpen($CmdLine[1], 0)

; Check if file opened for reading OK
If $file = -1 Then Exit (2)

; If file is empty then exit (script is successful)
Local $line = FileReadLine($file)
If @error = -1 Then Exit

;code to process file goes here
FileClose($file)
Exit ;is optional if last line of script
