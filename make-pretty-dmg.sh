#!/bin/bash

BASEDIR="$1"
DMG_SRC_DIR="$2"
DMG_OUT="$3"

chflags nouchg $DMG_SRC_DIR/*
chmod -R +w $DMG_SRC_DIR

bash $BASEDIR/yoursway-create-dmg/create-dmg --icon-size 32 --window-size 400 300 --app-drop-link 300 150 --icon "Data Loader" 100 150 $DMG_OUT $DMG_SRC_DIR