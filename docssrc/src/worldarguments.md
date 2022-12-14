# World arguments

![A picture of world arguments in action](./images/arguments/worldargument.png)

The `WorldArgument` class allows a command sender to refer to a loaded Bukkit `World`.

<div class="example">

### Example - Unloading world

Say we want to unload a world on our Minecraft server. We want to create a command with the following syntax:

```mccmd
/unloadworld <world>
```

Using the world from the `WorldArgument`, we can then unload the world safely using `Bukkit.getServer().unloadWorld()` and passing `true` (to save chunks):

<div class="multi-pre">

```java,Java
{{#include ../../commandapi-documentation-code/src/main/java/dev/jorel/commandapi/examples/java/Examples.java:worldarguments}}
```

```kotlin,Kotlin
{{#include ../../commandapi-documentation-code/src/main/kotlin/dev/jorel/commandapi/examples/kotlin/Examples.kt:worldarguments}}
```

</div>

</div>
