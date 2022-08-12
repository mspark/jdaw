package de.mspark.jdaw.maintainance;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import de.mspark.jdaw.cmdapi.JdawState;
import de.mspark.jdaw.cmdapi.TextCommand;
import de.mspark.jdaw.cmdapi.TextListenerAction;
import de.mspark.jdaw.cmdapi.Triggerable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class ListCommand extends TextCommand {

    private List<Triggerable> allLoadedCmds = new LinkedList<>();

    @Override
    public String trigger() {
        return "mlist";
    }

    @Override
    public String description() {
        return "List of all registered JDAW commands";
    }

    @Override
    public boolean botAdminOnly() {
        return true;
    }

    @Override
    public boolean executableWihtoutArgs() {
        return true;
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        Optional<String> triggerListText = allLoadedCmds.stream()
                .map(t -> t.trigger() + " | Aliases: " + Arrays.toString(t.aliases()))
                .reduce((a, b) -> a + "\n" + b);
        var embed = new EmbedBuilder().setTitle(description())
                .setDescription(triggerListText.orElse("No commands available"));
        msg.getChannel().sendMessageEmbeds(embed.build()).submit();
    }

    @Override
    public void onNewRegistration(JdawState stateOnRegistrationAttempt, TextListenerAction newRegisteredAction) {
        if (this.allLoadedCmds.isEmpty()) {
            this.allLoadedCmds.addAll(stateOnRegistrationAttempt.registeredActions());
        }
        this.allLoadedCmds.add(newRegisteredAction);
    }
}
