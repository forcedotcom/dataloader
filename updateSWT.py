#!/usr/bin/env python3

import requests, zipfile, io, shutil, os, sys
import subprocess
from bs4 import BeautifulSoup
from os.path import expanduser
import urllib.request
import re
import argparse
import atexit

####################################################################
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

LOCAL_REPO_DIR = "./local-proj-repo"
LOCAL_REPO_SAVE_DIR = "../local-proj-repo-save"

def is_exe(fpath):
    return os.path.isfile(fpath) and os.access(fpath, os.X_OK)

def cleanupBeforeExit():
    if os.path.isdir(LOCAL_REPO_SAVE_DIR):
        # restore local SWT repo before exiting
        shutil.move(LOCAL_REPO_SAVE_DIR + "/", LOCAL_REPO_DIR)
        shutil.rmtree(LOCAL_REPO_SAVE_DIR)
        
def exitWithError(errorStr):
    print(errorStr)
    sys.exit(-1)


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
#    print(unzippedDirName)

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
        exitWithError("did not find mvn command in the execute path")

        
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

def updateSWT(mvnArtifactId, downloadPageLabel, gitCloneRootDir, version, forceUpdate):
    URL = "https://download.eclipse.org/eclipse/downloads/"
    page = requests.get(URL)

    soup = BeautifulSoup(page.content, "html.parser")
    linkToVersionDownload = ""
    localSWTVersion = ""
    
    if os.path.isdir(LOCAL_REPO_SAVE_DIR + "/local/swt/" + mvnArtifactId):
        subdirs = os.listdir(LOCAL_REPO_SAVE_DIR + "/local/swt/" + mvnArtifactId + "/")
        for dir in subdirs :
            localSWTVersion = dir
    
    if version == "" :
        anchorElement = soup.find(id="Latest_Release").find_next("a")
        linkToVersionDownload = anchorElement['href']
        version = anchorElement.text
        
    else:
        for link in soup.findAll('a', href=True):
            if version in link.text :
                linkToVersionDownload = link['href']
                break

    if forceUpdate == False \
        and version.strip() == localSWTVersion.strip() \
        and os.path.isdir(LOCAL_REPO_SAVE_DIR + "/local/swt/" + mvnArtifactId) :
        shutil.move(LOCAL_REPO_SAVE_DIR + "/local/swt/" + mvnArtifactId + "/", LOCAL_REPO_DIR+ "/local/swt/" + mvnArtifactId)
        return

    if linkToVersionDownload == "" :
        exitWithError("version " + version + " not found for download")
        

    downloadsPage = URL + linkToVersionDownload
    page = requests.get(downloadsPage)
    soup = BeautifulSoup(page.content, "html.parser")
    
    results = getSWTDownloadLinkForPlatform(soup, downloadPageLabel)
    unzippedDir = downloadAndExtractZip(downloadsPage + results)
    installInLocalMavenRepo(unzippedDir, mvnArtifactId, gitCloneRootDir)

######## end of updateSWTAndPOM #########################

atexit.register(cleanupBeforeExit)

parser = argparse.ArgumentParser(description = "my parser")
parser.add_argument("-v", "--version", required = False, default = "")
parser.add_argument("-f", "--force", required = False, default = False, nargs='?', const=True)
parser.add_argument("-c", "--cloneroot", required = False, default = os.getcwd())

arguments = parser.parse_args()

# initialize variables from arguments
version = arguments.version
rootdir = arguments.cloneroot
forceUpdate = arguments.force
localSWTVersion = ""

# Save the local SWT repo before proceeding to update
if os.path.isdir(LOCAL_REPO_DIR):
    shutil.move(LOCAL_REPO_DIR + "/", LOCAL_REPO_SAVE_DIR)
    
# Windows
updateSWT("swtwin32_x86_64", "Windows (64 bit version)", rootdir, version, forceUpdate)

# Mac x86
updateSWT("swtmac_x86_64", "Mac OSX (64 bit version)", rootdir, version, forceUpdate)

# Mac ARM
updateSWT("swtmac_aarch64", "Mac OSX (64 bit version for Arm64/AArch64)", rootdir, version, forceUpdate)

# Linux x86
updateSWT("swtlinux_x86_64", "Linux (64 bit version)", rootdir, version, forceUpdate)

# Linux ARM
updateSWT("swtlinux_aarch64", "Linux (64 bit version for AArch64)", rootdir, version, forceUpdate)

if os.path.isdir(LOCAL_REPO_SAVE_DIR):
    shutil.rmtree(LOCAL_REPO_SAVE_DIR)

for subdir in os.listdir(LOCAL_REPO_DIR):
    if subdir != "local" :
        shutil.rmtree(LOCAL_REPO_DIR + "/" + subdir)
