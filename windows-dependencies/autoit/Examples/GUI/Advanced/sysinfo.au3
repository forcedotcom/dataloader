#include <GuiConstantsEx.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $VOL, $SERIAL, $TOTAL, $FREE
	Local $Input_ComputerName, $Input_CurrentUserName, $Input_OperatingSystem
	Local $Input_ServicePack, $Input_VolumeLabel, $Input_SerialNumber
	Local $Input_TotalSpace, $Input_FreeSpace, $Input_IpAddress, $Input_StartupDirectory
	Local $Input_WindowsDirectory, $Input_SystemFolderDirectory, $Input_DesktopDirectory
	Local $Input_MyDocumentsDirectory, $Input_ProgramFilesDirectory, $Input_StartMenuDirectory
	Local $Input_TemporaryFileDirectory, $Input_DesktopWidth, $Input_DesktopHeight
	Local $Input_Date, $Input_Time, $msg

	#forceref $Input_ComputerName, $Input_CurrentUserName, $Input_OperatingSystem
	#forceref $Input_ServicePack, $Input_VolumeLabel, $Input_SerialNumber
	#forceref $Input_TotalSpace, $Input_FreeSpace, $Input_IpAddress, $Input_StartupDirectory
	#forceref $Input_WindowsDirectory, $Input_SystemFolderDirectory, $Input_DesktopDirectory
	#forceref $Input_MyDocumentsDirectory, $Input_ProgramFilesDirectory, $Input_StartMenuDirectory
	#forceref $Input_TemporaryFileDirectory, $Input_DesktopWidth, $Input_DesktopHeight
	#forceref $Input_Date, $Input_Time

	GUICreate("Computer Information - By : Para", 469, 639, (@DesktopWidth - 469) / 2, (@DesktopHeight - 639) / 2, $WS_OVERLAPPEDWINDOW + $WS_VISIBLE + $WS_CLIPSIBLINGS)

	$VOL = DriveGetLabel("C:\")
	$SERIAL = DriveGetSerial("C:\")
	$TOTAL = DriveSpaceTotal("C:\")
	$FREE = DriveSpaceFree("C:\")

	GUICtrlCreateLabel("Computer Name", 10, 10, 150, 20)
	GUICtrlCreateLabel("Current User Name", 10, 40, 150, 20)
	GUICtrlCreateLabel("Operating System", 10, 70, 150, 20)
	GUICtrlCreateLabel("Service Pack", 10, 100, 150, 20)
	GUICtrlCreateLabel("C: Volume Label", 10, 130, 150, 20)
	GUICtrlCreateLabel("C: Serial Number", 10, 160, 150, 20)
	GUICtrlCreateLabel("C: Total Space", 10, 190, 150, 20)
	GUICtrlCreateLabel("C: Free Space", 10, 220, 150, 20)
	GUICtrlCreateLabel("Ip Address", 10, 250, 150, 20)
	GUICtrlCreateLabel("Startup Directory", 10, 280, 150, 20)
	GUICtrlCreateLabel("Windows Directory", 10, 310, 150, 20)
	GUICtrlCreateLabel("System Folder Directory", 10, 340, 150, 20)
	GUICtrlCreateLabel("Desktop Directory", 10, 370, 150, 20)
	GUICtrlCreateLabel("My Documents Directory", 10, 400, 150, 20)
	GUICtrlCreateLabel("Program File Directory", 10, 430, 150, 20)
	GUICtrlCreateLabel("Start Menu Directory", 10, 460, 150, 20)
	GUICtrlCreateLabel("Desktop Width (Pixels)", 10, 520, 150, 20)
	GUICtrlCreateLabel("Temporary File Directory", 10, 490, 150, 20)
	GUICtrlCreateLabel("Desktop Height (Pixels)", 10, 550, 150, 20)
	GUICtrlCreateLabel("Date", 10, 580, 150, 20)
	GUICtrlCreateLabel("Time", 10, 610, 150, 20)
	$Input_ComputerName = GUICtrlCreateInput("" & @ComputerName, 180, 10, 280, 20)
	$Input_CurrentUserName = GUICtrlCreateInput("" & @UserName, 180, 40, 280, 20)
	$Input_OperatingSystem = GUICtrlCreateInput("" & @OSType, 180, 70, 280, 20)
	$Input_ServicePack = GUICtrlCreateInput("" & @OSServicePack, 180, 100, 280, 20)
	$Input_VolumeLabel = GUICtrlCreateInput("" & $VOL, 180, 130, 280, 20)
	$Input_SerialNumber = GUICtrlCreateInput("" & $SERIAL, 180, 160, 280, 20)
	$Input_TotalSpace = GUICtrlCreateInput("" & $TOTAL, 180, 190, 280, 20)
	$Input_FreeSpace = GUICtrlCreateInput("" & $FREE, 180, 220, 280, 20)
	$Input_IpAddress = GUICtrlCreateInput("" & @IPAddress1, 180, 250, 280, 20)
	$Input_StartupDirectory = GUICtrlCreateInput("" & @StartupDir, 180, 280, 280, 20)
	$Input_WindowsDirectory = GUICtrlCreateInput("" & @WindowsDir, 180, 310, 280, 20)
	$Input_SystemFolderDirectory = GUICtrlCreateInput("" & @SystemDir, 180, 340, 280, 20)
	$Input_DesktopDirectory = GUICtrlCreateInput("" & @DesktopDir, 180, 370, 280, 20)
	$Input_MyDocumentsDirectory = GUICtrlCreateInput("" & @MyDocumentsDir, 180, 400, 280, 20)
	$Input_ProgramFilesDirectory = GUICtrlCreateInput("" & @ProgramFilesDir, 180, 430, 280, 20)
	$Input_StartMenuDirectory = GUICtrlCreateInput("" & @StartMenuDir, 180, 460, 280, 20)
	$Input_TemporaryFileDirectory = GUICtrlCreateInput("" & @TempDir, 180, 490, 280, 20)
	$Input_DesktopWidth = GUICtrlCreateInput("" & @DesktopWidth, 180, 520, 280, 20)
	$Input_DesktopHeight = GUICtrlCreateInput("" & @DesktopHeight, 180, 550, 280, 20)
	$Input_Date = GUICtrlCreateInput("(MONTH)(DAY)(YEAR) " & @MON & "-" & @MDAY & "-" & @YEAR, 180, 580, 280, 20)
	$Input_Time = GUICtrlCreateInput("(HOUR)(MIN)(SEC) " & @HOUR & ":" & @MIN & ":" & @SEC, 180, 610, 280, 20)

	GUISetState()
	While 1
		$msg = GUIGetMsg()
		Select
			Case $msg = $GUI_EVENT_CLOSE
				ExitLoop
			Case Else
				;;;
		EndSelect
	WEnd
	Exit
EndFunc   ;==>_Main
