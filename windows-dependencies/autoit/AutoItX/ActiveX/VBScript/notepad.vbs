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

WScript.Echo "This script will run notepad and type in some text"

oShell.Run "notepad.exe", 1, FALSE

' Wait for the Notepad window to become active
oAutoIt.WinWaitActive "Untitled - Notepad", ""

' Send some keystokes to notepad
oAutoIt.Send "Hello, this is line 1{ENTER}"
oAutoIt.Send "This is line 2{ENTER}This is line 3"
oAutoIt.Sleep 1000
oAutoIt.Send "!{F4}"
oAutoIt.WinWaitActive "Notepad", "save the changes?"
oAutoIt.Send "!n"
oAutoIt.WinWaitClose "Untitled - Notepad", ""


WScript.Quit

