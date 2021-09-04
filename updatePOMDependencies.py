#/usr/bin/env python3

import requests, zipfile, io, shutil, os, sys
import subprocess
from bs4 import BeautifulSoup
from os.path import expanduser
import urllib.request
import re

####################################################################
# Usage:
# python3 swtinstall.py <git clone root>
# 
# Prerequisites:
# - Python 3.9 or higher
# - Directory containing mvn command in PATH environment variable.
# - Python BeautifulSoup installed locally. Run 'pip3 install beautifulsoup4'
# - Python requests installed locally. Run 'pip3 install requests'
#
# Side-effects:
# - Zip content extracted in ~/Downloads directory
# - SWT jar files installed in <git clone root>/local-proj-repo subdirectories
#
# Follow-on manual steps:
# - Update version value for SWT dependencies in pom.xml with the downloaded SWT version.
#
# Outline of the steps taken:
# - Start at https://download.eclipse.org/eclipse/downloads/
# - Go to "Latest Release" section
# - Click on the first link in the "Build Name" column
# - Go to "SWT Binary and Source" section
# - Click on the links next to "Windows (64 bit version)", "Mac OSX (64 bit version)", and "Mac OSX (64 bit version for Arm64/AArch64)"
# - Extract the contents of the zip file
# - Go to the extraction folder and run mvn install:install-file command
#
####################################################################

def is_exe(fpath):
    return os.path.isfile(fpath) and os.access(fpath, os.X_OK)

def which(program):
    fpath, fname = os.path.split(program)
    if fpath:
        if is_exe(program):
            return program
    else:
        for path in os.environ["PATH"].split(os.pathsep):
            exe_file = os.path.join(path, program)
            if is_exe(exe_file):
                return exe_file

    return None

###########################################################

def getSWTDownloadLinkForPlatform(soup, platformString):
    results = soup.find(id="SWT").find_next("td").string
    while results != None and results != platformString :
        results = results.find_next("td").string

    if results == platformString :
        results = results.find_next("a")['href']

    return results

######## end of getSWTDownloadLinkForPlatform ##########

def downloadAndExtractZip(url):
    zipfileName = url.split('=',1)[1]

    home = expanduser("~")
    unzippedDirName = home + "/Downloads/" + zipfileName.removesuffix('.zip') + "/"
    print(unzippedDirName)

    page = requests.get(url)
    soup = BeautifulSoup(page.content, "html.parser")
    zipURL = soup.find("meta").find_next("a")['href']
#    print(zipURL)

    page = requests.get(zipURL)
    soup = BeautifulSoup(page.content, "html.parser")
    zipURL = soup.find(id="novaContent").find_next("a").find_next("a")['href']
    zipURL = "https://www.eclipse.org/downloads/" + zipURL
#    print(zipURL)

    # navigate the redirect to the actual mirror
    page = requests.get(zipURL)
    soup = BeautifulSoup(page.content, "html.parser")
    zipURL = soup.find('meta', attrs={'http-equiv': 'Refresh'})['content'].split(';')[1].split('=')[1]
#    print(zipURL)
    
    # delete existing content
    if os.path.exists(unzippedDirName) and os.path.isdir(unzippedDirName):
        shutil.rmtree(unzippedDirName)
    response = requests.get(zipURL, stream=True)
    z = zipfile.ZipFile(io.BytesIO(response.content))
    z.extractall(unzippedDirName)

    return unzippedDirName

######## end of downloadAndExtractZip ##########

def installInLocalMavenRepo(unzippedSWTDir, mvnArtifactId, gitCloneRootDir):
#     command to execute
    swtVersion = unzippedSWTDir.split('-')[1]
#    print(swtVersion)

    if which("mvn") == None :
        print("did not find mvn command in the execute path")
        sys.exit(2)
        
    mavenCommand = "mvn install:install-file " \
                    + "-Dfile=" + unzippedSWTDir + "swt.jar " \
                    + "-DgroupId=local.swt " \
                    + "-DartifactId=" + mvnArtifactId + " " \
                    + "-Dversion=" + swtVersion + " " \
                    + "-Dpackaging=jar " \
                    + "-Dmaven.repo.local=" + gitCloneRootDir + "/local-proj-repo"
#    print(mavenCommand)
    subprocess.run(mavenCommand, shell=True)

######## end of installInLocalMavenRepo  ##########

def getLatestSWTVersionFromLocalRepo(gitCloneRootDir, mvnArtifactId):
    swtContentList = os.listdir(gitCloneRootDir 
                         + "/local-proj-repo/local/swt/" + mvnArtifactId)
    maxNumberItem = "1.0"
    for item in swtContentList:
        if ".xml" in item:
            continue
        if float(item) > float(maxNumberItem):
            maxNumberItem = item
    return maxNumberItem

