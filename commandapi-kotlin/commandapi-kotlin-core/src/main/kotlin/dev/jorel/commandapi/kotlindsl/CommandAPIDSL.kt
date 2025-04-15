package dev.jorel.commandapi.kotlindsl

import dev.jorel.commandapi.executors.CommandArguments
import kotlin.reflect.KProperty

// CommandArguments DSL
inline operator fun <reified T> CommandArguments.getValue(nothing: Nothing?, property: KProperty<*>) = this[property.name] as T