import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by root on 25.10.18 with love.
 */

public class Map extends HttpServlet {

    private MapboxManager mapboxManager;

    public Map() {
        mapboxManager = new MapboxManager();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loginURI = "/login";

        boolean loggedIn = session != null && session.getAttribute("username") != null;
        if(!loggedIn) {
            response.sendRedirect(loginURI);
        } else {
            request.getRequestDispatcher("map.jsp").forward(request, response);
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String latitude = request.getParameter("latitude");
        String longitude = request.getParameter("longitude");
        String title = request.getParameter("title");
        String description = request.getParameter("description");

        String trunkDiameter = request.getParameter("trunk diameter");
        String speciesName = request.getParameter("species name");
        String scientificSpeciesName = request.getParameter("scientific species name");

        String newTitle = request.getParameter("newTitle");
        String newDescription = request.getParameter("newDescription");

        String action = request.getParameter("action");

        System.out.println(latitude);
        System.out.println(longitude);
        System.out.println(title);
        System.out.println(description);

        System.out.println(trunkDiameter);
        System.out.println(speciesName);
        System.out.println(scientificSpeciesName);

        System.out.println(newTitle);
        System.out.println(newDescription);
        System.out.println(action);


        System.out.println("\n Action: " + action);

        switch (action) {
            case "update":
                if (newTitle != null && newDescription != null && latitude != null && longitude != null
                        && !latitude.equals("") && !longitude.equals("")) {
                    mapboxManager.updateFeature(title, description, speciesName, scientificSpeciesName, newTitle,
                            newDescription, trunkDiameter, latitude, longitude);
                }
                break;


            case "delete":
                mapboxManager.deleteFeature(title, description);
                break;


            case "add":
                JsonObject geoJson = new JsonObject();
                geoJson.addProperty("type", "Feature");

                JsonObject properties = new JsonObject();
                properties.addProperty("title", title);
                properties.addProperty("description", description);
                properties.addProperty("trunk diameter", trunkDiameter);
                properties.addProperty("species name", speciesName);
                properties.addProperty("scientific species name", scientificSpeciesName);
                geoJson.add("properties", properties);

                JsonArray array = new JsonArray();
                array.add(Double.parseDouble(latitude));
                array.add(Double.parseDouble(longitude));

                JsonObject geometry = new JsonObject();
                geometry.add("coordinates", array);
                geometry.addProperty("type", "Point");
                geoJson.add("geometry", geometry);


                MapboxManager.prettyPrint(geoJson);
                mapboxManager.insertFeature(geoJson);
                break;

            default:
                System.out.println("Wrong action from USER!");
                break;
        }


        request.getRequestDispatcher("map.jsp").forward(request, response);
    }
}
