/**
 * Joe Lieberman
 * 8/1/2017
 * Penn State University
 */

package com.lieb.j.traininspection;

import android.content.Context;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        new WWClient(this) {
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

                        // needs to have spaces between whats wrong
                        String Cid = r.get("_id").toString();
                        Cid = Cid.substring(1, Cid.length() - 1);

                        TextView txtOb = new TextView(WWList.this);
                        //txtOb.setLayoutParams(lv.getLayoutParams());

                        String car = "Car id: " + Cid + "\n";
                        String issues = "Issues: " + obj;


                        txtOb.setText(car + issues  + "\n__________________________________________\n");
                        txtOb.setTextSize(30);
                        lv.addView(txtOb);
                    }

                }
        }.execute(tid);

    }
}
class WWClient extends AsyncTask<CharSequence, Void, List<JsonObject>> {
    private Context context;
    public WWClient(Context myContext){
        this.context = myContext;
    }
    /**
     * Accesses Whats-wrong database and querys with the index created for trains with matching trainids
     * @param Cars
     * @return query result
     */
    @Override
        protected List<JsonObject> doInBackground(CharSequence...Cars) {
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
