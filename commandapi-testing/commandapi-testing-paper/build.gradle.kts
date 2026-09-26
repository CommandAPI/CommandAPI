plugins {
	id("buildlogic.java-conventions")
}

description = "Testing plugin for Paper"

val commandApiShade = project(":commandapi-paper-shade")

evaluationDependsOn(":commandapi-paper-shade")

dependencies {
	compileOnly(paper.version.v1206)

	implementation(files(commandApiShade.tasks.named("shadowJar")))
}