import com.byteowls.jopencage.JOpenCageGeocoder;
import com.byteowls.jopencage.model.JOpenCageForwardRequest;
import com.byteowls.jopencage.model.JOpenCageLatLng;
import com.byteowls.jopencage.model.JOpenCageResponse;
import lombok.Setter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Setter
public class Bot extends TelegramLongPollingBot {


    private final String geolocationApiKey = "";

    private DBConnection connection;


    private final String text = "/weather + название города на латинице, страна(штат, регион) - " +
            "получить дневной прогноз погоды" + "\n" +
            "/history - получить историю своих запросов";


    public Bot(DBConnection connection) {
        this.connection = connection;
        try {
            execute(new SetMyCommands(BotCommands.commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
        var message = update.getMessage();
        var user = message.getFrom();
        if (message.getText().equals("/help")) {
            sendText(message.getFrom().getId(), text);
        }
        if (message.getText().equals("/history")) {
            connection.selectHistory(message.getFrom().getUserName());
            try {
                sendDocUploadingAFile(connection.getHistory(), message.getFrom().getId());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            sendText(message.getFrom().getId(), text);
        } else if (message.getText().equals("/weather")) {
            sendText(message.getFrom().getId(), "Введите адрес места в формате / + ....");
        } else if (message.getText().startsWith("/")){
            if (message.getText().endsWith("start")) {
                sendText(message.getFrom().getId(), text);
                return;
            }
            executeCommand(message.getText(), message);
            sendText(message.getFrom().getId(), text);
        } else {
            sendText(message.getFrom().getId(), "Неверный запрос");
            sendText(message.getFrom().getId(), text);
        }
        System.out.println(user.getUserName() + " wrote " + message.getText());
    }

    public InlineKeyboardMarkup formMenu() {
        InlineKeyboardButton START = new InlineKeyboardButton();
        InlineKeyboardButton WEATHER = new InlineKeyboardButton();
        InlineKeyboardButton HISTORY = new InlineKeyboardButton();
        InlineKeyboardButton HELP = new InlineKeyboardButton();
        START.setCallbackData("/start");
        WEATHER.setCallbackData("/weather");
        HISTORY.setCallbackData("/history");
        HELP.setCallbackData("/help");
        List<InlineKeyboardButton> buttons = List.of(START, WEATHER, HISTORY, HELP);
        List<List<InlineKeyboardButton>> menu = List.of(buttons);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(menu);
        return markup;
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

    public void sendDocUploadingAFile(File file, Long chatId) throws TelegramApiException {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId("" + chatId);
        sendDocumentRequest.setDocument(new InputFile(file));
        execute(sendDocumentRequest);
    }

    public String executeCommand(String command, Message message) {
        String[] data = command.split("/");
        String mess = "";
        if (data.length < 2) {
            sendText(message.getFrom().getId(), "Неверный запрос");
        } else {
            String town = data[data.length - 1].trim();
            double[] latAndLong;
            try {
                latAndLong = getCoordsByTownName(town);
                if (latAndLong == null || latAndLong.length == 0) {
                    sendText(message.getFrom().getId(), "Неверный запрос");
                }
                ApiConnect connect = new ApiConnect();
                mess = connect.getConnection(connect.setParams("" +
                                latAndLong[0], "" + latAndLong[1], town));
                if (mess.isBlank()) {
                    sendText(message.getFrom().getId(), "Некорректно введен адрес");
                } else {
                    connection.dbTransaction(message.getFrom().getUserName(), mess, town);
                    sendText(message.getFrom().getId(), mess);
                }
            } catch (IOException e) {
                sendText(message.getFrom().getId(), "Некорректно указано место");
            }
        }
        return mess;
    }

    public double[] getCoordsByTownName(String name) throws IOException {
        JOpenCageGeocoder jOpenCageGeocoder = new JOpenCageGeocoder(geolocationApiKey);
        JOpenCageForwardRequest request = new JOpenCageForwardRequest(name);
        JOpenCageResponse response = jOpenCageGeocoder.forward(request);
        JOpenCageLatLng firstResultLatLng = response.getFirstPosition();
        return new double[]{firstResultLatLng.getLat(), firstResultLatLng.getLng()};
    }
}

