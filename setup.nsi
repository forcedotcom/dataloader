!include target\project.nsh
!include MUI2.nsh
!include Sections.nsh
!include LogicLib.nsh
!include WordFunc.nsh

;--------------------------------------
; General
Icon "src\main\nsis\icon_SforceDL16x16.ico"
Name "${PROJECT_NAME}"
BrandingText "${PROJECT_ORGANIZATION_NAME}"

; Default install directory
InstallDir "$PROGRAMFILES\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}\"

ShowInstDetails show
AutoCloseWindow false

;Request application privileges for Windows 7
RequestExecutionLevel admin

;--------------------------------------
;Interface Settings
!define MUI_ABORTWARNING
!define MUI_ICON "src\main\nsis\icon_SforceDL32x32.ico"

;--------------------------------------
;Pages

!insertmacro MUI_PAGE_LICENSE "src\main\nsis\license.rtf"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_DIRECTORY
!insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------------
;Installer Section

Section "${PROJECT_NAME}"
  !define DL_START_MENU_DIR "$SMPROGRAMS\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}"
  !define DL_JRE_PATH "$INSTDIR\Java\bin\javaw.exe"
  !define DL_EXEC_JAR_PARAM "-Dappdata.dir=$\"$APPDATA$\" -jar $\"$INSTDIR\${PROJECT_FINAL_NAME}-uber.jar$\""
  !define DL_SMALL_ICON_PATH "$INSTDIR\icon_SforceDL16x16.ico"
  !define DL_LARGE_ICON_PATH "$INSTDIR\icon_SforceDL32x32.ico"
  !define DL_UNINSTALLER_PATH "$INSTDIR\dataloader_uninstall.exe"
  !define DL_CONFIG_DIR "$APPDATA\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME} ${PROJECT_VERSION}"

  SetOutPath "$INSTDIR"
  SectionIn RO
  SetOverwrite try
  File "target\${PROJECT_FINAL_NAME}-uber.jar"
  File "src\main\nsis\icon_SforceDL16x16.ico"
  File "src\main\nsis\icon_SforceDL32x32.ico"
  
  SetOutPath "$INSTDIR\licenses"
  File /r "src\main\nsis\licenses\"
  
  SetOutPath "$INSTDIR\samples"
  File /r "src\main\nsis\samples\"
  
  SetOutPath "$INSTDIR\bin"
  File "target\classes\encrypt.bat"
  File "target\classes\process.bat"
  
  SetOutPath "$INSTDIR\Java"
  File /r "windows-dependencies\Java\"
  
  WriteUninstaller ${DL_UNINSTALLER_PATH}
    
  ; copy config files to appdata dir
  CreateDirectory "${DL_CONFIG_DIR}"
  SetOutPath "${DL_CONFIG_DIR}"
  File "src\main\nsis\config.properties"
  
  CreateDirectory "${DL_START_MENU_DIR}"
  CreateShortCut "${DL_START_MENU_DIR}\${PROJECT_NAME}.lnk" "${DL_JRE_PATH}" "${DL_EXEC_JAR_PARAM}" "${DL_SMALL_ICON_PATH}"
  CreateShortCut "${DL_START_MENU_DIR}\Uninstall ${PROJECT_NAME}.lnk" "${DL_UNINSTALLER_PATH}"
  CreateShortCut "$DESKTOP\${PROJECT_NAME}.lnk" "${DL_JRE_PATH}" "${DL_EXEC_JAR_PARAM}" "${DL_LARGE_ICON_PATH}"

  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROJECT_NAME}" "DisplayName" "${PROJECT_ORGANIZATION_NAME} ${PROJECT_NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROJECT_NAME}" "UninstallString" "$\"${DL_UNINSTALLER_PATH}$\""
  
SectionEnd

;--------------------------------------
;Uninstaller Section

Section "Uninstall"
  SectionIn RO
  Delete "$INSTDIR\${PROJECT_FINAL_NAME}-uber.jar"
  Delete "$INSTDIR\dataloader-25.0.0-uber.jar"
  Delete "$INSTDIR\icon_SforceDL16x16.ico"
  Delete "$INSTDIR\icon_SforceDL32x32.ico"
  Delete "$INSTDIR\dataloader_uninstall.exe"
  RMDir /r "$INSTDIR\licenses"
  RMDir /r "$INSTDIR\samples"
  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\Java"
  RMDir /r "$SMPROGRAMS\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}"
  Delete "$DESKTOP\${PROJECT_NAME}.lnk"
  RMDir /r "$APPDATA\${PROJECT_ORGANIZATION_NAME}"
  
  ; delete salesforce.com directory only if it's empty
  RMDir "$SMPROGRAMS\${PROJECT_ORGANIZATION_NAME}"
  
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROJECT_NAME}"
  
SectionEnd

Function .onInit
  UserInfo::GetAccountType
  pop $0
  ${If} $0 != "admin" ;Require admin rights on NT4+
    MessageBox mb_iconstop "Administrator rights are required to run this installer."
    SetErrorLevel 740 ;ERROR_ELEVATION_REQUIRED
    Quit
  ${EndIf}
FunctionEnd