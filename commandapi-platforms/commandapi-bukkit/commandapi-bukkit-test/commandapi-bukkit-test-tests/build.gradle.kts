plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Bukkit tests shared between the platforms"

/*
   The tests in this module are shared between the Bukkit platforms. They are compiled and run by
   commandapi-paper-test-tests and commandapi-spigot-test-tests, which provide the platform's
   CommandAPIVersionHandler, Main and test resources.

   This module has no platform of its own, so the tests aren't compiled here.
*/
tasks.named("compileTestJava") {
	enabled = false
}

tasks.named("test") {
	enabled = false
}
