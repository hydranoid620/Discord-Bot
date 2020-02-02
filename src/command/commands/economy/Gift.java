package command.commands.economy;

import command.Command;
import database.connectors.EconomyConnector;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Gift extends Command {

    EconomyConnector ec;

    /**
     * Initializes the command's key to "!gift"
     */
    public Gift() {
        super("!gift", true);
        ec = new EconomyConnector();
    }

    @Override
    public boolean keyMatches(String string) {
        return string.startsWith(getKey());
    }

    @Override
    public void start(MessageReceivedEvent event) {
        if (!inputIsValid(event.getMessage())) {
            event.getChannel().sendMessage("To gift someone *gc* say `!gift <\\@user> <amount>`").queue();
            return;
        }

        try {
            long targetId = event.getMessage().getMentionedMembers().get(0).getUser().getIdLong();
            int amount = Integer.parseInt(event.getMessage().getContentRaw().split(" ")[2]);

            ec.addOrRemoveMoney(targetId, amount);
            ec.addOrRemoveMoney(event.getAuthor().getIdLong(), -amount);
        } catch (Exception e) {
            printStackTraceAndSendMessage(event, e);
        }
    }

    private boolean inputIsValid(Message message) {
        if (message.getMentionedMembers().size() != 1)
            return false;

        String[] split = message.getContentRaw().split(" ");

        if (split.length > 3)
            return false;

        return split[2].matches("[1-9][0-9]*");
    }
}
