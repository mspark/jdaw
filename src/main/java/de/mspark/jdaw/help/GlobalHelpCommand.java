package de.mspark.jdaw.help;

import java.lang.annotation.Annotation;
import java.util.List;

import de.mspark.jdaw.Command;
import de.mspark.jdaw.CommandProperties;
import de.mspark.jdaw.DistributionSetting;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.guilds.GuildConfigService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

/**
 * When implementing this with the {@link EnableHelpCommand} Annotation, a help command for all sub commands will be
 * available.
 *
 * @author marcel
 */
public class GlobalHelpCommand extends Command {

    @CommandProperties(trigger = "sample", description = "sample")
    static class DefaultProperties {
    }

    private HelpConfig config;
    private List<Command> allLoadedCmds;

    public GlobalHelpCommand(GuildConfigService gc, JDAManager jdas, List<Command> allLoadedCmds, HelpConfig config) {
        super(gc, jdas, DistributionSetting.MAIN_ONLY);
        this.allLoadedCmds = allLoadedCmds;
        this.config = config;
    }

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
        if (cmdArguments.isEmpty()) {
            var eb = new EmbedBuilder().setTitle(config.botName()).setDescription(config.botDescription());
            allLoadedCmds.stream()
                .filter(cmd -> cmd.userHasEnoughPermission(msg))
                .filter(cmd -> cmd.helpPageWithAliases(msg).isPresent())
                .forEach(cmd -> eb.addField(cmd.getTrigger(), cmd.getShortDescription(), false));
            msg.getChannel().sendMessageEmbeds(eb.build()).submit();
        } else {
            String wantedHelpPage = cmdArguments.get(0);
            allLoadedCmds.stream()
                .filter(c -> c.getTrigger().equalsIgnoreCase(wantedHelpPage))
                .findFirst()
                .filter(cmd -> cmd.userHasEnoughPermission(msg))
                .flatMap(c -> c.helpPageWithAliases(msg))
                .ifPresentOrElse(
                    helpPage -> msg.getChannel().sendMessageEmbeds(helpPage).submit(),
                    () -> msg.reply("No help page").submit());
        }
    }

    @Override
    public String getShortDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTrigger() {
        return "help";
    }

    /*
     * This class has no @CommandProperties annotation because the bean is created elsewhere. Through that, we need to
     * provide an instance of the annotation manually.
     */
    @Override
    public CommandProperties commandProperties() {
        var props = DefaultProperties.class.getAnnotation(CommandProperties.class);
        var command = this;
        return new CommandProperties() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return props.annotationType();
            }

            @Override
            public Permission[] userGuildPermissions() {
                return props.userGuildPermissions();
            }

            @Override
            public Permission[] userChannelPermissions() {
                return props.userChannelPermissions();
            }

            @Override
            public String trigger() {
                return command.getTrigger();
            }

            @Override
            public boolean executableWihtoutArgs() {
                return true;
            }

            @Override
            public String description() {
                return command.getShortDescription();
            }

            @Override
            public Permission[] botGuildPermissions() {
                return props.botGuildPermissions();
            }

            @Override
            public boolean botAdminOnly() {
                return props.botAdminOnly();
            }

            @Override
            public String[] aliases() {
                return props.aliases();
            }

            @Override
            public boolean privateChatAllowed() {
                return props.privateChatAllowed();
            }
        };
    }
}
