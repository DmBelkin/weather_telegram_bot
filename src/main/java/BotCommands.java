import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotCommands {

    List<BotCommand> commands = List.of(
            new BotCommand("/start", "start bot"),
            new BotCommand("/help", "bot info"),
            new BotCommand("/weather", "weather"),
            new BotCommand("/history", "user history")
    );
}
