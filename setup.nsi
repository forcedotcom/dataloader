!include target\project.nsh
!include MUI2.nsh
!include Sections.nsh

Icon "src\main\nsis\icon_SforceDL16x16.ico"
Name "${PROJECT_NAME}"
InstallDir "C:\Program Files\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}\"
ShowInstDetails show
AutoCloseWindow false

!define MUI_ICON "src\main\nsis\icon_SforceDL16x16.ico"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "src\main\resources\licenses\WSC_license.html"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
; !insertmacro MUI_PAGE_STARTMENU "Application" "${PROJECT_STARTMENU_FOLDER}"
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_COMPONENTS
!insertmacro MUI_UNPAGE_DIRECTORY
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

Section "${PROJECT_NAME}"
  SectionIn RO
  SetOutPath $INSTDIR
  SetOverwrite on
  File "target\${PROJECT_FINAL_NAME}-jar-with-dependencies.jar"
  writeUninstaller "$INSTDIR\tomighty_uninstall.exe"
SectionEnd

Section "uninstall"
  RMDir $INSTDIR
SectionEnd