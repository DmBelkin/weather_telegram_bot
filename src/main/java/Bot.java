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

    private final String geolocationApiKey = "e7c5b9ac6a6046fbae832a1aae56910d";

    private DBConnection connection;

    @Override
    public String getBotUsername() {
        return "@learn_DM_weather_bot";
    }

    @Override
    public String getBotToken() {
        return "7430295335:AAErnQY2xt3Py2Vlcl-ChZR0j2muOgvOu8Q";
    }

    public Bot(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var message = update.getMessage();
        var user = message.getFrom();
        if (message.getText().contains("/")) {
            if (message.getText().equals("/")) {
                sendText(id, "/ + название города на латинице - получить дневной прогноз погоды" + "\n" +
                        "/ + history - получить историю запросов");
                return;
            }
            executeCommand(message.getText(), message);
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
            if (town.equals("history")) {
                sendText(id, connection.selectHistory(message.getFrom().getUserName()));
                return "";
            }
            double[] latAndLong = null;
            try {
                latAndLong = getCoordsByTownName(town);
                if (latAndLong == null || latAndLong.length == 0) {
                    sendText(id, "Неверный запрос");
                }
                ApiConnect connect = new ApiConnect();
                mess = connect.getConnection(connect.setParams("" +
                                latAndLong[0], "" + latAndLong[1],
                        "temperature_2m,relative_humidity_2m" +
                                ",apparent_temperature,precipitation," +
                                "cloud_cover,wind_speed_10m", town));
                connection.dbTransaction(message.getFrom().getUserName(), mess, town);
                sendText(id, mess);
            } catch (IOException e) {
                sendText(id, "Неверный запрос");
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

