# List arguments

![A list argument with the command "/multigive @p stone grass_block dirt" and Minecraft suggestions with a list of Minecraft items](./images/arguments/listargument.png)

List arguments allows users to provide a list of values. This argument can take on two forms:

- Using an underlying `GreedyStringArgument`, so the greedy string argument rule applies - **this argument can only be used at the end of an argument list**.
- Using an underlying `TextArgument`, so this argument can be used anywhere in an argument list, but its contents must be surrounded with quotes (`"`).

-----

## The `ListArgumentBuilder`

Unlike other arguments, because this argument can be interpreted in various different ways, this argument can only be created using a `ListArgumentBuilder`, instead of directly accessing the `ListArgument` constructor. The `ListArgumentBuilder` loosely follows the following format:

\begin{align}
&\quad\texttt{Create a ListArgumentBuilder} \\\\
\rightarrow&\quad\texttt{\(Provide the list delimiter\)} \\\\
\rightarrow&\quad\texttt{Provide the list to pull suggestions from} \\\\
\rightarrow&\quad\texttt{Provide the mapper of the list items to a string} \\\\
\rightarrow&\quad\texttt{Build the ListArgument}
\end{align}

### Construction

First, you have to create a `ListArgumentBuilder` parameterized over the type that the list will generate. For example, if you want to create a list of Strings, you would use `new ListArgumentBuilder<String>`.

- The `nodeName` parameter represents the name of the node to use for the argument.
- The optional `delimiter` argument specifies the delimiter (separator) to use between entries. If a delimiter is not provided, a space `" "` will be used as the delimiter.

```java
public ListArgumentBuilder<T>(String nodeName);
public ListArgumentBuilder<T>(String nodeName, String delimiter);
```

$$\downarrow$$

> ### Allowing duplicates (Optional)
>
> If you want your users to enter duplicate entries in your list, you can use the `allowDuplicates` method to set whether duplicates are allowed. By default, duplicates are disabled.
>
> When duplicates are enabled, items that have been entered before can be displayed again in the list of suggestions:
>
> ```java
> ListArgumentBuilder.allowDuplicates(true);
> ```
>
> ![List arguments with duplicates enabled](./images/arguments/listargument_with_duplicates.gif)
>
> When duplicates are disabled, items that have already been entered will not appear in the list of suggestions:
>
> ```java
> ListArgumentBuilder.allowDuplicates(false);
> ```
>
> ![List arguments with duplicates disabled](./images/arguments/listargument_without_duplicates.gif)

$$\downarrow$$

> ### Allowing any value (Optional)
>
> If you want to disable list validation you can enable the `allowAnyValue` method to set whether any value is allowed. By default, this is disabled.
>
> When `allowAnyValue` is set to `true`, any value a player enters will be allowed in the list regardless of the list provided for the argument. Note that duplicate values are still only permitted when `allowDuplicates` is set to `true` too:
>
> ```java
> ListArgumentBuilder.allowAnyValue(true);
> ```
>
> ![List arguments with allowAnyValue set to true](./images/arguments/listargument_allow_any_values.gif)

$$\downarrow$$

### Providing the list

The `ListArgument` requires a list that the list argument can pull suggestions and validation from. The `ListArgument` does not support values which are not present in the provided list. There are three methods that can be used to provide a list for the `ListArgument`:

- Providing an immutable list (a list that doesn't change) using the `Collection<T>` parameter:

  ```java
  public ListArgumentBuilder withList(Collection<T> list);
  ```

- Providing a list that is determined when suggestions are being displayed to the user and before the command has been executed using the `Supplier<Collection<T>>` parameter:

  ```java
  public ListArgumentBuilder withList(Supplier<Collection<T>> list);
  ```

- Providing a list that is determined when suggestions are being displayed to the user and before the command has been executed, that also depends on the `SuggestionInfo` present when running the command, using the `Function<SuggestionInfo<CommandSender>, Collection<T>>` parameter:

  ```java
  public ListArgumentBuilder withList(Function<SuggestionInfo<CommandSender>, Collection<T>> list);
  ```

$$\downarrow$$

### Providing a list mapping function

In order to display suggestions, the `ListArgument` needs to know how to convert a list entry to a string. For example, a `Location` may be converted into `"x,y,z"`. The `ListArgumentBuilder` provides three methods for providing a mapping function:

- The `withStringMapper()` method converts the object to a string using the object's `.toString()` method. If the object is null, this method will populate it with the string `"null"`:

  ```java
  public ListArgumentBuilder withStringMapper();
  ```

- The `withMapper()` method requires a function that maps the object to a string:

  ```java
  public ListArgumentBuilder withMapper(Function<T, String> mapper);
  ```

- The `withStringTooltipMapper()` method requires a function that maps the object to an `IStringTooltip`. This allows you to also provide hover tooltips for the current item:

  ```java
  public ListArgumentBuilder withStringTooltipMapper(Function<T, IStringTooltip> mapper);
  ```

$$\downarrow$$

### Building the `ListArgumentBuilder`

To finish building the `ListArgument`, call the `buildGreedy()` or `buildText()` method. The `buildGreedy()` method will treat the list argument as a greedy string, which means you can only use this list argument at the end of the list of arguments you are declaring for the command. If you use the `buildText()` instead, you can use the list argument anywhere (and multiple times), but the list must be surrounded with quotation characters (`"`).

```java
public ListArgument<T> buildGreedy();
public ListArgument<T> buildText();
```

-----

## Examples

<div class="example">

### Example - Multi-give command

Say you wanted to give yourself multiple items in a single command. For this command, we'll use the following syntax, which lets you provide the number of items to give, and a list of materials:

```mccmd
/multigive <amount> <materials>
```

To do this, we create a command with an `IntegerArgument` to specify the amount (between 1 and 64), and a `ListArgument` that accepts a list of `Material` objects. We use the `ListArgumentBuilder` to provide a list of materials as well as a mapping function that converts the material's name to a lowercase string. By default, we use a space delimiter (separator) for arguments in the list.

<div class="multi-pre">

```java,Java
{{#include ../../commandapi-documentation-code/src/main/java/dev/jorel/commandapi/examples/java/Examples.java:argumentList1}}
```

```kotlin,Kotlin
{{#include ../../commandapi-documentation-code/src/main/kotlin/dev/jorel/commandapi/examples/kotlin/Examples.kt:argumentList1}}
```

```kotlin,Kotlin_DSL
{{#include ../../commandapi-documentation-code/src/main/kotlin/dev/jorel/commandapi/examples/kotlin/ExamplesKotlinDSL.kt:argumentList1}}
```

</div>

![A /multigive argument gif where a user types "/multigive 64 stone dirt cobblestone grass_block" and suggestions appear automatically. Running the command gives the player 64 stone, dirt, cobblestone and grass_block items in their hotbar](./images/arguments/listargument_multigive.gif)

</div>
