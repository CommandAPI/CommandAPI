# Simple literal arguments

So far, we've described normal arguments and literal arguments. We've described the nuances with literal arguments and how they're not really "arguments", so they don't appear in the `args[]` for commands.

Now forget all of that. Simple literal arguments are the same as literal arguments but they _do_ appear in the `args[]` for commands. Simple literal arguments are just a way better alternative to literal arguments. The simple literal argument constructor allows you to provide a `String[]` of possible values which you can use for your command declaration.

The simple literal argument has all of the same benefits of a regular literal argument - they are hardcoded options that the user must enter - they don't allow other values.

> **Developer's Note:**
>
> The only reason that `LiteralArgument` still exists is for legacy purposes. `SimpleLiteralArgument` is much more recommended because it's easier to understand and implement. The `LiteralArgument` has a very slight performance improvement over the `SimpleLiteralArgument`, but it's basically unnoticeable.

<div class="example">

### Example - Using simple literals to make the gamemode command

In this example, we'll show how to use simple literals to declare Minecraft's `/gamemode` command. As you can see from the example code below, the argument declaration and command declaration is the same as if you were declaring any normal argument or command.

```java
LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
arguments.put("gamemode", new SimpleLiteralArgument("adventure", "creative", "spectator", "survival"));

new CommandAPICommand("gamemode")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        // The literal string that the player enters IS available in the args[]
        switch((String) args[0]) {
            case "adventure":
                player.setGameMode(GameMode.ADVENTURE);
                break;
            case "creative":
                player.setGameMode(GameMode.CREATIVE);
                break;
            case "spectator":
                player.setGameMode(GameMode.SPECTATOR);
                break;
            case "survival":
                player.setGameMode(GameMode.SURVIVAL);
                break;
        }
    }) 
    .register();
```

</div>

