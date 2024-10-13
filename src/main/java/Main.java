import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Scanner;

public class Main {

    private static final Long id = 1042994610L;

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        DBConnection connection = new DBConnection();
        Bot bot = new Bot(connection);
        botsApi.registerBot(bot);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            if (message.equals("ok")) {
                connection.getSessionFactory().close();
                return;
            }
            try {
                bot.sendText(id, message);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
