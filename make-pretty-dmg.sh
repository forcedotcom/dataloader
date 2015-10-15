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

bash $BASEDIR/yoursway-create-dmg/create-dmg --volname "DataLoader" --background "$BASEDIR/src/main/resources/img/installscreens.gif" --hide-extension "Data Loader.app" --icon-size 60 --window-size 400 300 --app-drop-link 300 230 --icon "Data Loader" 100 230 "$DMG_OUT" $DMG_SRC_DIR
