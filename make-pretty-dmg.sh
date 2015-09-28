#!/bin/bash

BASEDIR="$1"
DMG_SRC_DIR="$2"
DMG_OUT="$3"
CODE_SIGN="$4"

chflags nouchg $DMG_SRC_DIR/*
chmod -R +w $DMG_SRC_DIR

if [ -n "$CODE_SIGN" ]
then
    codesign --force --verify --sign "$4" $DMG_SRC_DIR/Data\ Loader.app
fi

bash $BASEDIR/yoursway-create-dmg/create-dmg --volname "Data Loader" --background "$BASEDIR/src/main/resources/img/installscreens.gif" --hide-extension "Data Loader.app" --icon-size 60 --window-size 400 300 --app-drop-link 300 230 --icon "Data Loader" 100 230 "$DMG_OUT" $DMG_SRC_DIR
