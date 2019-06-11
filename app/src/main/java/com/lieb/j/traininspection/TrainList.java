/**
 * Joe Lieberman
 * 7/26/17
 * Automated RT inspection
 */

package com.lieb.j.traininspection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Trainlist Class pulls specific train information from the cloudant server in the train-inspection database
 * TrainID will become one of the parts of the extra from intent details
 * CarID will become one of the parts of the extra from intent details
 * Date will become one of the parts of the extra from intent details
 */
public class TrainList extends Activity {
    static String TrainID;
    static String CarID = "d";


    /**
     *Creates activity and gets Extras
     *strWW WhatsWrong it will eventually tell me what color to make the buttons upon reStarting this activity
     *details is the train details from the preinspection activity which becomes the selector in the query
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_cars);


        //String[] strWW = getIntent().getExtras().getStringArray("WW");
        String[] details = getIntent().getExtras().getStringArray("Details");

        if (details != null) {
            TrainID = details[2];
        }

        try {
            getTrain();
        } catch (IOException e) {
            //I need help with error handling in situations like these
            //What i was thinking was send an error message, user clicks ok, then send the user back to the preInspection Activity for detail reentry
        }

    }

    /*
    Threads off the main UI thread for network security
     */
    public List<JsonObject> BOL = new ArrayList<>();
    protected void getTrain() throws IOException {
        @SuppressLint("StaticFieldLeak")
        final AsyncTask<CharSequence, Void, List<JsonObject>> execute = new CClient(this) {
            private ArrayList<String> btnColor = new ArrayList<>();

            @Override
            /*
              puts together the TrainList GUI by giving each of the car ids a button that changes color red/green depending on its physical state.
              @params result is returned from the query done in doinbackground()
             */
            protected void onPostExecute(final List<JsonObject> result) {
                LinearLayout GridCars = (LinearLayout) findViewById(R.id.CarsLayout);
                TextView lblCars = (TextView) findViewById(R.id.txtQueryKey);
                lblCars.setText("\bCars in Train: " + TrainID);
                lblCars.setTextSize(75);

                BOL = result;
                //if the user doesnt enter anything or leaves it blank

                if (TrainID.equals("Enter Train ID") || result.size() == 0 || result==null) {
                    lblCars.setText("Invalid Train ID, Please Enter a Train ID that has cars.");
                    lblCars.setTextColor(Color.RED);
                    final Intent n = new Intent(TrainList.this, PreInspection.class);
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            startActivity(n);
                        }
                    }, 3000);
                } else {
                    try {
                        //goes through each train checking for their S_W attribute to give the button a color
                        for (JsonObject r : result) {
                            CarID = r.get("_id").toString();
                            boolean boolSW = r.get(("S_W")).getAsBoolean();

                            CarID = CarID.substring(1, CarID.length() - 1); //returns with " " around it

                            final Button btnCars = new Button(TrainList.this);
                            btnCars.setText(CarID);
                            btnCars.setTextSize(50);
                            btnCars.setClickable(true);

                            if (boolSW) {
                                btnCars.setBackgroundColor(Color.RED);
                                btnColor.add("R" + CarID);
                            } else {
                                btnCars.setBackgroundColor(Color.GREEN);
                                btnColor.add("G" + CarID);
                            }

                            btnCars.setOnClickListener(new View.OnClickListener() {
                                /**
                                 * always checks to see if the user is positive of the change, this is incase of accidental misclicks
                                 * on a normal click it will change the color to green and the S_W value to false
                                 */
                                @Override
                                public void onClick(final View v) {
                                    //if the button is clicked and not already green, it changes the color to green and updates S_W attribute

                                    final Button b = (Button) v;
                                    String id = b.getText().toString();
                                    String match = "";
                                    for (String n : btnColor) {
                                        if (id.equals(n.substring(1))) {
                                            match = n;
                                        }
                                    }
                                    if (match.substring(0, 1).equals("R")) {
                                        btnColor.remove(match);
                                        match = "G" + match.substring(1);
                                        btnColor.add(match);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(TrainList.this);
                                        ListView lv = new ListView(TrainList.this);
                                        builder.setView(lv);
                                        builder.setMessage("Are you sure you want to change the status of the car?");
                                        builder.setCancelable(false);

                                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                v.setBackgroundColor(Color.GREEN);
                                                updateSW("g" + b.getText());
                                                JsonObject j = new JsonObject();
                                                j.addProperty("_id", b.getText().toString());
                                                j.addProperty("Update", false);
                                                new WWUpdate(TrainList.this).execute(j);

                                            }
                                        });
                                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });

                                        AlertDialog alertWW = builder.create();
                                        alertWW.show();
                                    }
                                }
                            });
                            btnCars.setOnLongClickListener(new View.OnLongClickListener() {
                                /**
                                 * on a long click it will change the color to red and the S_W value to True
                                 */
                                @Override
                                public boolean onLongClick(final View v) {
                                    //if the button is longclicked, it changes the color to red and updates S_W attribute

                                    final Button b = (Button) v;
                                    boolean wasRed = false;

                                    String id = b.getText().toString();
                                    String match = "";
                                    for (String n : btnColor) {
                                        if (id.equals(n.substring(1))) {
                                            match = n;
                                        }
                                    }

                                    if(match.substring(0,1).equals("R"))
                                        wasRed = true;

                                    final boolean isRed = wasRed;

                                    btnColor.remove(match);
                                    match = "R" + match.substring(1);
                                    btnColor.add(match);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(TrainList.this);
                                    ListView lv = new ListView(TrainList.this);
                                    builder.setView(lv);

                                    builder.setMessage("Are you sure you want to change the status of the car?");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            v.setBackgroundColor(Color.RED);
                                            Intent i = new Intent(TrainList.this, Inspection.class);

                                            //if already was red, then put extra wasRed = True, else false

                                            i.putExtra("CarID", b.getText().toString());
                                            i.putExtra("Details", getIntent().getExtras().getStringArray("Details"));
                                            i.putExtra("WasRed", isRed);

                                            updateSW("r" + b.getText());

                                            startActivity(i);

                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });


                                    AlertDialog alertWW = builder.create();
                                    alertWW.show();

                                    return true;

                                }
                            });
                            GridCars.addView(btnCars);

                        }

                    } catch (Exception e) {
                        lblCars.setText(e.toString());
                        lblCars.setTextColor(Color.RED);
                    }

                    finishBtn(GridCars);
                    ProgressBar pg = (ProgressBar) findViewById(R.id.pgTrainCars);
                    pg.setVisibility(View.INVISIBLE);
                }


            }

        }.execute();

    }

    /**
     * adds the finish button with an alert dialog to make sure the user is done
     * @param g Linear layout from cclient thread
     */
    public void finishBtn(LinearLayout g) {
        Button finish = new Button(TrainList.this);
        finish.setText("Finish");
        finish.setTextSize(65);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TrainList.this);
                ListView lv = new ListView(TrainList.this);
                builder.setView(lv);
                builder.setMessage("Are you sure you are done?");
                builder.setCancelable(false);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(TrainList.this, WWList.class);
                        String[] details = getIntent().getExtras().getStringArray("Details");
                        String[] fdetails = Arrays.copyOf(details, details.length+1);
                        fdetails[fdetails.length-1] = "f";

                        i.putExtra("Details", fdetails);
                        startActivity(i);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertWW = builder.create();
                alertWW.show();
            }
        });

        g.addView(finish);

    }

    /**
     * sends the id with either r or g concatenated to the beginning to change the color
     * @param id the id number of the car which is the primary key of the db
     */
    public void updateSW(final CharSequence id) {
        CarID = id.toString();
        new CClient(this).execute(CarID);

    }
}
/**
 * Thread to access cloudant account and query train-inspection db for cars that have the same train ID that the user is asking for
 */
