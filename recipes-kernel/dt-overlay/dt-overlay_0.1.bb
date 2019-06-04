# This could be an interim module as current 4.4 kernel has not
#  device-tree overlay support.
# This recipe build DTBO files for DTS sources and copy them to
#  the u-boot image area. 

SUMMARY = "Build device-tree overlays for Novasom M7 board"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=12f884d2ae1ff87c09e5b7ccc2c4ca7e"

DEPENDS = " dtc-native"

SRC_URI = " \
    file://novasom-m7-i2c0-overlay.dts \
    file://novasom-m7-spi0-overlay.dts \
    file://novasom-m7-uart1-2w-overlay.dts \
    file://novasom-m7-uart1-4w-overlay.dts \
    file://COPYING \
"

S = "${WORKDIR}"

do_compile() {
    dtc -@ -I dts -O dtb -o novasom-m7-i2c0.dtbo novasom-m7-i2c0-overlay.dts
    dtc -@ -I dts -O dtb -o novasom-m7-spi0.dtbo novasom-m7-spi0-overlay.dts
    dtc -@ -I dts -O dtb -o novasom-m7-uart1-2w.dtbo novasom-m7-uart1-2w-overlay.dts
    dtc -@ -I dts -O dtb -o novasom-m7-uart1-4w.dtbo novasom-m7-uart1-4w-overlay.dts
}

do_install() {
    install -d ${DEPLOY_DIR_IMAGE}
    install -m 0755 ${S}/*.dtbo ${DEPLOY_DIR_IMAGE}
    
    install -d  ${D}/lib/firmware
    install -m 0755 ${S}/*.dtbo ${D}/lib/firmware
}

FILES_${PN} = "/lib/firmware/*.dtbo"
