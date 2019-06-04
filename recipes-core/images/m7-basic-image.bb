# Copyright (C) 2019 Novasis Ingegneria Srl
# Author: Paolo Geninatti <tech@pagit.eu>
#
# Yocto basic image for Novasom M7 board

include recipes-core/images/core-image-minimal.bb

DESCRIPTION = "Console-only image with device-tree overlays for \
enable all interfaces on M7 pin-strip connector and \
configations/tools for common board usage." 

IMAGE_FEATURES_append = " package-management ssh-server-dropbear read-only-rootfs"

IMAGE_INSTALL_append = " \
	kernel-modules \
	tcf-agent \
	tzdata \
	connman-client \
	connman \
	i2c-tools \
	spitools \
	dt-overlay \
	usbutils \
	wireless-tools \
	iw \
	alsa-utils \
    rtl8723bs-bt \
    emmc-flash \
"

LICENSE = "MIT"

# R/W data partition size (in 512 bytes block units)
RWDATAFS_SIZE = "524288"

read_only_rootfs_hacks() {
    sed -i 's|/etc/resolv.conf|/var/run/resolv.conf|g' ${IMAGE_ROOTFS}/etc/tmpfiles.d/connman_resolvconf.conf
    sed -i 's|PrivateTmp=yes|PrivateTmp=no|g' ${IMAGE_ROOTFS}/lib/systemd/system/systemd-hostnamed.service
    sed -i 's|PrivateTmp=yes|PrivateTmp=no|g' ${IMAGE_ROOTFS}/lib/systemd/system/systemd-timesyncd.service
    sed -i 's|^After=.*|& systemd-tmpfiles-setup.service|' ${IMAGE_ROOTFS}/lib/systemd/system/systemd-rfkill.service
}

RWDATA_PART_novasom-m7-sd = "/dev/mmcblk0p6"
RWDATA_PART_novasom-m7-emmc = "/dev/mmcblk2p6"

mount_rwdata() {
    mkdir -p ${IMAGE_ROOTFS}/data
    cat >>${IMAGE_ROOTFS}/etc/fstab <<EOF

${RWDATA_PART}       /data          auto       defaults,sync,auto  0  0
EOF
}

ROOTFS_POSTPROCESS_COMMAND += "read_only_rootfs_hacks; mount_rwdata; "

