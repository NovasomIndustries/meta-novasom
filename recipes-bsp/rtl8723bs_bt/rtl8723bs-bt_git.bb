SUMMARY = "Bluetooth Code for RTL8723bs"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    git://github.com/lwfinger/rtl8723bs_bt.git;branch=master \
    file://makefile.patch \
    file://start-bt.patch \
"

SRCREV = "09eb91f52a639ec5e4c5c4c98dc2afede046cf20"

INSANE_SKIP_${PN} = "ldflags"
INSANE_SKIP_${PN}-dev = "ldflags"

S = "${WORKDIR}/git/"

do_install() {
    install -d ${D}/lib/firmware/rtl_bt
    install -m 0755 ${S}/rtlbt_config ${D}/lib/firmware/rtl_bt
    install -m 0755 ${S}/rtlbt_fw ${D}/lib/firmware/rtl_bt
    install -m 0755 ${S}/rtlbt_fw_new ${D}/lib/firmware/rtl_bt
    
    install -d ${D}/opt/rtl_bt
    install -m 0755 ${S}/rtk_hciattach ${D}/opt/rtl_bt  
    install -m 0755 ${S}/start_bt.sh ${D}/opt/rtl_bt  
}

FILES_${PN} += "/lib/firmware/rtl_bt /opt/rtl_bt"
