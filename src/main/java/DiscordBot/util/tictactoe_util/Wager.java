package DiscordBot.util.tictactoe_util;

import DiscordBot.commands.tictactoe.ListOfWagers;
import DiscordBot.util.economy.Wallet;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Wager {

    private long challengerId = -1;
    private long targetId = -1;
    private int wagerAmount = -1;

    public Wager(){
        /*switch (setWager(channel, message, content.substring(6))) {
            case -1:
                channel.sendMessage("Invalid input!").complete();
                return;
            case 0:
                pushNewWager(channel, conn);
                channel.sendMessage("Your wager against the CPU has been set!").complete();
            case 1:
                createPendingWager(channel, message, content, conn);
                channel.sendMessage("Your wager against a player is pending acceptance!").complete();
        }*/
    }

    public long getChallengerId() {
        return challengerId;
    }

    public long getTargetId() {
        return targetId;
    }

    public int getWagerAmount() {
        return wagerAmount;
    }

    public boolean setChallengerId(Message message) {
        if (!verifyFormat(message)) {
            return false;
        }
        this.challengerId = message.getAuthor().getIdLong();
        return true;
    }

    public boolean setTargetId(Message message) {
        if (!verifyFormat(message)) {
            return false;
        }
        if (message.getMentionedUsers().isEmpty()) {
            this.targetId = 0;
        } else {
            this.targetId = message.getMentionedUsers().get(0).getIdLong();
        }
        return true;
    }

    public boolean setWagerAmount(Message message) {
        if (!verifyFormat(message)) {
            return false;
        }
        this.wagerAmount = Integer.parseInt(message.getContentRaw().substring(message.getContentRaw().lastIndexOf(" ")));
        return true;
    }

    private boolean verifyFormat(Message message) {
        return message.getContentRaw().trim().matches("^ [0-9]*$")
                || message.getContentRaw().trim().matches("^ @.* [0-9]*$");
    }

    private int setWager(MessageChannel channel, Message message, String content){

        int returnValue = -1;
        // If wager size is too large, return -1
        if (content.substring(message.getContentRaw().lastIndexOf(' ')).length() >= 6) {
            return -1;
        }

        if (content.matches("^ [0-9]*$")) { // Challenging CPU format
            this.targetId = 0;
            returnValue = 0;
        }
        else if (content.matches("^ @.* [0-9]*$")){ // Challenging human format
            if (message.getMentionedUsers().isEmpty()) {
                channel.sendMessage("Your target user does not exist!").complete();
                return -1;
            }
            this.targetId = message.getMentionedUsers().get(0).getIdLong();
            returnValue = 1;
        }

        if (returnValue > -1) {
            this.challengerId = message.getAuthor().getIdLong();
            this.wagerAmount = Integer.parseInt(content.substring(content.lastIndexOf(" ")));
            return returnValue;
        } else {
            return -1;
        }
    }

    private void pushNewWager(MessageChannel channel, Connection conn) {
        try {
            PreparedStatement challengeCpu = conn.prepareStatement("INSERT INTO tictactoe VALUES ("
            + this.challengerId + ", " + this.targetId + ", " + wagerAmount + ")");
        } catch (SQLException e){
            e.printStackTrace();
            channel.sendMessage("Could not push your wager to database. Please contact a moderator!").queue();
        }
    }

    private void createPendingWager(MessageChannel channel, Message message, String content, Connection conn) {

        // Check if author already has a wager pending
        for (Wager w: ListOfWagers.getWagers()) {
            if (w.getChallengerId() == this.challengerId){
                channel.sendMessage("You already have a pending wager!").complete();
                return;
            }
        }

        // Check if author can afford the wager
        Wallet challengerWallet = new Wallet(message.getAuthor(), conn);
        if (!challengerWallet.canAfford(this.wagerAmount)){
            channel.sendMessage("You can't afford that wager!").complete();
            return;
        }

        // Check if target can afford the wager
        Wallet targetWallet = new Wallet(message.getMentionedUsers().get(0), conn);
        if (!targetWallet.canAfford(wagerAmount)) {
            channel.sendMessage("Your target cannot afford this wager.").queue();
            return;
        }

        // Wait 5 minutes and prune expired wager
        try {
            Thread.sleep(300000);
            ListOfWagers.removeWager(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
            channel.sendMessage("Error in pruning timer. Please contact a moderator!").complete();
        }
    }
}
