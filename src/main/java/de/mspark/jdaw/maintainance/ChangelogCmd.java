package de.mspark.jdaw.maintainance;

import java.util.List;
import java.util.stream.Collectors;

import de.mspark.jdaw.cmdapi.TextCommand;
import de.mspark.jdaw.help.HelpConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ChangelogCmd extends TextCommand {

    // TODO
    /*
     * fix: only last 5 version
     * feat: send to well known channels or private DMs
     * feat: give a date for each version
     * feat: way to set upcoming versions
     */
    private final HelpConfig helpConfig;
    private final Changelog changelog;

    public ChangelogCmd(HelpConfig helpConfig, Changelog changelog) {
        this.helpConfig = helpConfig;
        this.changelog = changelog;
    }

    @Override
    public String trigger() {
        return "changelog";
    }

    @Override
    public String description() {
        return "Prints a summary of the latest changes to this bot.";
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        var embed = new EmbedBuilder()
                .setAuthor(helpConfig.botName())
                .setDescription("You will now see last 5 versions.");
        HelpConfig.addFooter(embed, helpConfig);
        changelog.getVersions().stream().forEach(version -> {
            String changes = version.getChanges().stream().collect(Collectors.joining("\n ▫️"));
            embed.addField(new Field("⚙️ Version " + version.getNumber() + " ⚙️", " ▫️"+ changes , false));
        });
        msg.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public boolean executableWihtoutArgs() {
        return true;
    }
}
