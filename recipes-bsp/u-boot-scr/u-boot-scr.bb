SUMMARY = "U-boot boot scripts"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "u-boot-mkimage-native"

INHIBIT_DEFAULT_DEPS = "1"

SRC_URI = "file://boot.cmd file://overlay.txt"

BOOT_PART_novasom-m7-sd  = "/dev/mmcblk0p5"
BOOT_PART_novasom-m7-emmc  = "/dev/mmcblk2p5"

do_compile() {
    DTS_FILE="$(basename "${KERNEL_DEVICETREE}")"
    BOOTARGS="earlycon=uart8250,mmio32,0xff130000 rw root=${BOOT_PART} rootwait rootfstype=ext4 init=/sbin/init rootwait"

    sed -i s#%%FDTFILE%%#"${DTS_FILE}"#g ${WORKDIR}/boot.cmd
    sed -i s#%%BOOTARGS%%#"${BOOTARGS}"#g ${WORKDIR}/boot.cmd
    mkimage -A arm -T script -C none -n "Boot script" -d "${WORKDIR}/boot.cmd" boot.scr
}

inherit deploy nopackages

do_deploy() {
    install -d ${DEPLOY_DIR_IMAGE}
    install -m 0644 boot.scr ${DEPLOY_DIR_IMAGE}
    install -m 0644 ${WORKDIR}/overlay.txt ${DEPLOY_DIR_IMAGE}
}

addtask do_deploy after do_compile before do_build
