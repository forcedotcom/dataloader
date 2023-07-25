@echo off
echo .
echo .
echo NOTICE:
echo Read this notice carefully before proceeding.
echo .
echo THIS SOFTWARE IS NOT SUPPORTED BY SALESFORCE INC.
echo SALESFORCE INC. DOES NOT ENDORSE OR SUPPORT ANY 3RD PARTY SOFTWARE DOWNLOADED OR INSTALLED BY THIS SOFTWARE.
echo THIS SOFTWARE DOWNLOADS AND INSTALLS 3RD PARTY SOFTWARE INCLUDING BUT NOT LIMITED TO SCOOP, 7ZIP, GIT, JAVA.
echo .
echo Copyright (c) 2023, Salesforce, inc.
echo .
echo THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
echo PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR 
echo ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
echo TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
echo NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE OR THE USE OF 3RD PARTY SOFTWARE
echo DOWNLOADED OR INSTALLED BY THIS SOFTWARE, REGARDLESS OF BEING ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
echo .
echo .
CALL :promptForJavaInstallation
pause
EXIT /b %ERRORLEVEL%

:promptForJavaInstallation
    echo .
    echo The script will download and install Temurin Java and associated dependencies
    echo using scoop from from https://github.com/ScoopInstaller/Scoop .
    echo It will also install the needed dependencies: 7zip, and git client.
    set prompt="Do you want to install Java? [Yes/No] "
    SET /p REPLY=%prompt%
    if /I "%REPLY%"=="Y" goto :installScoopAndJava
    if /I "%REPLY%"=="Yes" goto :installScoopAndJava
    if /I "%REPLY%"=="N" EXIT 0
    if /I "%REPLY%"=="No" EXIT 0
    echo Type Yes or No.
    goto :promptForJavaInstallation
    EXIT /b 0

:installScoopAndJava
    set JRE_NAME=temurin17-jre
    set "SCOOP_CMD_FILE=%UserProfile%\scoop\shims\scoop.ps1"
    if NOT EXIST %SCOOP_CMD_FILE% (
        Powershell.exe -executionpolicy remotesigned "irm get.scoop.sh | iex"
    )
    if NOT "%ERRORLEVEL%" == "0" (
        goto :exitWithJavaDownloadMessage
    )
    echo going to install 7zip
    CALL :execScoop install 7zip
    echo going to update 7zip
    CALL :execScoop update 7zip
    echo going to install git
    CALL :execScoop install git
    echo going to update git
    CALL :execScoop update git
    echo going to remove java bucket 
    CALL :execScoop bucket rm java
    echo going to add java bucket 
    CALL :execScoop bucket add java
    echo going to install java 
    CALL :execScoop install %JRE_NAME%
    echo going to update java
    CALL :execScoop update %JRE_NAME%
    echo installed JRE
    EXIT /b 0
    
:execScoop
    powershell.exe -noprofile -ex unrestricted -file "%SCOOP_CMD_FILE%" %*
    EXIT /b %ERRORLEVEL%
