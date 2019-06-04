FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
    file://0001-Added-Novasom-M7-custom-configuration.patch \
"

RDEPENDS_${PN} += " u-boot-scr"
