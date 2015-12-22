#!/bin/bash

BASEDIR="$1"
DMG_SRC_DIR="$2"
TIME=$(date +%s)
DMG_OUT="${BASEDIR}/target/ApexDataLoader.${TIME}.dmg"
CODE_SIGN="$3"

chflags nouchg $DMG_SRC_DIR/*
chmod -R +w $DMG_SRC_DIR

if [ "$CODE_SIGN" != "DONTSIGN" ]
then
    codesign --force --verify --sign "$CODE_SIGN" $DMG_SRC_DIR/Data\ Loader.app
fi

bash $BASEDIR/yoursway-create-dmg/create-dmg --volname "DataLoader" --volicon "$BASEDIR/src/main/resources/img/icons/icon_loader.icns" --background "$BASEDIR/src/main/nsis/installer-salesforce-dataLoader.png" --hide-extension Data\ Loader.app --icon-size 60 --window-size 400 300 --app-drop-link 300 210 --icon "Data Loader" 100 210 "$DMG_OUT" $DMG_SRC_DIR
