# Copyright (C) 2019 Novasis Ingegneria Srl
# Author: Paolo Geninatti <tech@pagit.eu>
#
# Yocto Wayland image for Novasom M7 board

include m7-basic-image.bb

DESCRIPTION = "GUI enabled  image that include a \
Wayland server." 

IMAGE_FEATURES += "\
	splash	\
"

CORE_IMAGE_EXTRA_INSTALL += " \
	libdrm-rockchip \
	packagegroup-rk-gstreamer-full \
	gstreamer1.0-libav \
"

COMMON_INSTALL = " \
	qtbase	\
	qtdeclarative \
	qtmultimedia \
	qtsvg \
	qtsensors \
	qtimageformats \
	qtsystems \
	qtscript \
	qt3d \
	qtgraphicaleffects \
	qtconnectivity \
	qtlocation \
	qtquickcontrols \
	qtquickcontrols2 \
"

QT_DEMOS = " \
	carmachine-examples \
"

IMAGE_INSTALL += " \
	${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'qtwayland', '', d)} \
	autostart \
	packagegroup-fonts-truetype \
	${COMMON_INSTALL} \
	${QT_DEMOS} \
	weston \
"

wayland_setup() {
    cat >${IMAGE_ROOTFS}/etc/tmpfiles.d/wayland.conf <<EOF
d	/tmp/wayland	0700 - - -
EOF

    cat >>${IMAGE_ROOTFS}/etc/profile <<EOF
export XDG_RUNTIME_DIR=/tmp/wayland
EOF
}

ROOTFS_POSTPROCESS_COMMAND += "wayland_setup; "
