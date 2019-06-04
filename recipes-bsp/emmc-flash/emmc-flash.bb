SUMMARY = "Install eMMC flash tool"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://flash_tool.sh \
    file://make_emmc.sh \
"

do_install() {
    install -d ${D}/opt/emmc
    install -m 0755 ${WORKDIR}/flash_tool.sh ${D}/opt/emmc  
    install -m 0755 ${WORKDIR}/make_emmc.sh ${D}/opt/emmc  
}

FILES_${PN} += "/opt/emmc"
