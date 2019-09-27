import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by root on 25.10.18 with love.
 */
public class MapboxManager {

    private static final String BASE_DATASETS_URL = "https://api.mapbox.com/datasets/v1";
    private static final String BASE_UPLOADS_URL = "https://api.mapbox.com/uploads/v1";
    private static final String USERNAME = "/re*****9";
    private static String DATASET_ID = "/cj*********32phyr3sz3j9";
    private static String TILESET_ID = "remm************phyr3sz3j9-6l4fj";
    private static final String ACCESS_TOKEN = "sk.eyJ1IjoicmVtbWFyazk5*******sNzNwajljYmNyIn0.jpqc08MSXr0FvFtMQLIrgQ";


    void insertFeature(JsonObject feature) throws IOException {
        String id;
        try {
            id = feature.get("id").getAsString();
        } catch (Exception e){
            System.out.println("Exception catch");
            id = null;
        }
        HttpURLConnection connection = getConnectionForInsertFeature(id);
        connection.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeBytes(feature.toString());
        out.flush();
        out.close();

        System.out.println("&&&&&Try insert or update&&&&&");
        prettyPrint(feature);
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        int responseCode = connection.getResponseCode();
        if(responseCode == 200) {
            //good
            createAnUpload();
        } else {
            System.out.println("Some error. insertOrUpdateFeature(). ReaponseCode = " + responseCode);
        }
    }

    void deleteFeature(String title, String description) throws IOException {

        JsonObject targetFeature = findFeature(title, description);

        if(targetFeature != null) {
            String targetID = targetFeature.get("id").getAsString();

            if (targetID != null) {
                HttpURLConnection connection = getConnectionForDeleteFeature(targetID);
                connection.setDoOutput(false);
                int responseCode = connection.getResponseCode();
                if (responseCode == 204) {
                    //good
                    createAnUpload();
                } else {
                    System.out.println("Some error occurred! ::deleteFeature()\n responseCode = " + responseCode);
                }
            }
        } else {
            System.out.println("Feature does not exist! delete()");
        }
    }

    void updateFeature(String title, String description, String newSpeciesName, String newScientificName, String newTitle,
                       String newDescription, String newDiameter, String newLongitude, String newLatitude) throws IOException {
        JsonObject targetFeature = findFeature(title, description);
        if(targetFeature != null) {
            JsonObject newProperties = targetFeature.get("properties").getAsJsonObject();

            newProperties.addProperty("title", newTitle);
            newProperties.addProperty("description", newDescription);
            newProperties.addProperty("trunk diameter", newDiameter);
            newProperties.addProperty("species name", newSpeciesName);
            newProperties.addProperty("scientific species name", newScientificName);

            JsonArray array = new JsonArray();
            array.add(Double.parseDouble(newLatitude));
            array.add(Double.parseDouble(newLongitude));

            JsonObject geometry = targetFeature.get("geometry").getAsJsonObject();
            geometry.add("coordinates", array);

            targetFeature.add("properties", newProperties);
            targetFeature.add("geometry", geometry);

            insertFeature(targetFeature);

        } else {
            System.out.println("Feature does not exist! update()");
        }

    }

    private void createAnUpload() throws IOException {
        HttpURLConnection connection = getConnectionForCreateAnUpload();
        connection.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());


        JsonObject requestJson = new JsonObject();


        requestJson.addProperty("tileset", TILESET_ID);
        requestJson.addProperty("url", "mapbox://datasets" + USERNAME + DATASET_ID);
        requestJson.addProperty("name", "chicago-parks");

        out.writeBytes(requestJson.toString());
        out.flush();
        out.close();

        String response = inputStreamToString(connection.getInputStream());
        JsonParser jsonParser = new JsonParser();
        JsonObject responseJson = jsonParser.parse(response).getAsJsonObject();
//        prettyPrint(responseJson);

        int responseCode = connection.getResponseCode();
        if(responseCode != 201) {
            System.out.println("Some error occurred!");
        }
    }

     JsonArray getAllFeature() throws IOException {
        HttpURLConnection connection = getConnetionForDataset();
        connection.setDoOutput(false);
        String response = inputStreamToString(connection.getInputStream());
        JsonParser jsonParser = new JsonParser();
        JsonObject features = jsonParser.parse(response).getAsJsonObject();
        return features.getAsJsonArray("features");
    }

    private HttpURLConnection getConnectionForDeleteFeature(String featureId) throws IOException{
        URL url = new URL(BASE_DATASETS_URL + USERNAME + DATASET_ID + "/features/" + featureId + "?access_token=" + ACCESS_TOKEN);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("DELETE");
        return httpURLConnection;
    }

    private HttpURLConnection getConnetionForDataset() throws IOException {
        URL url = new URL(BASE_DATASETS_URL + USERNAME + DATASET_ID + "/features?access_token=" + ACCESS_TOKEN);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        return httpURLConnection;
    }

    private HttpURLConnection getConnectionForCreateAnUpload() throws IOException {
        URL url = new URL(BASE_UPLOADS_URL + USERNAME + "?access_token=" + ACCESS_TOKEN);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
        return httpURLConnection;
    }

    private HttpURLConnection getConnectionForInsertFeature(String id) throws IOException {
        String newId;
        if(id != null) {
            newId = id;
        } else {
            newId = getNewId();
        }
        URL url = new URL(BASE_DATASETS_URL + USERNAME + DATASET_ID + "/features/" + newId + "?access_token=" + ACCESS_TOKEN);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("PUT");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        return httpURLConnection;
    }

    private HttpURLConnection getConnectionForRetriveCredential() throws IOException {
        URL url = new URL(BASE_UPLOADS_URL + USERNAME + "/credentials?access_token=" + ACCESS_TOKEN);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        return httpURLConnection;
    }

    private JsonObject retrieveS3Credentials() throws IOException {
        HttpURLConnection connection = getConnectionForRetriveCredential();
        connection.setDoOutput(false);

        int responseCode = connection.getResponseCode();
        if(responseCode == 200) {
            String response = inputStreamToString(connection.getInputStream());
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(response).getAsJsonObject();
            prettyPrint(jsonObject);
            return jsonObject;
        } else {
            String response = inputStreamToString(connection.getInputStream());
            System.out.println(response);
            return null;
        }
    }

    private String getNewId() throws IOException {
        File file = new File("counter");
        if(!file.exists()) {
            file.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write("50");
            out.flush();
            out.close();
            return "50";
        }
        BufferedReader in = new BufferedReader(new FileReader(file));
        String lastId = in.readLine();
        in.close();

        Integer last = Integer.parseInt(lastId);
        last++;
        file.delete();
        file.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(last.toString());
        out.flush();
        out.close();
        return last.toString();
    }

    private JsonObject findFeature(String title, String description) throws IOException {
        JsonArray features = getAllFeature();
        for(JsonElement element : features) {
            JsonObject object = (JsonObject) element;
            JsonObject properties = object.get("properties").getAsJsonObject();
            String t = properties.get("title").getAsString();
            String d = properties.get("description").getAsString();
            if(t.equals(title) && d.equals(description)) {
                System.out.println("#####Find Feature#####");
                prettyPrint(object);
                System.out.println("######################");
                return object;
            }
        }
        return null;
    }

    private static String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }

    static void prettyPrint(JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(jsonObject) + "\n");
    }
}