######## end of getLatestSWTVersionFromLocalRepo ##############

def updatePOMForSWTVersion(gitCloneRootDir, mvnArtifactId, version):
    newPom = open(gitCloneRootDir + "/pom.xml.new", "w")

    with open(gitCloneRootDir + '/pom.xml') as pom:
        for line in pom:
            output = line
            if "<artifactId>"+mvnArtifactId+"</artifactId>" in line:
                newPom.write(output)
                line = pom.readline()
                leadingWhitespace = line.split('<')[0]
                output = leadingWhitespace + "<version>" + version + "</version>\n" 

            newPom.write(output)
       
    newPom.close()
    pom.close()
    os.rename(gitCloneRootDir + '/pom.xml', gitCloneRootDir + '/pom.xml.save')
    os.rename(gitCloneRootDir + '/pom.xml.new', gitCloneRootDir + '/pom.xml')

######## end of updatePOMForSWTVersion ########################

def getLatestArtifactVersion(artifactURL):
    hdr = {
        'User-Agent': 'Mozilla/5.0', 
        'Accept-Language': 'en-US,en;q=0.8'
        }

    req = urllib.request.Request(
        artifactURL,
        headers=hdr
        )

    page = ""
    with urllib.request.urlopen(req) as f:
        line = f.read().decode('utf-8')
        page = page + line

    content = BeautifulSoup(page, "html.parser").find('table', attrs={'class': 'grid versions'})
    versionNumber = content.find('a').contents[0]
    return versionNumber

######## end of getLatestArtifactVersion ###########

def updatePOMForRemoteMVNArtifacts(gitCloneRootDir):
    newPom = open(gitCloneRootDir + "/pom.xml.new", "w")
    latestArtifactVersion = ""

    with open(gitCloneRootDir + '/pom.xml') as pom:
        for line in pom:
            output = line
            if "<!-- https://mvnrepository.com/artifact/" in line:
                artifactURL = line.split(' ')[1]
                print(artifactURL)
                latestArtifactVersion = getLatestArtifactVersion(artifactURL)

            if "</dependency>" or "</plugin>" in line:
                latestArtifactVersion = ""

            if latestArtifactVersion != "" and "<version>" in line:
                leadingWhitespace = line.split('<')[0]
                output = leadingWhitespace + "<version>" \
                           + latestArtifactVersion + "</version>\n"

            newPom.write(output)

    newPom.close()
    pom.close()
    os.rename(gitCloneRootDir + '/pom.xml', gitCloneRootDir + '/pom.xml.save')
    os.rename(gitCloneRootDir + '/pom.xml.new', gitCloneRootDir + '/pom.xml')

######## end of updatePOMForRemoteMVNArtifacts ########################

def updateSWTAndPOM(mvnArtifactId, downloadPageLabel, gitCloneRootDir):
    URL = "https://download.eclipse.org/eclipse/downloads/"
    page = requests.get(URL)

    soup = BeautifulSoup(page.content, "html.parser")
    results = soup.find(id="Latest_Release").find_next("a")['href']

    downloadsPage = URL + results
    page = requests.get(downloadsPage)
    soup = BeautifulSoup(page.content, "html.parser")
    
    results = getSWTDownloadLinkForPlatform(soup, downloadPageLabel)
    unzippedDir = downloadAndExtractZip(downloadsPage + results)
    installInLocalMavenRepo(unzippedDir, mvnArtifactId, gitCloneRootDir)
    latestVersion = getLatestSWTVersionFromLocalRepo(gitCloneRootDir, mvnArtifactId)
    updatePOMForSWTVersion(gitCloneRootDir, mvnArtifactId, latestVersion)

######## end of updateSWTAndPOM #########################


if (len(sys.argv) < 2):
   print('Usage: python3 swtinstall.py <git clone root>')
   sys.exit(2)

# Windows
updateSWTAndPOM("swtwin32_x86_64", "Windows (64 bit version)", sys.argv[1])

# Mac x86
updateSWTAndPOM("swtmacx86_64", "Mac OSX (64 bit version)", sys.argv[1])

# Mac ARM
updateSWTAndPOM("swtmacarm64", "Mac OSX (64 bit version for Arm64/AArch64)", sys.argv[1])

# Linux
updateSWTAndPOM("swtlinux_x86_64", "Linux (64 bit version)", sys.argv[1])

# update other dependencies in POM
updatePOMForRemoteMVNArtifacts(sys.argv[1])