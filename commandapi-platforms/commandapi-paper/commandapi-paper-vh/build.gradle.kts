plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support version handler"

dependencies {
	compileOnly(libs.paper.version.common)

	compileOnly(project(":commandapi-paper-nms-dependency"))
}