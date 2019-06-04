#!/bin/sh

MACHINE=novasom-m7-sd
DEVICE=

if [ -z "$1" ]; then
    echo "Usage: ./make_emmc.sh <filename.img>" 
    exit 1
fi

./flash_tool.sh -c rk3328 -d /dev/mmcblk2 -p system -i $1
