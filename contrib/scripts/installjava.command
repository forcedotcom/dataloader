#!/bin/bash

promptForJavaInstallation() {
    while true
    do
        prompt="Do you want to install Eclipse Temurin Java using sdkman from sdkman.io ? [Yes/No] "
        read -r -p "$prompt" input
        case $input in
            [yY][eE][sS]|[yY])
                installSdkmanAndJava
                break
                ;;
            [nN][oO]|[nN])
                exit 0
                ;;
            *)
                echo "Type Yes or No."
                ;;
        esac
    done
}

installSdkmanAndJava() {
    source "$HOME/.sdkman/bin/sdkman-init.sh" >& /dev/null
    sdk version >& /dev/null
    if [ $? -eq 0 ]
    then
        sdk selfupdate force
    else
        curl -s "https://get.sdkman.io" | bash
    fi
    
    if [ $? -eq 0 ] # succeeded in installing sdkman
    then
        source "$HOME/.sdkman/bin/sdkman-init.sh"
        sdk install java
    fi
    
    if [ $? -ne 0 ] # did not successfully install sdkman or java
    then
        echo "Unable to install Java. Try manual installation."
        echo
    fi
}

echo
echo
echo NOTICE:
echo Read this notice carefully before proceeding.
echo .
echo THIS SOFTWARE IS NOT SUPPORTED BY SALESFORCE INC.
echo SALESFORCE INC. DOES NOT ENDORSE OR SUPPORT ANY 3RD PARTY SOFTWARE DOWNLOADED OR INSTALLED BY THIS SOFTWARE.
echo THIS SOFTWARE DOWNLOADS AND INSTALLS 3RD PARTY SOFTWARE INCLUDING BUT NOT LIMITED TO SDKMAN, JAVA.
echo
echo THIS SOFTWARE IS NOT SUPPORTED BY SALESFORCE INC.
echo
echo "Copyright (c) 2023, Salesforce, inc."
echo
echo "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED \
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A \
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR \
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED \
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) \
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING \
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE OR THE USE OF 3RD PARTY SOFTWARE \
DOWNLOADED OR INSTALLED BY THIS SOFTWARE, REGARDLESS OF BEING ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
echo
echo
promptForJavaInstallation