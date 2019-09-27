package IO;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by root on 01.11.18 with love.
 */
public class CustomBD {

    JsonObject mainJson;

    public CustomBD() {
        try {
            File file = new File("mainJson.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader(file));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            JsonParser jsonParser = new JsonParser();
            try {
                mainJson = jsonParser.parse(result.toString()).getAsJsonObject();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                mainJson = new JsonObject();
            }
            in.close();
        } catch (Exception e) {
            System.out.println("Error init DB");
            e.printStackTrace();
            mainJson = null;
        }
    }

    public boolean checkUser(String username, String password) throws NoSuchAlgorithmException {
        if(!existUser(username)) {
            return false;
        }
        JsonObject info = mainJson.get(username).getAsJsonObject();
        String hash = getSHA512FromString(password);
        return info.get("password").getAsString().equals(hash);
    }

    public boolean addUser(String username, String password, String email, String name, String address) throws NoSuchAlgorithmException, IOException {
        if (existUser(username)) {
            return false;
        }
        JsonObject info = new JsonObject();
        info.addProperty("password", getSHA512FromString(password));
        info.addProperty("email", email);
        if (name != null)
            info.addProperty("name", name);
        if (address != null)
            info.addProperty("address", address);
        mainJson.add(username, info);
        commit();
        return true;
    }

    private boolean existUser(String username) {
        try {
            JsonObject info = mainJson.get(username).getAsJsonObject();
            return info != null;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void commit() throws IOException {
        File file = new File("mainJson.txt");
        if(file.exists()) {
            file.delete();
        }
        file.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(mainJson.toString());
        out.flush();
        out.close();
    }

    private static String getSHA512FromString(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(str.getBytes());
        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuilder hashCodeBuffer = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            hashCodeBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return hashCodeBuffer.toString();
    }

}
