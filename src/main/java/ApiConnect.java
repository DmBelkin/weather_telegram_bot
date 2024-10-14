import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class ApiConnect {

    private final String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?";

    private String town;

    private String lat;

    private String lon;

    private final String queryToWeatherApi = "temperature_2m,relative_humidity_2m," +
            "apparent_temperature,precipitation,cloud_cover,wind_speed_10m";

    public String setParams(String lat, String lon, String town) {
        this.lat = lat;
        this.lon = lon;
        this.town = town;
        return weatherApiUrl + "latitude=" + lat +
                "&longitude=" + lon + "&current=" + queryToWeatherApi;
    }

    public String getConnection(String uri) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type",
                "application/json; charset=utf-8");
        connection.setRequestProperty("Accept",
                "text/html,application/xhtml+xml," +
                        "application/xml;q=0.9,image/avif,image/webp," +
                        "image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        HttpURLConnection.setFollowRedirects(true);
        connection.setInstanceFollowRedirects(true);
        connection.setDoOutput(true);
        InputStream inputStream = connection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String response = bufferedReader.readLine();
        bufferedReader.close();
        connection.disconnect();
        return getResultMessage(response);
    }

    public String getResultMessage(String json) {
        JSONParser parser = new JSONParser();
        StringBuilder result = new StringBuilder();
        result.append(town + "\n");
        result.append("latitude: " + lat + "\n");
        result.append("longitude: " + lon + "\n");
        try {
            JSONObject object = (JSONObject)parser.parse(json);
            JSONObject obj = (JSONObject)object.get("current");
            result.append("Температура: " + obj.get("temperature_2m") + " °C\n");
            result.append("Ощущается как: " + obj.get("apparent_temperature") + " °C\n");
            result.append("Относительная влажность: " + obj.get("relative_humidity_2m") + "%\n");
            result.append("Облачность: " + obj.get("cloud_cover") + "%\n");
            result.append("Осадки: " + obj.get("precipitation") + "мм\n");
            result.append("Скрость ветра: " + obj.get("wind_speed_10m")  + "км/ч\n");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result.toString().trim();
    }
}
