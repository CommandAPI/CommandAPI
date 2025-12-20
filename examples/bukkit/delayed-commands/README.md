# Delayed Commands

A simple example showcasing various ways to implement "delayed commands" with the CommandAPI. This was inspired by [#451](https://github.com/JorelAli/CommandAPI/issues/451). While delayed commands are not directly implemented in the CommandAPI, this example aims to show various ways to add them yourself. Developers are encouraged to find solutions that fit their own needs, perhaps adding them to this project for others to use.

### What is a "delayed command"?

Per the [original issue](https://github.com/JorelAli/CommandAPI/issues/451), a delayed command is limited to only run once within a certain time period. This can prevent users from spamming commands that might perform CPU-intensive tasks. This creates a delay between executions of a command, hence, a "delayed command".

### TODO: Write some examples and explain them