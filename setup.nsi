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

;Request application privileges for Windows Vista
RequestExecutionLevel user

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
  SetOutPath "$INSTDIR"
  SectionIn RO
  SetOverwrite try
  File "target\${PROJECT_FINAL_NAME}-jar-with-dependencies.jar"
  File "src\main\nsis\icon_SforceDL16x16.ico"
  SetOutPath "$INSTDIR\Java"
  File /r "windows-dependencies\Java\"

  WriteUninstaller "$INSTDIR\dataloader_uninstall.exe"

  !define DL_START_MENU_DIR "$SMPROGRAMS\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}"
  !define DL_JRE_PATH "$INSTDIR\Java\bin\javaw"
  !define DL_EXEC_JAR_PARAM "-jar $\"$INSTDIR\${PROJECT_FINAL_NAME}-jar-with-dependencies.jar$\""
  !define DL_ICON_PATH "$INSTDIR\icon_SforceDL16x16.ico"
  
  CreateDirectory ${DL_START_MENU_DIR}
  CreateShortCut "${DL_START_MENU_DIR}\Dataloader.lnk" "${DL_JRE_PATH}" "${DL_EXEC_JAR_PARAM}" "${DL_ICON_PATH}"
  CreateShortCut "${DL_START_MENU_DIR}\Uninstall Dataloader.lnk" "$INSTDIR\dataloader_uninstall.exe"
  CreateShortCut "$DESKTOP\Dataloader.lnk" "${DL_JRE_PATH}" "${DL_EXEC_JAR_PARAM}" "${DL_ICON_PATH}"

  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Dataloader" "DisplayName" "${PROJECT_ORGANIZATION_NAME} ${PROJECT_NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Dataloader" "UninstallString" "$\"$INSTDIR\dataloader_uninstall.exe$\""
  
SectionEnd

;--------------------------------------
;Uninstaller Section

Section "Uninstall"
  SectionIn RO
  RMDir /r "$INSTDIR"
  RMDir /r "$SMPROGRAMS\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}"
  Delete "$DESKTOP\Dataloader.lnk"
  
  ; delete salesforce.com directory only if it's empty
  RMDir "$SMPROGRAMS\${PROJECT_ORGANIZATION_NAME}"
  
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Dataloader"
  
SectionEnd