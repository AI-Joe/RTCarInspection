package com.lieb.j.traininspection;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.FindByIndexOptions;
import com.cloudant.client.api.model.IndexField;
import com.google.gson.JsonObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic Restructure for offline use
 */

public class TrainObject {
    private String tid = "";
    private List<CarObject> arrayCo = new ArrayList<>();
    private List<JsonObject> c = new ArrayList<>();
    public Context context;


    public TrainObject(String trainID){
        this.tid = trainID;

        try {
            InputStream is = context.getAssets().open("cloudantclient.properties");
            InputStreamReader ir = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(ir);

            String username = br.readLine().substring(8);
            String account = br.readLine().substring(9);
            String pass = br.readLine().substring(9);

            CloudantClient client = ClientBuilder.account(account)
                    .username(username)
                    .password(pass)
                    .build();

            String dbname = "train-inspection";

            Database dbCars = client.database(dbname, false); //new client for datbase manipulation
            dbCars.createIndex("trainid", "trainid", "json", new IndexField[]{ //new index for querying
                    new IndexField("trainid", IndexField.SortOrder.asc),
                    new IndexField("loc", IndexField.SortOrder.asc),
                    new IndexField("type", IndexField.SortOrder.asc),
                    new IndexField("_id", IndexField.SortOrder.asc)
            });


            dbCars = client.database(dbname, true);

            //might need to make this a globally passed train, which the c value can change during this query.
            this.c = dbCars.findByIndex("\"selector\": {" +
                            "\"trainid\": \"" + this.tid + "\" }", JsonObject.class,
                    new FindByIndexOptions().fields("_id").fields("_rev").fields("loc").fields("type").fields("S_W")); //query result

        }
        catch(IOException e){
            System.out.println(e);
        }

    }

    public List<CarObject> getCars(){


        for(JsonObject car: c){
            String id = car.get("_id").getAsString();
            String loc = car.get("loc").getAsString();
            String rm = car.get("reportingmark").getAsString();
            String type = car.get("type").getAsString();
            Boolean SW = car.get("S_W").getAsBoolean();

            CarObject nc = new CarObject(id,loc,rm,type,SW);
            arrayCo.add(nc);

        }


        return this.arrayCo;
    }
    public String getTrainID(){
        return this.tid;
    }
    public int countCars(){
        return this.arrayCo.size();
    }
}
