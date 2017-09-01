package com.lieb.j.traininspection;

/**
 * Created by Joe Lieberman
 * 7/29/17
 * Penn State University
 */

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.IndexField;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class WWUpdate extends AsyncTask<JsonObject, Void, Void> {
        private Context context;
        public WWUpdate(Context myContext){
            this.context = myContext;
        }
        /**
         * Thread for updating the whats-wrong database when a button is changed from red to green or inspection is submitted.
         * @params Cars is the Car that is clicked
         */
        @Override
        protected Void doInBackground(JsonObject...Cars) {
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
