#!/bin/sh -e

LOADER1_SIZE=8000
RESERVED1_SIZE=128
RESERVED2_SIZE=8192
LOADER2_SIZE=8192
ATF_SIZE=8192
BOOT_SIZE=229376

SYSTEM_START=0
LOADER1_START=64
RESERVED1_START=$(expr ${LOADER1_START} + ${LOADER1_SIZE})
RESERVED2_START=$(expr ${RESERVED1_START} + ${RESERVED1_SIZE})
LOADER2_START=$(expr ${RESERVED2_START} + ${RESERVED2_SIZE})
ATF_START=$(expr ${LOADER2_START} + ${LOADER2_SIZE})
BOOT_START=$(expr ${ATF_START} + ${ATF_SIZE})
ROOTFS_START=$(expr ${BOOT_START} + ${BOOT_SIZE})

LOCALPATH=$(pwd)
TOOLPATH=${LOCALPATH}/rkbin/tools
CHIP=""
DEVICE=""
IMAGE=""
DEVICE=""
SEEK="0"

usage() {
	echo -e "\nUsage: emmc: flash_tool.sh -c rk3328  -p system -i out/system.img  \n"
	echo -e "       sdcard: flash_tool.sh -c rk3328  -d /dev/sdb -p system  -i out/system.img \n"
}

finish() {
	echo -e "\e[31m FLASH IMAGE FAILED.\e[0m"
	exit -1
}
trap finish ERR

while getopts "c:t:s:d:p:r:d:i:h" flag; do
	case $flag in
		c)
			CHIP="$OPTARG"
			;;
		d)
			DEVICE="$OPTARG"
			;;
		i)
			IMAGE="$OPTARG"
			if [ ! -f "${IMAGE}" ]; then
				echo -e "\e[31m CAN'T FIND IMAGE \e[0m"
				usage
				exit
			fi
			;;
		p)
			PARTITIONS="$OPTARG"
			BPARTITIONS=$(echo $PARTITIONS | tr 'a-z' 'A-Z')
			SEEK=${BPARTITIONS}_START
			eval SEEK=\$$SEEK

			if [ -n "$(echo $SEEK | sed -n "/^[0-9]\+$/p")" ]; then
				echo "PARTITIONS OFFSET: $SEEK sectors."
			else
				echo -e "\e[31m INVAILD PARTITION.\e[0m"
				exit
			fi
			;;
	esac
done

if [ ! $IMAGE ]; then
	usage
	exit
fi

flash_sdcard() {
	dd if=${IMAGE} of=${DEVICE} seek=${SEEK}
	sync
}

if [ ! $DEVICE ]; then
	usage
else
	flash_sdcard
fi
