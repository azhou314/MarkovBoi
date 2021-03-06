package events;

import bot.Bot;
import commands.ReadCommand;
import markov.MarkovUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import threads.ReadChannelThread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/* Whenever a message is sent, inserts data from message into db.*/
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(net.dv8tion.jda.core.events.message.MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            Connection connection = null;
            try {
                /* Check that all ReadChannelThreads are done */
                while (true) {
                    for (ReadChannelThread rc : ReadCommand.threadSet) {
                        if (!rc.isAlive()) {
                            ReadCommand.threadSet.remove(rc);
                        }
                    }

                    if (ReadCommand.threadSet.isEmpty()) {
                        break;
                    }
                }

                connection = DriverManager.getConnection(MarkovUtils.URL);
                String id = event.getAuthor().getId();
                Message message = event.getMessage();
                if (!Bot.getStoredIDs().contains(id)) {
                    Bot.createNewChain(id, connection);
                }
                Bot.setConnection(connection);
                Bot.readMessage(id, message);
            } catch(SQLException e){
                event.getChannel().sendMessage(e.getMessage());
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {}
            }
        }
    }
}

