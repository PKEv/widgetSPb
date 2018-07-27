package com.house.pavel.widgetspb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JSONParser {

    static String getPrice(String s) throws JSONException {
        String price;


        JSONObject obj = new JSONObject(s);

        //JSONArray arr = new JSONArray();
        JSONArray arr = obj.getJSONArray("data");

        //JSONObject pairObj = obj.getJSONObject("data");
        //JSONArray arr = new JSONArray();
        //arr.put(pairObj);
        JSONObject pairObj = arr.getJSONObject(2);
        price = pairObj.getString("value");

        return price;
    }
}
