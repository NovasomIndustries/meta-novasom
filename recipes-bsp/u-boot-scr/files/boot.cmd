echo running boot.cmd with ${devtype} ${devnum}:${distro_bootpart}
setenv bootargs %%BOOTARGS%%
setenv fdtfile %%FDTFILE%%

if load ${devtype} ${devnum}:${distro_bootpart} ${kernel_addr_r} /image; then
  if load ${devtype} ${devnum}:${distro_bootpart} ${fdt_addr_r} /${fdtfile}; then
	fdt addr ${fdt_addr_r}
	fdt resize

	setexpr fdtovaddr ${fdt_addr_r} + F000
	
	test -n ${overlay} || load ${devtype} ${devnum}:${distro_bootpart} ${fdtovaddr} /overlay.txt && env import -t ${fdtovaddr} ${filesize} 
	if test -n ${overlay}; then
		echo configured overlay: ${overlay}
		for ov in ${overlay}; do
			echo overlaying ${ov}...
			load ${devtype} ${devnum}:${distro_bootpart} ${fdtovaddr} /${ov}.dtbo && fdt apply ${fdtovaddr}
		done
	fi

    booti ${kernel_addr_r} - ${fdt_addr_r};
  fi;
fi