class CClient extends AsyncTask<CharSequence, Void, List<JsonObject>> {
    public Context context;
    CClient(Context myContext){
        this.context = myContext;
    }
    /**
     * Connects to my bluemix cloudant client and is a thread off of the Main UI for network Security
     * Currently uses the admin username/password/account--this needs to be changed to API keys.
     *
     * @param Cars is the carid with a concatenated letter at the beginning for color specification
     * @return the results of a query of JsonObjects in the database Train-Inspection
     */
    @Override
    protected List<JsonObject> doInBackground(CharSequence... Cars) {
        try {

            InputStream is = context.getAssets().open("cloudantclient.properties");
            InputStreamReader ir = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(ir);

            String username = br.readLine().substring(8);
            String account = br.readLine().substring(9);
            String pass = br.readLine().substring(9);
            String strCid = " ";

            if(Cars.length>0 && Cars[0]!=null){
               CharSequence cid = Cars[0];
               strCid = (String)cid;
            }

            CloudantClient client = ClientBuilder.account(account)
                    .username(username)
                    .password(pass)
                    .build();


            String dbname = "train-inspection"; //database name
            Database dbCars = client.database(dbname, false); //new client for datbase manipulation
            if (strCid.substring(0, 1).equals("r")) { //if the user longclicks it concatenates a r to the front of the car id
                String id = strCid.substring(1, strCid.length());

                JsonObject j = (dbCars.find(JsonObject.class, id)); //makes json object with attribute "S_W" = true, and id = car id
                j.addProperty("S_W", true);
                strCid = "d";
                dbCars.update(j);
                dbCars.save(j);


            } else if (strCid.substring(0, 1).equals("g")) {
                String id = strCid.substring(1, strCid.length());

                JsonObject j = (dbCars.find(JsonObject.class, id));
                j.addProperty("S_W", false);

                dbCars.update(j);

                dbCars.save(j);

                TrainList.CarID = "d";

            }
            dbname = "train-inspection";
            dbCars = client.database(dbname, false);


            List<JsonObject> c = new ArrayList<>();
            //c = dbCars.findByIndex("\"selector\": {" +
            //                "\"trainid\": \"" + TrainList.TrainID + "\" }", JsonObject.class,
            //        new FindByIndexOptions().fields("_id").fields("_rev").fields("loc").fields("type").fields("S_W")); //query result

            //might need to make this a globally passed train, which the c value can change during this query.
            //TrainObject train = new TrainObject(TrainList.TrainID);

            return dbCars.findByIndex("\"selector\": {" +
                            "\"trainid\": \"" + TrainList.TrainID + "\" }", JsonObject.class,
                    new FindByIndexOptions().fields("_id").fields("loc").fields("type").fields("S_W").fields("reporting_mark")); //query result

        } catch (Exception e) {
            return null;
        }


    }

}