create_rk_image () {
    bbnote "Novasom M7 customized create_rk_image(): add dt overlays to BOOT_IMG"
     
	# last dd rootfs will extend gpt image to fit the size,
	# but this will overrite the backup table of GPT
	# will cause corruption error for GPT
	IMG_ROOTFS_SIZE=$(stat -L --format="%s" ${IMG_ROOTFS})

	ROOTFS_BLKSIZE=$(expr $IMG_ROOTFS_SIZE \/ 512)

	GPTIMG_MIN_SIZE=$(expr $IMG_ROOTFS_SIZE + \( ${LOADER1_SIZE} + ${RESERVED1_SIZE} + ${RESERVED2_SIZE} + ${LOADER2_SIZE} + ${ATF_SIZE} + ${BOOT_SIZE} + ${RWDATAFS_SIZE} + 70 \) \* 512 )

	GPT_IMAGE_SIZE=$(expr $GPTIMG_MIN_SIZE \/ 1024 \/ 1024 + 2)

	RWDATA_IMG="rwdata.img"

	# Initialize sdcard image file
	dd if=/dev/zero of=${GPTIMG} bs=1M count=0 seek=$GPT_IMAGE_SIZE

	# Create partition table
	parted -s ${GPTIMG} mklabel gpt

	# Create vendor defined partitions
	LOADER1_START=64
	RESERVED1_START=$(expr ${LOADER1_START} + ${LOADER1_SIZE})
	RESERVED2_START=$(expr ${RESERVED1_START} + ${RESERVED1_SIZE})
	LOADER2_START=$(expr ${RESERVED2_START} + ${RESERVED2_SIZE})
	ATF_START=$(expr ${LOADER2_START} + ${LOADER2_SIZE})
	BOOT_START=$(expr ${ATF_START} + ${ATF_SIZE})
	ROOTFS_START=$(expr ${BOOT_START} + ${BOOT_SIZE})
	RWDATAFS_START=$(expr ${ROOTFS_START} + ${ROOTFS_BLKSIZE} + 35)

	parted -s ${GPTIMG} unit s mkpart loader1 ${LOADER1_START} $(expr ${RESERVED1_START} - 1)
	# parted -s ${GPTIMG} unit s mkpart reserved1 ${RESERVED1_START} $(expr ${RESERVED2_START} - 1)
	# parted -s ${GPTIMG} unit s mkpart reserved2 ${RESERVED2_START} $(expr ${LOADER2_START} - 1)
	parted -s ${GPTIMG} unit s mkpart loader2 ${LOADER2_START} $(expr ${ATF_START} - 1)
	parted -s ${GPTIMG} unit s mkpart trust ${ATF_START} $(expr ${BOOT_START} - 1)

	# Create boot partition and mark it as bootable
	parted -s ${GPTIMG} unit s mkpart boot ${BOOT_START} $(expr ${ROOTFS_START} - 1)
	parted -s ${GPTIMG} set 4 boot on

	# Create rootfs partition
	parted -s ${GPTIMG} unit s mkpart rootfs ${ROOTFS_START} $(expr ${ROOTFS_START} + ${ROOTFS_BLKSIZE} - 1)

	# Create data partition
	parted -s ${GPTIMG} unit s mkpart data ${RWDATAFS_START} $(expr ${RWDATAFS_START} + ${RWDATAFS_SIZE} - 1)

	parted ${GPTIMG} print

	if [ "${DEFAULTTUNE}" = "aarch64" ];then
		ROOT_UUID="B921B045-1DF0-41C3-AF44-4C6F280D3FAE"
	else
		ROOT_UUID="69DAD710-2CE4-4E3C-B16C-21A1D49ABED3"
	fi

	# Change rootfs partuuid
	gdisk ${GPTIMG} <<EOF
x
c
5
${ROOT_UUID}
w
y
EOF

	# Delete the boot image to avoid trouble with the build cache
	rm -f ${WORKDIR}/${BOOT_IMG}

	# Create boot partition image
	BOOT_BLOCKS=$(LC_ALL=C parted -s ${GPTIMG} unit b print | awk '/ 4 / { print substr($4, 1, length($4 -1)) / 512 /2 }')
	BOOT_BLOCKS=$(expr $BOOT_BLOCKS / 63 \* 63)

	mkfs.vfat -n "boot" -S 512 -C ${WORKDIR}/${BOOT_IMG} $BOOT_BLOCKS
	mcopy -i ${WORKDIR}/${BOOT_IMG} -s ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}-${MACHINE}.bin ::${KERNEL_IMAGETYPE}

	DTS_FILE=""
	DTBPATTERN="${KERNEL_IMAGETYPE}((-\w+)+\.dtb)"
	for DFILES in ${DEPLOY_DIR_IMAGE}/*; do
		DFILES=${DFILES##*/}
		if echo "${DFILES}" | grep -P $DTBPATTERN ; then
			[ -n "${DTS_FILE}" ] && bberror "Found multiple DTB under deploy dir, Please delete the unnecessary one."
			DTS_FILE=${DFILES#*${KERNEL_IMAGETYPE}-}
		fi
	done

	mcopy -i ${WORKDIR}/${BOOT_IMG} -s ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}-${DTS_FILE} ::${DTS_FILE}

    bbnote "Searching for .dtbo files in ${DEPLOY_DIR_IMAGE}"
	for DFILES in ${DEPLOY_DIR_IMAGE}/*.dtbo; do
	    bbnote "Found ${DFILES}"
 	    mcopy -i ${WORKDIR}/${BOOT_IMG} -s ${DFILES} ::
    done
    
 	mcopy -i ${WORKDIR}/${BOOT_IMG} -s ${DEPLOY_DIR_IMAGE}/boot.scr ::
 	mcopy -i ${WORKDIR}/${BOOT_IMG} -s ${DEPLOY_DIR_IMAGE}/overlay.txt ::
    
	# Create extlinux config file
	cat >${WORKDIR}/extlinux.conf <<EOF
default yocto

label yocto
	kernel /${KERNEL_IMAGETYPE}
	devicetree /${DTS_FILE}
	append ${GPTIMG_APPEND}
EOF

#	mmd -i ${WORKDIR}/${BOOT_IMG} ::/extlinux
#	mcopy -i ${WORKDIR}/${BOOT_IMG} -s ${WORKDIR}/extlinux.conf ::/extlinux/
	
	mdir -i ${WORKDIR}/${BOOT_IMG} -/ ::

	# Burn Boot Partition
	dd if=${WORKDIR}/${BOOT_IMG} of=${GPTIMG} conv=notrunc,fsync seek=${BOOT_START}

	# Burn Rootfs Partition
	dd if=${IMG_ROOTFS} of=${GPTIMG} conv=notrunc,fsync seek=${ROOTFS_START}

	# Delete the rwdata image to avoid trouble with the build cache
	rm -f ${WORKDIR}/${RWDATA_IMG}

	# Create rwdata partition image
	RWDATA_BLOCKS=$(LC_ALL=C parted -s ${GPTIMG} unit b print | awk '/ 6 / { print substr($4, 1, length($4 -1)) / 512 /2 }')
	RWDATA_BLOCKS=$(expr $RWDATA_BLOCKS / 63 \* 63)

	mkfs.ext4 -L "data" -F ${WORKDIR}/${RWDATA_IMG} $RWDATA_BLOCKS
	dd if=${WORKDIR}/${RWDATA_IMG} of=${GPTIMG} conv=notrunc,fsync seek=${RWDATAFS_START}
}
