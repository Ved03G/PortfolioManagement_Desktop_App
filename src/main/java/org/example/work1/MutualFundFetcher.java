package org.example.work1;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MutualFundFetcher {
    public JSONObject fetchFundDataBySchemeCode(String schemeCode) {
        try {
            URL url = new URL("https://api.mfapi.in/mf/" + schemeCode);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(reader);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // Fetch API Data
    public JSONArray fetchData() {
        try {
            URL url = new URL("https://api.mfapi.in/mf");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JSONParser parser = new JSONParser();
            return (JSONArray) parser.parse(reader);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Display Data in Your Application
    public void displayFundsInApplication() {
        JSONArray mutualFunds = fetchData();
        if (mutualFunds != null) {
            for (Object obj : mutualFunds) {
                JSONObject fund = (JSONObject) obj;
                System.out.println("Scheme Name: " + fund.get("schemeName"));
                System.out.println("NAV: " + fund.get("nav"));
                System.out.println("Date: " + fund.get("date"));
                System.out.println("Scheme Type: " + fund.get("schemeType"));
                System.out.println("-------------------");
            }
        }
    }
}
