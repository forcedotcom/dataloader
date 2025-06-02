#!/usr/bin/env python3

import requests
import zipfile
import io
import shutil
import os
import sys
import subprocess
from bs4 import BeautifulSoup
from os.path import expanduser
import urllib.request
import re
import argparse
import atexit
import tempfile
import logging
from typing import Optional, Dict, List
from dataclasses import dataclass

####################################################################
# Prerequisites:
# - Python 3.9 or higher
# - Directory containing mvn command in PATH environment variable.
# - Python BeautifulSoup installed locally. Run 'pip3 install beautifulsoup4'
# - Python requests installed locally. Run 'pip3 install requests'
#
# Side-effects:
# - Zip content extracted in temporary directory
# - SWT jar files installed in <git clone root>/local-proj-repo subdirectories
#
# Follow-on manual steps:
# - Update version value for SWT dependencies in pom.xml with the downloaded SWT version.
####################################################################

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger(__name__)

# Constants
LOCAL_REPO_DIR = "./local-proj-repo"
ECLIPSE_DOWNLOAD_URL = "https://download.eclipse.org/eclipse/downloads/"
SWT_SIGNATURE_FILES = [
    "META-INF/ECLIPSE_.SF",
    "META-INF/ECLIPSE_.DSA",
    "META-INF/ECLIPSE_.RSA"
]

# Platform configurations
PLATFORM_CONFIGS = {
    "swtwin32_x86_64": "Windows (x86 64-bit)",
    "swtwin32_aarch64": "Windows (ARM 64-bit)",
    "swtmac_x86_64": "Mac OSX (x86 64-bit)",
    "swtmac_aarch64": "Mac OSX (ARM 64-bit)",
    "swtlinux_x86_64": "Linux (x86 64-bit)",
    "swtlinux_aarch64": "Linux (ARM 64-bit)"
}

@dataclass
class SWTConfig:
    """Configuration for SWT update process."""
    version: str
    force_update: bool
    git_clone_root: str
    temp_dir: Optional[str] = None


class SWTUpdateError(Exception):
    """Base exception for SWT update errors."""
    pass

class MavenNotFoundError(SWTUpdateError):
    """Raised when Maven is not found in PATH."""
    pass

class VersionNotFoundError(SWTUpdateError):
    """Raised when specified version is not found for download."""
    pass

def is_exe(fpath: str) -> bool:
    """Check if a file is executable.
    
    Args:
        fpath: Path to the file to check
        
    Returns:
        bool: True if file exists and is executable
    """
    return os.path.isfile(fpath) and os.access(fpath, os.X_OK)

def cleanupBeforeExit(config: Optional[SWTConfig] = None) -> None:
    """Clean up temporary directory before script exit.
    
    Args:
        config: SWT configuration containing temp directory path
    """
    if config and config.temp_dir and os.path.exists(config.temp_dir):
        logger.info("Cleaning up temporary directory: %s", config.temp_dir)
        shutil.rmtree(config.temp_dir)

def getSWTDownloadLinkForPlatform(soup: BeautifulSoup, platformString: str) -> str:
    """Get the download link for a specific platform from the Eclipse download page.
    
    Args:
        soup: BeautifulSoup object of the download page
        platformString: Platform identifier string to search for
        
    Returns:
        str: Download link for the platform
        
    Raises:
        VersionNotFoundError: If platform link is not found
    """
    results = soup.find(id="SWT").find_next("td").string
    while results is not None and results != platformString:
        results = results.find_next("td").string

    if results == platformString:
        return results.find_next("a")['href']
    
    raise VersionNotFoundError(f"Download link not found for platform: {platformString}")

def downloadAndExtractZip(url: str, temp_dir: str) -> str:
    """Download and extract SWT zip file to temporary directory.
    
    Args:
        url: URL of the download page
        temp_dir: Temporary directory to extract to
        
    Returns:
        str: Path to the extracted directory
        
    Raises:
        SWTUpdateError: If download or extraction fails
    """
    try:
        zipfileName = url.split('=', 1)[1]
        unzippedDirName = os.path.join(temp_dir, zipfileName.removesuffix('.zip'))

        # Get the actual zip file URL
        page = requests.get(url)
        soup = BeautifulSoup(page.content, "html.parser")
        zipURL = soup.find("meta").find_next("a")['href']

        page = requests.get(zipURL)
        soup = BeautifulSoup(page.content, "html.parser")
        divWithZip = soup.find("div", {"class": "mirror-well"})
        zipURL = divWithZip.find_next("a")['href']
        zipURL = "https://www.eclipse.org/downloads/" + zipURL

        # Navigate the redirect to the actual mirror
        page = requests.get(zipURL)
        soup = BeautifulSoup(page.content, "html.parser")
        zipURL = soup.find('meta', attrs={'http-equiv': 'Refresh'})['content'].split(';')[1].split('=')[1]

        # Clean up existing content
        if os.path.exists(unzippedDirName):
            shutil.rmtree(unzippedDirName)

        # Download and extract
        response = requests.get(zipURL, stream=True)
        z = zipfile.ZipFile(io.BytesIO(response.content))
        z.extractall(unzippedDirName)

        # Remove signature files
        for sig_file in SWT_SIGNATURE_FILES:
            subprocess.run(["zip", "-d", os.path.join(unzippedDirName, "swt.jar"), sig_file],
                         capture_output=True)

        return unzippedDirName
    except Exception as e:
        raise SWTUpdateError("Failed to download and extract SWT: %s" % str(e))

