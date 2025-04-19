compileVersion() {
	echo "Compiling NMS_Common for Spigot $1..."
	mvn clean package -Dmaven.source.skip=true -Dmaven.javadoc.skip=true -pl :commandapi-bukkit-nms-common -am -P Platform.Bukkit,"$2" --quiet
}

compileVersion "1.20 and 1.20.1" "Spigot_1_20_R1"
compileVersion "1.20.2" "Spigot_1_20_R2"
compileVersion "1.20.3 and 1.20.4" "Spigot_1_20_R3"
compileVersion "1.20.5 and 1.20.6" "Spigot_1_20_R4"
compileVersion "1.21 and 1.21.1" "Spigot_1_21_R1"
compileVersion "1.21.2 and 1.21.3" "Spigot_1_21_R2"
compileVersion "1.21.4" "Spigot_1_21_R3"
compileVersion "1.21.5" "Spigot_1_21_R4"