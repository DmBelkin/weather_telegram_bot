import com.byteowls.jopencage.JOpenCageGeocoder;
import com.byteowls.jopencage.model.JOpenCageForwardRequest;
import com.byteowls.jopencage.model.JOpenCageLatLng;
import com.byteowls.jopencage.model.JOpenCageResponse;
import lombok.Setter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Setter
public class Bot extends TelegramLongPollingBot {

    private final Long id = 1042994610L;

    private final String geolocationApiKey = "42695c7284b1456c8a339b6911ab5a49";

    private DBConnection connection;


    private final String text = "/weather + название города на латинице, страна(штат, регион) - " +
            "получить дневной прогноз погоды" + "\n" +
            "/history - получить историю своих запросов";


    public Bot(DBConnection connection) {
        this.connection = connection;
        sendText(id, text);
    }

    @Override
    public String getBotUsername() {
        return "@DmBLearnbot";
    }

    @Override
    public String getBotToken() {
        return "7495334044:AAGEyk58c2LTwF6UGIH06SNYQ8erQ4GldIQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
        var message = update.getMessage();
        var user = message.getFrom();
        if (message.getText().equals("/history")) {
            sendText(id, connection.selectHistory(message.getFrom().getUserName()));
            sendText(id, text);
        } else if (message.getText().equals("/weather")) {
            sendText(id, "Введите адрес места в формате / + ....");
            sendText(id, text);
        } else if (message.getText().startsWith("/")){
            executeCommand(message.getText(), message);
        } else {
            sendText(id, "Неверный запрос");
            sendText(id, text);
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

    public double[] getCoordsByTownName(String name) throws IOException {
        JOpenCageGeocoder jOpenCageGeocoder = new JOpenCageGeocoder(geolocationApiKey);
        JOpenCageForwardRequest request = new JOpenCageForwardRequest(name);
        JOpenCageResponse response = jOpenCageGeocoder.forward(request);
        JOpenCageLatLng firstResultLatLng = response.getFirstPosition();
        return new double[]{firstResultLatLng.getLat(), firstResultLatLng.getLng()};
    }
}

