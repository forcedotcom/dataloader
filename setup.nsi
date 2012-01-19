!include target\project.nsh
!include MUI2.nsh
!include Sections.nsh

;--------------------------------------
; General
Icon "src\main\nsis\icon_SforceDL16x16.ico"
Name "${PROJECT_NAME}"

; Default install directory
InstallDir "$PROGRAMFILES\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}\"

ShowInstDetails show
AutoCloseWindow false

;Request application privileges for Windows Vista
RequestExecutionLevel user

;--------------------------------------
;Interface Settings
!define MUI_ABORTWARNING
!define MUI_ICON "src\main\nsis\icon_SforceDL16x16.ico"
!define MUI_WELCOMEPAGE_TEXT "Welcome to the installer for ${PROJECT_ORGANIZATION_NAME} ${PROJECT_NAME} ${PROJECT_VERSION}"
!define MUI_FINISHPAGE_TEXT "${PROJECT_ORGANIZATION_NAME} ${PROJECT_NAME} ${PROJECT_VERSION} has been successfully installed"

;--------------------------------------
;Pages

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "src\main\resources\licenses\wsc-license.rtf"
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
  SetOverwrite on
  File "target\${PROJECT_FINAL_NAME}-jar-with-dependencies.jar"
  File "src\main\nsis\icon_SforceDL16x16.ico"

  WriteUninstaller "$INSTDIR\dataloader_uninstall.exe"
  
  CreateDirectory "$SMPROGRAMS\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}"
  CreateShortCut "$SMPROGRAMS\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}\Dataloader.lnk" \
    "$INSTDIR\${PROJECT_FINAL_NAME}-jar-with-dependencies.jar" "" "$INSTDIR\icon_SforceDL16x16.ico"
  CreateShortCut "$SMPROGRAMS\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}\Uninstall Dataloader.lnk" \
    "$INSTDIR\dataloader_uninstall.exe"
  CreateShortCut "$DESKTOP\Dataloader.lnk" "$INSTDIR\${PROJECT_FINAL_NAME}-jar-with-dependencies.jar" "" "$INSTDIR\icon_SforceDL16x16.ico"

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