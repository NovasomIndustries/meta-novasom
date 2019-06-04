#!/bin/sh

MACHINE=novasom-m7-sd
DEVICE=

if [ -z "${DEVICE}" ]; then
    echo "Please edit make_sdcard.sh and insert DEVICE=/dev/<your_sdcard_device> (i.e. DEVICE=/dev/sdd)" 
    exit 1
fi

if [ -z "$1" ]; then
    IMAGE=m7-basic-image
else
    IMAGE="$1"
fi

./flash_tool.sh -c rk3328 -d /dev/sdd -p system -i build/tmp/deploy/images/${MACHINE}/${IMAGE}-${MACHINE}-gpt.img
