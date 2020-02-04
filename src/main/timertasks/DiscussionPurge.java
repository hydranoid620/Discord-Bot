package main.timertasks;

import main.Server;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

public class DiscussionPurge implements Runnable {

    /**
     * Purges all messages in the Discussion channel.
     */
    @Override
    public void run() {
        try {
            List<Message> messages;
            do {
                TextChannel discussionChannel = Server.getGuild().getTextChannelById(670857670214942730L);
                messages = new MessageHistory(discussionChannel).retrievePast(100).complete();
                if (messages.size() > 20)
                    discussionChannel.deleteMessages(messages.subList(20, messages.size())).queue();
            } while (messages.size() > 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
