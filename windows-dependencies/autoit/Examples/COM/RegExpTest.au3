; Regular Expression test using VBScript.RegExp object
;
; Requirements:
; AutoItCOM    Version 3.1.0
; VBscript.DLL version 5.0 or higher.
;
; Source: http://msdn.microsoft.com/library/en-us/script56/html/vsobjregexp.asp


Func RegExpTest($patrn, $strng)

   $Retstr = ""

   $regEx = ObjCreate("VBScript.RegExp") ; Create a regular expression.

   $regEx.Pattern = $patrn   ; Set pattern.
   $regEx.IgnoreCase = 1     ; Set case insensitivity: True.
   $regEx.Global = 1         ; Set global applicability: True.
   $Matches = $regEx.Execute($strng)   ; Execute search.

   For $Match in $Matches   ; Iterate Matches collection.
      $RetStr = $RetStr & "Match found at position "
      $RetStr = $RetStr & $Match.FirstIndex & ". Match Value is '"
      $RetStr = $RetStr & $Match.Value & "'." & @CRLF
   Next

   $regEx = ""

   Return $RetStr
EndFunc



MsgBox(0,"Test RegExp", RegExpTest("is.", "IS1 is2 IS3 is4"))
