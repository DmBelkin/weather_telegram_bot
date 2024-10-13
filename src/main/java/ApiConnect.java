import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ApiConnect {

    private final String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?";

    public String setParams(String lat, String lon, String hourly) {
        return weatherApiUrl + "latitude=" + lat +
                "&longitude=" + lon + "&daily=" + hourly;
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
        try {
            List<String> time = new ArrayList<>();
            List<String> temperature = new ArrayList<>();
            JSONObject object = (JSONObject)parser.parse(json);
            System.out.println(object);
            JSONObject obj = (JSONObject)object.get("daily");
            JSONArray array = (JSONArray) obj.get("time");
            array.forEach(o -> {
                time.add(o.toString());
            });
            JSONArray tempsArray = (JSONArray)obj.get("temperature_2m_min");
            tempsArray.forEach(o -> {
                temperature.add(o.toString());
            });
            for (int i = 0; i < time.size(); i++) {
                result.append(time.get(i) + " " + temperature.get(i) + "\n");
            }
            System.out.println(result);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result.toString().trim();
    }
}
