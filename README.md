This project tries to provide an infrastructure for an easy and fast discord bot development. It is a library which provides a set of "standard" features for text commands which can be found in many self written discord bots.
This project uses [JDA](https://github.com/DV8FromTheWorld/JDA) (java discord api implementation)

It aims on simplifying the development of independent commands. It also has support for multiple bot tokens for balancing mechanisms.

You need at least one configured bot token. When giving multiple bot token, the first one in the list is always the "main" bot. 

# Features
It's a goal to make command development easier. By using JDAW you can write new commands within seconds without implementing permission checks (user, guild and global) or trigger checks by yourself. Other features are:

* [A global help command](../../wiki/Help-Command)
* [Support for multiple discord tokens](../../Command-Balancing)
* [Seperate guild configuration](../../wiki/Multiguild-Support) (different prefix per guild)
* [View pre-installed maintenance commands](../../wiki/Preinstalled-Commands)

Work in progress:
* Enable Guild Prefix Command (again)
* Slash command support

Have a look at the [wiki](../../wiki) for further explanations!
An example discord bot which uses JDAW can be found in [this](https://github.com/mspark/example-jdaw) repository. 

# Commands

A command is written by extending the `TextCommand` interface. A very simple command can look like this:

```java
class HelloWorldCmd extends TextCommand {

    @Override
    public String trigger() {
        return "hello";
    }

    @Override
    public String description() {
        return "Prints hello world";
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        msg.reply("Hello World").submit();        
    }

}
```

The command is now executed when all of these conditions are met:
* text starts with prefix (like `!`)
* text starts with command trigger ("hello")
* user has enough permissions (nothing configured here)
* bot has enough permissions (nothing configured here)

Further explanation with an example can be found [in the wiki](../../wiki/Writing-Commands). 

# Quickstart
1. Implement the `JdawConfig` interface. 
2. Use the `JdawInstanceBuilder` and set the `JdawConfig`
3. Register written implementation of `TextCommand`
4. Start instance


Short runnable Example:

```java
class TestCommand extends TextCommand {

    @Override
    public String trigger() {
        return "test";
    }

    @Override
    public String description() {
        return "Test command";
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        if (cmdArguments.get(0).equalsIgnoreCase("hallo")) { // will trigger on ?test hallo
            msg.reply("hallo!").submit();
        } else {
            msg.reply("You invoked test without arguments").submit();
        }
    }

}

class OwnJdawConfig implements JdawConfig {

    @Override
    public String defaultPrefix() {
        return "?";
    }

    @Override
    public String[] apiTokens() {
        return new String[] { "MAIN API TOKEN", "BOOSTER API TOKEN" };
    }
    
}

public class App {
    
    public static void main(String[] args) {
        new JdawInstanceBuilder(new OwnJdawConfig())
            .addCommand(new TestCommand())
            .buildJdawInstance();
    }
}
```

You determine a lot of behaviour by overriding methods from the `TextCommand` class like needed permissions, if the command is allowed in private chats and a lot more. 


## Further JDAW configuration

- You can disable the default commands via the instance builder, (see [Pre-installed Commands](../../wiki/Preinstalled-Commands))
- Enable the help command by providing a help configuration, see [Help Command Wiki](../../wiki/Help-Command). 
- You can modify the JDA (instance of a discord bot) by setting a `JDAConfigModifier` , see [JDAW Configuration page](../../wiki/JDAW-Configuration)
- You can listen to JDAW Events by providing a `JDAWEventListener`. By default all text commands can listen to JDAW events.

# Use JDAW
The JDAW package is not published in the maven central repository. Currently it is only available via github packages. In order to use this you need to configure your maven according to the offical [github documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token). In the end you need something like this in your `~/.m2/settings.xml`:

```xml
<repository>
   <id>github</id>
   <url>https://maven.pkg.github.com/mspark/jdaw</url>
   <snapshots>
       <enabled>true</enabled>
   </snapshots>
</repository>
```


In your project pom you need to add the distribution management: 

```xml
<distributionManagement>
	<repository>
		<id>github-jdaw</id>
		<name>JDAW GitHub Packages</name>
		<url>https://maven.pkg.github.com/mspark/jdaw</url>
	</repository>
</distributionManagement>
```

Now you can add the dependency to JDAW.

```xml
<dependencies>
  <dependency>
    <groupId>de.mspark.de</groupId>
    <artifactId>jdaw</artifactId>
    <version>6.0</version>
  </dependency>
</dependencies>

```
Get the latest version from the Githubs-Packages. See https//github.com/mspark/jdaw/packages/1115525
