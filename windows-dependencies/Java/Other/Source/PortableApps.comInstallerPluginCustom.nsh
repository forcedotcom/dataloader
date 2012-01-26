!macro CustomCodePostInstall
	ExecDOS::exec '"$INSTDIR\bin\unpack200.exe" -r -q "$INSTDIR\lib\charsets.pack" "$INSTDIR\lib\charsets.jar"' "" ""
	Pop $R0
	ExecDOS::exec '"$INSTDIR\bin\unpack200.exe" -r -q "$INSTDIR\lib\deploy.pack" "$INSTDIR\lib\deploy.jar"' "" ""
	Pop $R0
	ExecDOS::exec '"$INSTDIR\bin\unpack200.exe" -r -q "$INSTDIR\lib\javaws.pack" "$INSTDIR\lib\javaws.jar"' "" ""
	Pop $R0
	ExecDOS::exec '"$INSTDIR\bin\unpack200.exe" -r -q "$INSTDIR\lib\jsse.pack" "$INSTDIR\lib\jsse.jar"' "" ""
	Pop $R0
	ExecDOS::exec '"$INSTDIR\bin\unpack200.exe" -r -q "$INSTDIR\lib\plugin.pack" "$INSTDIR\lib\plugin.jar"' "" ""
	Pop $R0
	ExecDOS::exec '"$INSTDIR\bin\unpack200.exe" -r -q "$INSTDIR\lib\rt.pack" "$INSTDIR\lib\rt.jar"' "" ""
	Pop $R0
	ExecDOS::exec '"$INSTDIR\bin\unpack200.exe" -r -q "$INSTDIR\lib\ext\localedata.pack" "$INSTDIR\lib\ext\localedata.jar"' "" ""
	Pop $R0
	CopyFiles /silent "$INSTDIR\bin\npdeployJava1.dll" "$INSTDIR\bin\new_plugin"
	CopyFiles /silent "$INSTDIR\bin\msvcr71.dll" "$INSTDIR\bin\new_plugin"
!macroend