def installInLocalMavenRepo(unzippedSWTDir: str, mvnArtifactId: str, gitCloneRootDir: str) -> None:
    """Install SWT jar into local Maven repository.
    
    Args:
        unzippedSWTDir: Directory containing extracted SWT jar
        mvnArtifactId: Maven artifact ID
        gitCloneRootDir: Root directory of git clone
        
    Raises:
        MavenNotFoundError: If Maven is not found in PATH
        SWTUpdateError: If installation fails
    """
    if shutil.which("mvn") is None:
        raise MavenNotFoundError("Maven command not found in PATH")

    swtVersion = unzippedSWTDir.split('-')[1]
    
    mavenCommand = [
        "mvn", "install:install-file",
        f"-Dfile={os.path.join(unzippedSWTDir, 'swt.jar')}",
        "-DgroupId=local.swt",
        f"-DartifactId={mvnArtifactId}",
        f"-Dversion={swtVersion}",
        "-Dpackaging=jar",
        f"-Dmaven.repo.local={os.path.join(gitCloneRootDir, 'local-proj-repo')}"
    ]
    
    try:
        subprocess.run(mavenCommand, check=True, capture_output=True, text=True)
        logger.info("Successfully installed %s version %s", mvnArtifactId, swtVersion)
    except subprocess.CalledProcessError as e:
        raise SWTUpdateError("Failed to install SWT in Maven repo: %s" % e.stderr)

def getLocalSWTVersion(mvnArtifactId: str) -> str:
    """Get the version of SWT installed in local Maven repository.
    
    Args:
        mvnArtifactId: Maven artifact ID to check
        
    Returns:
        str: Version string if found, empty string otherwise
    """
    artifactPath = os.path.join(LOCAL_REPO_DIR, "local/swt", mvnArtifactId)
    if os.path.isdir(artifactPath):
        subdirs = [d for d in os.listdir(artifactPath) 
                  if os.path.isdir(os.path.join(artifactPath, d))]
        if subdirs:
            version = subdirs[0]
            logger.info("Found local version for %s: %s", mvnArtifactId, version)
            return version
    return ""

def updateSWT(config: SWTConfig, mvnArtifactId: str, downloadPageLabel: str) -> None:
    """Update SWT for a specific platform.
    
    Args:
        config: SWT update configuration
        mvnArtifactId: Maven artifact ID
        downloadPageLabel: Platform label on download page
        
    Raises:
        VersionNotFoundError: If version not found for download
        SWTUpdateError: If update process fails
    """
    try:
        page = requests.get(ECLIPSE_DOWNLOAD_URL)
        soup = BeautifulSoup(page.content, "html.parser")
        linkToVersionDownload = ""
        
        localSWTVersion = getLocalSWTVersion(mvnArtifactId)
        
        if config.version == "":
            anchorElement = soup.find(id="Latest_Release").find_next("a")
            linkToVersionDownload = anchorElement['href']
            config.version = anchorElement.text.strip()
            logger.info("Found download version: %s", config.version)
        else:
            for link in soup.find_all('a', href=True):
                if config.version in link.text:
                    linkToVersionDownload = link['href']
                    break

        logger.info("Comparing versions - Local: '%s', Download: '%s'", localSWTVersion, config.version)
        if not config.force_update and config.version.strip() == localSWTVersion.strip():
            logger.info("Skipping download for %s - version %s already installed", mvnArtifactId, config.version)
            return

        if not linkToVersionDownload:
            raise VersionNotFoundError("Version %s not found for download" % config.version)

        downloadsPage = ECLIPSE_DOWNLOAD_URL + linkToVersionDownload
        page = requests.get(downloadsPage)
        soup = BeautifulSoup(page.content, "html.parser")
        
        results = getSWTDownloadLinkForPlatform(soup, downloadPageLabel)
        unzippedDir = downloadAndExtractZip(downloadsPage + results, config.temp_dir)
        installInLocalMavenRepo(unzippedDir, mvnArtifactId, config.git_clone_root)
        
    except Exception as e:
        raise SWTUpdateError("Failed to update SWT for %s: %s" % (mvnArtifactId, str(e)))

def main() -> None:
    """Main entry point for the script."""
    parser = argparse.ArgumentParser(description="Update SWT dependencies in local Maven repository")
    parser.add_argument("-v", "--version", required=False, default="",
                       help="Specific version to download (default: latest)")
    parser.add_argument("-f", "--force", required=False, default=False, nargs='?', const=True,
                       help="Force update even if versions match")
    parser.add_argument("-c", "--cloneroot", required=False, default=os.getcwd(),
                       help="Root directory of git clone")

    arguments = parser.parse_args()
    
    config = SWTConfig(
        version=arguments.version,
        force_update=arguments.force,
        git_clone_root=arguments.cloneroot,
        temp_dir=tempfile.mkdtemp()
    )
    
    logger.info("Created temporary directory: %s", config.temp_dir)
    
    try:
        for artifact_id, platform_label in PLATFORM_CONFIGS.items():
            updateSWT(config, artifact_id, platform_label)
            
        # Clean up any non-local directories in the local repo
        for subdir in os.listdir(LOCAL_REPO_DIR):
            if subdir != "local":
                shutil.rmtree(os.path.join(LOCAL_REPO_DIR, subdir))
                
    except Exception as e:
        logger.error("Error during SWT update: %s", str(e))
        sys.exit(1)

    finally:
        if config.temp_dir and os.path.exists(config.temp_dir):
            shutil.rmtree(config.temp_dir)


if __name__ == "__main__":
    config = None
    try:
        main()
    finally:
        cleanupBeforeExit(config)
