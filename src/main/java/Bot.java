import com.byteowls.jopencage.JOpenCageGeocoder;
import com.byteowls.jopencage.model.JOpenCageForwardRequest;
import com.byteowls.jopencage.model.JOpenCageLatLng;
import com.byteowls.jopencage.model.JOpenCageResponse;
import lombok.Setter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;

@Setter
public class Bot extends TelegramLongPollingBot {

    private final Long id = 0L;

    private final String geolocationApiKey = "";

    private DBConnection connection;

    private final String text = "/ + название города на латинице, страна(штат, регион) - " +
            "получить дневной прогноз погоды" + "\n" +
            "/ + history - получить историю своих запросов";


    public Bot(DBConnection connection) {
        this.connection = connection;
        InlineKeyboardButton start = InlineKeyboardButton.builder()
                .text("START").callbackData("start")
                .build();
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(start)).build();

        sendMenu(id, "MENU", markup);

    }

    @Override
    public String getBotUsername() {
        return "";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getCallbackQuery().getData().equals("start")) {
            try {
                buttonTap(update.getCallbackQuery().getId());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        var message = update.getMessage();
        var user = message.getFrom();
        if (message.getText().equals("/history")) {
            sendText(id, connection.selectHistory(message.getFrom().getUserName()));
        } else if (message.getText().contains("/")) {
            executeCommand(message.getText(), message);
        } else {
            sendText(id, "Неверный запрос");
        }
        System.out.println(user.getUserName() + " wrote " + message.getText());
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(Long.toString(who))
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public String executeCommand(String command, Message message) {
        String[] data = command.split("/");
        String mess = "";
        if (data.length < 2) {
            sendText(id, "Неверный запрос");
        } else {
            String town = data[data.length - 1].trim();
            double[] latAndLong;
            try {
                latAndLong = getCoordsByTownName(town);
                if (latAndLong == null || latAndLong.length == 0) {
                    sendText(id, "Неверный запрос");
                }
                ApiConnect connect = new ApiConnect();
                mess = connect.getConnection(connect.setParams("" +
                                latAndLong[0], "" + latAndLong[1], town));
                connection.dbTransaction(message.getFrom().getUserName(), mess, town);
                sendText(id, mess);
            } catch (IOException e) {
                sendText(id, "Некорректно указано место");
            }
        }
        return mess;
    }

    public void sendMenu(Long who, String txt, InlineKeyboardMarkup kb){
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(txt)
                .replyMarkup(kb).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void buttonTap(String queryId) throws TelegramApiException {
        sendText(id, text);
        AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                .callbackQueryId(queryId).build();

        execute(close);
    }


    public double[] getCoordsByTownName(String name) throws IOException {
        JOpenCageGeocoder jOpenCageGeocoder = new JOpenCageGeocoder(geolocationApiKey);
        JOpenCageForwardRequest request = new JOpenCageForwardRequest(name);
        JOpenCageResponse response = jOpenCageGeocoder.forward(request);
        JOpenCageLatLng firstResultLatLng = response.getFirstPosition();
        return new double[]{firstResultLatLng.getLat(), firstResultLatLng.getLng()};
    }
}

