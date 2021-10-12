
Example Application start. 

```java
@SpringBootApplication
@ConfigurationPropertiesScan({"own.package"})
@ComponentScan({"de.mspark.jdaw.jda", "own.package"})
public class SpeedmeetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpeedmeetApplication.class, args);
    }
}

```

Example Command:

```java

@CommandProperties(trigger = "config", userGuildPermissions = Permission.ADMINISTRATOR)
public class ConfigCommand extends Command {....}

```
For this you have to implement the JDAWConfig interface. For example: 


```java
@ConstructorBinding /* Remove when updating to Spring 2.6 */
@ConfigurationProperties(prefix = "bot")
public record Config(
	String prefix, 
	String[] channelWhitelist, 
	String[] apiTokens) implements JDAWConfig {}
```

When you want to modify the discord JDA configuration, you provide a bean of the JDAConfigurationVisitor which 
is able to modify the JDABuilder.