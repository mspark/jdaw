
JDAW is a wrapper for dv8tion-JDA with spring. It tries to simplify the development of independent commands. JDAW also supports multiple discord tokens at once. 


You need at least one configured bot token. When giving multiple bot token, the first one in the list is always the "main" bot. 

### Commands


A single Command can be written like:

```java

@CommandProperties(trigger = "config", userGuildPermissions = Permission.ADMINISTRATOR)
public class ConfigCommand extends Command {....}

```

The Config command will be available as spring bean as well. Every command is executed on by the main bot. This behaviour is configurable. 

### Global Help Command
The global help command creates a help page for all command implementations and a short description of the whole application as well.

In order to acitivate the help command, implement the `GlobalHelpCommand` class with the `@HelpCmd` annotation. 

### Other Actions
If you want to run additional actions on any other configured bot, you can use the `JDAManager` inside a command implementation.  

### Setup

In order to start the application the spring components from JDAW must be found by spring. Enable the `ComponentScan`: 

```java
@SpringBootApplication
@ConfigurationPropertiesScan({"own.package"})
@ComponentScan({"de.mspark.jdaw.jda", "own.package"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

```
In addition to that, you have to implement the `JDAWConfig` interface to configure your application.

Loading from file:

```java
@ConstructorBinding /* Remove when updating to Spring 2.6 */
@ConfigurationProperties(prefix = "bot")
public record Config(
	String prefix, 
	String[] channelWhitelist, 
	String[] apiTokens) implements JDAWConfig {}
```

Via Bean: 

```java
@Bean
public JDAWConfig jdawConfig() {
    return new JDAWConfig() {
            
        @Override
        public String prefix() {
            return "$";
        }
            
        @Override
        public String[] channelWhitelist() {
            return new String []{ "", "" /* ids here */ };
        }
       
        @Override
        public String[] apiTokens() {
            return new String []{ "", "" /* tokens here - first is main*/ };
        }
    };
}
```
For modifying the JDA configuraiton, provide one or multiple beans of `JDAConfigurationVisitor`. Example for enable member caching:

```java
@Bean
public JDAConfigurationVisitor jdaConfigurationVisitor() {
    return jda -> jda.setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS);
}
```

### Use the project
```
<dependencies>
  <dependency>
    <groupId>de.mspark.de</groupId>
    <artifactId>jdaw</artifactId>
    <version>1.2</version>
  </dependency>
</dependencies>

```

In order to use github packages, you need to configure your maven according to the offical [github documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token).