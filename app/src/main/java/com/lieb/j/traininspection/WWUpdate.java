package com.lieb.j.traininspection;

/**
 * Created by Joe Lieberman
 * 7/29/17
 * Penn State University
 */

import android.os.AsyncTask;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.IndexField;
import com.google.gson.JsonObject;

public class WWUpdate extends AsyncTask<JsonObject, Void, Void> {
        /**
         * Thread for updating the whats-wrong database when a button is changed from red to green or inspection is submitted.
         * @params Cars is the Car that is clicked
         */
        @Override
        protected Void doInBackground(JsonObject...Cars) {
            try {
                CloudantClient client = ClientBuilder.account("68210c1a-d572-410c-a691-0e05d6aa78ad-bluemix")
                        .username("68210c1a-d572-410c-a691-0e05d6aa78ad-bluemix")
                        .password("0a7515ddc83c641eb087259025e244e5f3b3d80e7fe8ff2dd871940cbf028993")
                        .build();

                String dbname = "whats-wrong";

                Database dbCars = client.database(dbname, true);
                dbCars.createIndex("trainid", "trainid", "json", new IndexField[]{
                        new IndexField("trainid", IndexField.SortOrder.asc),
                        new IndexField("W_W", IndexField.SortOrder.asc),
                        new IndexField("_id", IndexField.SortOrder.asc)
                });

                JsonObject C = Cars[0];

                //updates the whats-wrong database
                if(C.get("Update").getAsBoolean() == true) {
                    dbCars.save(C);
                }else{
                    dbCars.remove(dbCars.find(JsonObject.class, C.get("_id").toString().substring(1,C.get("_id").toString().length()-1)));
                }

                return null;
            } catch (Exception e) {
                
                return null;
            }
        }
}
