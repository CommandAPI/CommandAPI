# Upgrading guide

## From 8.0.0 to 8.1.0

Arguments are now parameterized over a generic type. This does very little in terms of the running of the CommandAPI, but does ensure type safety with its internals. Instances of the `Argument` type now have to be parameterized. In general, this basically means:

```java
Argument myArgument = new GreedyStringArgument("arg");
```

\\[\downarrow\\]

```java
Argument<?> myArgument = new GreedyStringArgument("arg");
```

Arguments that have multiple return types also need to be parameterized over their return type. This includes:

- `CustomArgument`
- `EntitySelectorArgument`
- `ScoreholderArgument`

For example:

```java
new EntitySelectorArgument("target", EntitySelector.ONE_PLAYER);
```

\\[\downarrow\\]

```java
new EntitySelectorArgument<Player>("target", EntitySelector.ONE_PLAYER);
```

-----

## From version 7.0.0 to 8.0.0

### Particle arguments

Particle arguments no longer return Bukkit's `org.bukkit.Particle` enum, but now return a wrapper `dev.jorel.commandapi.wrappers.ParticleData` instead. More information about this wrapper class and how to use it can be found on the [particle arguments page](./particlearguments.md). To update, change any `Particle` casts into a `ParticleData` cast instead:

```java
new CommandAPICommand("mycommand")
    .withArgument(new ParticleArgument("particle"))
    .executes((sender, args) -> {
        Particle particle = (Particle) args[0];
        // Do stuff with particle
    })
    .register();
```

\\[\downarrow\\]

```java
new CommandAPICommand("mycommand")
    .withArgument(new ParticleArgument("particle"))
    .executes((sender, args) -> {
        ParticleData particleData = (ParticleData) args[0];

        Particle particle = particleData.particle();
        Object data = particleData.data();

        // Do stuff with particle and data
    })
    .register();
```

-----

## From version 6.5.2 to 7.0.0

### Maven repository

The Maven repository used to serve the CommandAPI has changed from JitPack.io to Maven Central. For Maven projects, you no longer require wan explicit `<repository>` entry for the CommandAPI. for Gradle projects, you need to ensure `mavenCentral()` in present in your `repositories` section.

**The group ID has changed from `dev.jorel.CommandAPI` to `dev.jorel`**

More information about setting up your development environment can be found in [Setting up your development environment](./quickstart.md).

### CommandAPI command failures

The `CommandAPI.fail()` no longer automatically throws the exception that it creates, and instead now requires you to manually throw the exception yourself. This improves upon invalid states in command executors and allows invalid states to be identified more easily at compile time. To update, simply add the `throw` keyword before you call `CommandAPI.fail()`:

```java
new CommandAPICommand("mycommand")
    .executes((sender, args) -> {
        if(!sender.hasPermission("some.permission")) {
            CommandAPI.fail("You don't have permission to run /mycommand!");
            return;
        }
        sender.sendMessage("Hello!");
    })
```

\\[\downarrow\\]

```java
new CommandAPICommand("mycommand")
    .executes((sender, args) -> {
        if(!sender.hasPermission("some.permission")) {
            throw CommandAPI.fail("You don't have permission to run /mycommand!");
        }
        sender.sendMessage("Hello!");
    })
```

### Suggestions

Suggestions have been overhauled and no longer take in a `Function<SuggestionsInfo, String[]>` anymore. Instead, they now take in a `ArgumentSuggestions` object which represents argument suggestions (and whether they are executed asynchronously or have tooltips).

#### Normal (string) suggestions

These normal suggestions methods have been replaced with an `ArgumentSuggestions` parameter instead of a function:

```java
Argument replaceSuggestions(Function<SuggestionInfo, String[]> suggestions);
Argument includeSuggestions(Function<SuggestionInfo, String[]> suggestions);
```

\\[\downarrow\\]

```java
Argument replaceSuggestions(ArgumentSuggestions suggestions);
Argument includeSuggestions(ArgumentSuggestions suggestions);
```

The same functionality can be reproduced by wrapping your existing functions in `ArgumentSuggestions.strings`:

```java
List<Argument> arguments = new ArrayList<>();
arguments.add(new StringArgument("world").replaceSuggestions(info -> 
    new String[] {"northland", "eastland", "southland", "westland" }
));
```

\\[\downarrow\\]

```java
List<Argument> arguments = new ArrayList<>();
arguments.add(new StringArgument("world").replaceSuggestions(ArgumentSuggestions.strings(info -> 
    new String[] {"northland", "eastland", "southland", "westland" }
)));
```

#### Normal (strings with tooltips) suggestions

The `...T()` methods have been replaced with the normal methods above, and can use the `ArgumentSuggestions.stringsWithTooltips` method:

```java
Argument replaceSuggestionsT(Function<SuggestionInfo, IStringTooltip[]> suggestions);
Argument includeSuggestionsT(Function<SuggestionInfo, IStringTooltip[]> suggestions);
```

\\[\downarrow\\]

```java
Argument replaceSuggestions(ArgumentSuggestions suggestions);
Argument includeSuggestions(ArgumentSuggestions suggestions);
```

For example:

```java
List<Argument> arguments = new ArrayList<>();
arguments.add(new StringArgument("emote")
    .replaceSuggestionsT( info -> new IStringTooltip[] {
            StringTooltip.of("wave", "Waves at a player"),
            StringTooltip.of("hug", "Gives a player a hug"),
            StringTooltip.of("glare", "Gives a player the death glare")
        }
    )
);
```

\\[\downarrow\\]

```java
List<Argument> arguments = new ArrayList<>();
arguments.add(new StringArgument("emote")
    .replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(info -> new IStringTooltip[] {
            StringTooltip.of("wave", "Waves at a player"),
            StringTooltip.of("hug", "Gives a player a hug"),
            StringTooltip.of("glare", "Gives a player the death glare")
        }
    ))
);
```

#### Safe suggestions

Similar to above with normal suggestions, safe suggestions have been replaced with `replaceSafeSuggestions` and `includeSafeSuggestions` respectively:

```java
Argument replaceWithSafeSuggestions(Function<SuggestionInfo, S[]> suggestions);
Argument includeWithSafeSuggestions(Function<SuggestionInfo, S[]> suggestions);
```

\\[\downarrow\\]

```java
Argument replaceSafeSuggestions(SafeSuggestions<T> suggestions);
Argument includeSafeSuggestions(SafeSuggestions<T> suggestions);
```

These can be used with the `SafeSuggestions.suggest` and `SafeSuggestions.tooltips` methods to wrap existing functions. For example:

```java
List<Argument> arguments = new ArrayList<>();
arguments.add(new RecipeArgument("recipe").replaceWithSafeSuggestions(info -> 
    new Recipe[] { emeraldSwordRecipe, /* Other recipes here */ }
));
```

\\[\downarrow\\]

```java
List<Argument> arguments = new ArrayList<>();
arguments.add(new RecipeArgument("recipe").replaceSafeSuggestions(SafeSuggestions.suggest(info -> 
    new Recipe[] { emeraldSwordRecipe, /* Other recipes here */ }
)));
```

-----

## From version 6.2.0 or earlier to 6.3.0

Please refer to an older version of the documentation. This has been omitted to save space and reduce confusion in this upgrading section.