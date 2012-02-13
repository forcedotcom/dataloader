''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'
' Example WSH Script (VBScript)
'
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

' Require Variants to be declared before used
Option Explicit 


''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
' Declare Variables & Objects
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

Dim oShell
Dim oAutoIt


''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
' Initialise Variables & Objects
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

Set oShell = WScript.CreateObject("WScript.Shell")
Set oAutoIt = WScript.CreateObject("AutoItX3.Control")


''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
' Start of Script
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

WScript.Echo "This script will run some test calculations"

oShell.Run "calc.exe", 1, FALSE

' Wait for the calc window to become active
oAutoIt.WinWaitActive "Calculator", ""

' Send some keystokes to calc
oAutoIt.Send "2*2="
oAutoIt.Sleep 500
oAutoIt.Send "4*4="
oAutoIt.Sleep 500
oAutoIt.Send "8*8="
oAutoIt.Sleep 500
oAutoIt.WinClose "Calc", ""
oAutoIt.WinWaitClose "Calc", ""

WScript.Quit

