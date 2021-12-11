
This project tries to provide an infrastructure for an easy and fast discord bot development. It is a library which provides a set of "standard" features which can be found in many self written discord bots.

This project uses [JDA](https://github.com/DV8FromTheWorld/JDA) (java discord api implementation) and the [Spring Framework](https://spring.io/projects/spring-framework). 


It aims on simplifying the development of independent commands. It also has support for multiple bot tokens for balancing mechanisms.

You need at least one configured bot token. When giving multiple bot token, the first one in the list is always the "main" bot. 

# Features
It's a goal to make command development easier. By using JDAW you can write new commands within seconds without implementing permission checks (user, guild and global) or trigger checks by yourself. Other features are:

* [A global help command](../../wiki/Help-Command)
* [Support for multiple discord tokens](../../Command-Balancing)
* [Seperate guild configuration](../../wiki/Multiguild-Support) (different prefix per guild)

Work in progress:
* Slash command support

Look in the [wiki](../../wiki) for furthor explanations!
An example discord bot which uses JDAW can be found in [this](https://github.com/mspark/example-jdaw) repository. 

# Commands

A single command is a spring bean and is provided when using the `@CommandProperties` annotation. A very simple command can look like this:

```java
@CommandProperties(trigger = "test", description = "Demonstration of different JDA execution")
public class BalanceTestCommand extends Command {

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {}
   // ....
}

```
The command is now executed when all of these conditions are met:
* text starts with prefix (like `!`)
* text starts with command trigger
* user has enough permissions (you need to configure this)
* bot has enough permissions

Further explanation with an example can be found [in the wiki](../../wiki/Writing-Commands). 

# Setup
**The easiest way to use JDAW is by using Spring-Boot** . The JDA configuration (discord login etc.) is done via bean configuration, thus you need to enable the `ComponentScan` on the package `de.mspark.jdaw`. If you don't want the default command you can exclude the package `de.mspark.jdaw.commands` (see [Preinstalled Commands](../../wiki/Preinstalled-Commands))


**Example for main class:**

```java
@SpringBootApplication
@ComponentScan({"de.mspark.jdaw", "own.package"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

````
In addition to that, you have to implement the `JDAWConfig` interface to configure your application. You can do this directly by providing a bean (or load it from your application.properties). 

```java
@Bean
public JDAWConfig jdawConfig() {
    return new JDAWConfig() {
            
        @Override
        public String prefix() {
            return "$";
        }
       
        @Override
        public String[] apiTokens() {
            return new String []{ "", "" /* tokens here - first is main*/ };
        }
    };
}
```

You can modify the JDA bot configuration by providing beans for `JDAConfigurationVisitor` too. Have a look at the [JDAW Configuration page](../../wiki/JDAW-Configuration) if you have need for this (when you want to cache members etc.). 

# Use JDAW
The JDAW package is not published in the maven central repository. Currently it is only available via github packages. In order to use this you need to configure your maven according to the offical [github documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token). In the end you need something like this in your `~/.m2/settings.xml`:

```
<repository>
   <id>github</id>
   <url>https://maven.pkg.github.com/mspark/jdaw</url>
   <snapshots>
       <enabled>true</enabled>
   </snapshots>
</repository>
```


In your project pom you need to add the distribution management: 

```
<distributionManagement>
	<repository>
		<id>github-jdaw</id>
		<name>JDAW GitHub Packages</name>
		<url>https://maven.pkg.github.com/mspark/jdaw</url>
	</repository>
</distributionManagement>
```

Now you can add the dependency to JDAW.

```
<dependencies>
  <dependency>
    <groupId>de.mspark.de</groupId>
    <artifactId>jdaw</artifactId>
    <version>3.0</version>
  </dependency>
</dependencies>

```

