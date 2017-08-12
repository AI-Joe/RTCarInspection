/**
 * Joe Lieberman
 * 8/1/2017
 * Penn State University
 */

package com.lieb.j.traininspection;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.FindByIndexOptions;
import com.cloudant.client.api.model.IndexField;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * Prints whats wrong in a list form
 */
public class WWList extends AppCompatActivity {
    static CharSequence Trainid = "d";

    /**
     * gets details extra and prints cars with issues
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wwlist);
        final String[] tid = getIntent().getExtras().getStringArray("Details");

        if (tid.length>3){
            Button btnStartOver = (Button)findViewById(R.id.btnNext);
            btnStartOver.setText("Next Train");
            btnStartOver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(WWList.this, PreInspection.class);
                    startActivity(i);
                }
            });

        }else {
            Button btnNext = (Button) findViewById(R.id.btnNext);
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(WWList.this, TrainList.class);
                    i.putExtra("Details", tid);

                    startActivity(i);
                }
            });
        }

        Trainid = tid[2];

        try{
            populateList(Trainid);
        }catch (Exception e){
            //needs to catch error
        }
    }

    /**
     * prints cars that are in the whats wrong database
     * @param tid train id charsequence
     */
    public void populateList(CharSequence tid){
        new WWClient() {
            @Override
            protected void onPostExecute(List<JsonObject> results) {
                LinearLayout lv = (LinearLayout) findViewById(R.id.llWrong);
                TextView lblWW = (TextView) findViewById(R.id.lblwwList);
                lblWW.setText("Cars with issues:");
                lblWW.setTextSize(lv.getWidth() / 20);

                    for (JsonObject r : results) { //for each Jsonobject in the query results
                        String obj = r.get("W_W").toString();
                        obj = obj.substring(1, obj.length() - 1);

                        //Eventually needs to have Location added
                        //String loc = r.get("loc").toString();
                        //loc = loc.substring(1,loc.length()-1);

                        String Cid = r.get("_id").toString();
                        Cid = Cid.substring(1, Cid.length() - 1);


                        TextView txtOb = new TextView(WWList.this);


                        txtOb.setText("Car id: " + Cid + "\n"
                                + "Issues: " + obj + "\n________________________________________________________\n");
                        txtOb.setTextSize(30);

                        lv.addView(txtOb);
                    }

                }
        }.execute(tid);

    }
}
class WWClient extends AsyncTask<CharSequence, Void, List<JsonObject>> {
    /**
     * Accesses Whats-wrong database and querys with the index created for trains with matching trainids
     * @param Cars
     * @return query result
     */
    @Override
        protected List<JsonObject> doInBackground(CharSequence...Cars) {
            try {
                CloudantClient client = ClientBuilder.account("68210c1a-d572-410c-a691-0e05d6aa78ad-bluemix")
                        .username("68210c1a-d572-410c-a691-0e05d6aa78ad-bluemix")
                        .password("0a7515ddc83c641eb087259025e244e5f3b3d80e7fe8ff2dd871940cbf028993")
                        .build();

                String dbname = "whats-wrong";  //database name
                String tid = WWList.Trainid.toString();

                Database dbCars = client.database(dbname, true); //database client
                dbCars.createIndex("trainid", "trainid", "json", new IndexField[]{ //queriable index
                        new IndexField("trainid", IndexField.SortOrder.asc),
                        new IndexField("W_W", IndexField.SortOrder.asc),
                        new IndexField("_id", IndexField.SortOrder.asc)
                });

                List<JsonObject> cars = dbCars.findByIndex("\"selector\": {" +
                                "\"trainid\": \"" + tid + "\" }", JsonObject.class,
                        new FindByIndexOptions().fields("_id").fields("trainid").fields("W_W"));

                return cars; //query results

            } catch (Exception e) {
                return null;
            }
        }
}
