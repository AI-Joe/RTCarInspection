/**
 * Joe Lieberman
 * 8/1/2017
 * Penn State University
 */

package com.lieb.j.traininspection;


import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.IndexField;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

/**
 * @Cid the Car ID number
 * @SW_True boolean which describes if something is wrong.
 * The class inspection assigns UI for whats wrong to each of the cars in the database whats-wrong
 */
public class Inspection extends AppCompatActivity {
    String Cid = "";
    boolean SW_True;
    boolean wasRed = false;
    CheckBox[] cbs;
    boolean[] redCbs = {false, false, false, false, false, false, false, false};
    public static ArrayList<String> strWW = new ArrayList<>();
    public static ArrayList<String> Coms = new ArrayList<>();

    @Override
    /**
     * Instantiates all the checkboxes for what could be wrong and gets the car id for the car that is being inspected
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        final String str_id = getIntent().getExtras().getString("CarID");

        wasRed = getIntent().getExtras().getBoolean("WasRed");

        //if wasRed == True{ toggleboxes(str_id)}
        Cid = str_id;

        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        TextView specs = (TextView) findViewById(R.id.txtDetails);
        specs.setText(str_id + "   |   " + (m + 1) + "/" + d);

        final GridLayout ww = (GridLayout) findViewById(R.id.gridWW);

        Switch sw = (Switch) findViewById(R.id.swtchWrong);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ww.setVisibility(View.VISIBLE);
                    SW_True = true;
                } else {
                    ww.setVisibility(View.INVISIBLE);
                    SW_True = false;
                }
            }
        });

        //if it is coming from a previously red
        if(wasRed){
            sw.toggle();
        }

        CheckBox app = (CheckBox) findViewById(R.id.App);
        CheckBox brakes = (CheckBox) findViewById(R.id.Brakes);
        CheckBox coupler = (CheckBox) findViewById(R.id.Coupler);
        CheckBox DG = (CheckBox) findViewById(R.id.DG);
        CheckBox ERB = (CheckBox) findViewById(R.id.ERB);
        CheckBox frame = (CheckBox) findViewById(R.id.Frame);
        CheckBox truck = (CheckBox) findViewById(R.id.KTruck);
        CheckBox other = (CheckBox) findViewById(R.id.Other);

        //sets all the listeners for the cars
        CheckBox[] checks = {app, brakes, coupler, DG, ERB, frame, truck, other};
        cbs = checks;

        for (CheckBox ch : checks) {
            SetListener(ch);
        }

        findCarSpecs(str_id);

        if(wasRed){
            checkPreviousBoxes(str_id, redCbs);
        }

    }

    /**
     * checks all previously clicked checkboxes based off the ww database
     * @param str_id car id to query ww db to check all previously checked checkboxes
     */
    public void checkPreviousBoxes(final String str_id, final boolean[] boolCbs){
        new WWClient(this){
            @Override
            protected void onPostExecute(List<JsonObject> results) {
                String WW = "";
                for(JsonObject r:results){
                    if(r.get("_id").getAsString().equals(str_id))
                        WW = r.get("W_W").getAsString();
                }

                int startWW = 0;
                String issues = "";
                int indexWW = WW.indexOf("|");
                do{
                    issues= WW.substring(startWW, indexWW);
                    startWW = indexWW+1;
                    indexWW = WW.indexOf("|", indexWW+1);

                    //gets checkbox id
                    String cbid = issues.substring(0,1);


                    if(cbid.equals("A")) {
                        redCbs[0] = true;
                        cbs[0].toggle();
                    }
                    else if(cbid.equals("B")){
                        redCbs[1] = true;
                        cbs[1].toggle();
                    }
                    else if(cbid.equals("C")) {
                        redCbs[2] = true;
                        cbs[2].toggle();
                    }
                    else if(cbid.equals("D")) {

                        redCbs[3] = true;
                        cbs[3].toggle();
                    }
                    else if(cbid.equals("E")) {

                        redCbs[4] = true;
                        cbs[4].toggle();
                    }
                    else if(cbid.equals("F")) {

                        redCbs[5] = true;
                        cbs[5].toggle();
                    }
                    else if(cbid.equals("K")) {

                        redCbs[6] = true;
                        cbs[6].toggle();
                    }
                    else {

                        redCbs[7] = true;
                        cbs[7].toggle();
                    }

                }while(indexWW<WW.length() && indexWW!= -1);


            }
        }.execute(str_id);
    }
    /**
     * @param str_id is the car id
     *               findCarSpecs autofills the inspection page with reporting mark and type information
     */
    public void findCarSpecs(final String str_id) {
        AsyncTask<CharSequence, Void, List<JsonObject>> execute = new CClient(this) {
            @Override
            protected List<JsonObject> doInBackground(CharSequence... Cars) {
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

                    Database dbCars = client.database(dbname, false);
                    /**dbCars.createIndex("trainid", "trainid", "json", new IndexField[]{
                            new IndexField("trainid", IndexField.SortOrder.asc),
                            new IndexField("loc", IndexField.SortOrder.asc),
                            new IndexField("_id", IndexField.SortOrder.asc),
                            new IndexField("reportingmark", IndexField.SortOrder.asc),
                            new IndexField("S_W", IndexField.SortOrder.asc),
                            new IndexField("type", IndexField.SortOrder.asc)
                    });**/

                    JsonObject j = (dbCars.find(JsonObject.class, str_id));

                    j.addProperty("reportingmark", j.get("reportingmark").toString());
                    j.addProperty("type", j.get("type").toString());
                    j.addProperty("S_W", j.get("S_W").getAsBoolean());

                    List<JsonObject> L_j = new ArrayList<JsonObject>();
                    L_j.add(j);


                    return L_j;
                } catch (IOException ex) {

                }
                return null;
            }


            @Override
            /**
             * adds the car information to the inspection UI
             */
            protected void onPostExecute(List<JsonObject> results) {
                JsonObject j = (JsonObject) results.get(0);
                String rm = j.get("reportingmark").getAsString();
                String type = j.get("type").getAsString();

                rm = rm.substring(1, rm.length() - 1);
                type = type.substring(1, type.length() - 1);

                //the text areas should match these results
                EditText txt_rm = (EditText) findViewById(R.id.txtRM);
                EditText txt_Type = (EditText) findViewById(R.id.txtType);

                txt_rm.setText(rm);
                txt_rm.setEnabled(false);
                txt_rm.setTextSize(30);
                txt_rm.setTextColor(Color.RED);

                txt_Type.setText(type);
                txt_Type.setEnabled(false);
                txt_Type.setTextSize(30);
                txt_Type.setTextColor(Color.RED);
            }
        }.execute(str_id);
    }

    /**
     * @param c the checkbox clicked
     *          sets checked change listeners for each checkbox.
     */
    public void SetListener(final CheckBox c) {
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                         @Override
                                         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                             boolean red = false;
                                             for(int i = 0; i<7; i++){
                                                 if(cbs[i] == c){
                                                     red = redCbs[i];
                                                 }
                                             }
                                             if (isChecked && !red)
                                                 checked(c);
                                             else if (!isChecked && red) {
                                                 AlertDialog.Builder b = new AlertDialog.Builder(Inspection.this);
                                                 b.setMessage("Do you want to view old or clear check previously checked");

                                                 b.setPositiveButton("View", new DialogInterface.OnClickListener() {
                                                     @Override
                                                     public void onClick(DialogInterface dialog, int which) {
                                                         for(int i = 0; i<7; i++){
                                                             if(cbs[i] == c){
                                                                 redCbs[i] = false;
                                                             }
                                                         }
                                                         c.toggle();
                                                     }
                                                 });
                                                 b.setNegativeButton("Clear", new DialogInterface.OnClickListener(){
                                                     @Override
                                                     public void onClick(DialogInterface dialog, int which) {
                                                         for(int i = 0; i<7; i++){
                                                             if(cbs[i] == c){
                                                                 redCbs[i] = false;
                                                             }
                                                         }
                                                     }
                                                 });
                                                 AlertDialog a = b.create();
                                                 a.show();

                                             }

                                         }
                                     });
    }

    /**
     * @param c all the possible outputs for WCBW
     *          populates the WCBW with the proper choices
     */
    public void checked(CheckBox c) {

        ArrayList<String> WCBW = new ArrayList<>();
        String[] ALapp = {"AL - Ladder", "AS - Sill Step", "AH - Hand Hold", "AR - Hand Rail/Cable", "AB - Running Board", "AC - Crossover and Step"};
        String[] ALbrakes = {"BF - Cut Out", "BC - Cylinder", "BR - Reservoir", "BV - Valves", "BP - Pipe, Fittings, Brkts, Angle Cock, Trainline",
                "BB - B.Beam/Hanger/Support", "BL - Brake Rod & Lever", "BT - Piston Travel/Not Set", "BH - Hand Brake",
                "BW - Hand Brake Wheel", "BA - Slack Adjuster"};
        String[] ALcoupler = {"CB - Body", "CC - Components(head)", "CD - Center Device", "CP - Vertical Pin", "CL - Operating Lever"};
        String[] ALdg = {"DY - Yoke", "DG - Gear", "DD - Cushion Device", "DR - Return Spring", "DC - Carrier Iron", "DS - Striker"};
        String[] ALerb = {"EE - End", "ES - Side", "EH - Hopper Slope Sheet", "ED - Door & Gate", "EA - Top Chord Angle", "ER - Roof",
                "EC - Hatch Dome Cover", "EP - Stake Pocket", "EM - Metal Stake & Post"};
        String[] ALframe = {"FB - Bolster Body", "FP - Center Plate", "FO - Off Center", "FF - Floor Bearer", "FC - Center Sill", "FS - Side Sill", "FE - End Sill"};
        String[] ALktruck = {"KH - Hot Box", "KR - Roller Bearing", "KA - RB Adapter", "KO - Wheel Out of Round", "KW - Wheel Set", "KB - Bolster",
                "KF - Side Frame", "KS - Spring & Snubber", "KD - Side Bring Defect", "KC - Side Bring Clearance"};
        String[] ALother = {"LB - Bulkhead Door", "RR - Refrigeration", "TA - Load Adjustment/Securement"};

        if (c.getId() == R.id.App) {
            WCBW.addAll(addWCBW(ALapp));
        } else if (c.getId() == R.id.Brakes) {
            WCBW.addAll(addWCBW(ALbrakes));
        } else if (c.getId() == R.id.Coupler) {
            WCBW.addAll(addWCBW(ALcoupler));
        } else if (c.getId() == R.id.DG) {
            WCBW.addAll(addWCBW(ALdg));
        } else if (c.getId() == R.id.ERB) {
            WCBW.addAll(addWCBW(ALerb));
        } else if (c.getId() == R.id.Frame) {
            WCBW.addAll(addWCBW(ALframe));
        } else if (c.getId() == R.id.KTruck) {
            WCBW.addAll(addWCBW(ALktruck));
        } else if (c.getId() == R.id.Other) {
            WCBW.addAll(addWCBW(ALother));
        }
        populate(WCBW, c);
    }

    public ArrayList<String> addWCBW(String[] a) {
        ArrayList<String> e = new ArrayList<>();
        Collections.addAll(e, a);
        return e;
    }

    /**
     * @param WCBW a list of strings that are options for what could be wrong with a car
     *             makes an alert dialog with multichoice checkboxes that add to an arraylist which is what actually is wrong with a car
     */
    public void populate(final ArrayList<String> WCBW, final CheckBox c) {
        final ArrayList<String> WW = new ArrayList<>();

        final AlertDialog.Builder builder = new AlertDialog.Builder(Inspection.this);
        boolean[] is_checked = new boolean[WCBW.size()];

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT / 2,
                LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout ll = new LinearLayout(Inspection.this);
        ll.setLayoutParams(lp);
        ll.setOrientation(LinearLayout.VERTICAL);

        final String[] strWCBW = new String[WCBW.size()];
        for (int i = 0; i < WCBW.size(); i++) {
            strWCBW[i] = WCBW.get(i);
        }



        builder.setTitle("Select What is Wrong:");


        builder.setMultiChoiceItems(strWCBW, is_checked, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    WW.add(strWCBW[which]);
                    AlertDialog.Builder b = new AlertDialog.Builder(Inspection.this);
                    LinearLayout lz = new LinearLayout(Inspection.this);
                    final EditText options = new EditText(Inspection.this);
                    final Switch sw = new Switch(Inspection.this);
                    sw.setTextSize(20);
                    sw.setText("Off for B side/ On for A side");

                    lz.setOrientation(LinearLayout.VERTICAL);
                    options.setTextSize(20);
                    options.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                    options.setText("Enter Specifics");

                    b.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(sw.isChecked())
                                Coms.add(options.getText().toString() + ", A side");
                            else
                                Coms.add(options.getText().toString() + ", B side");

                        }
                    });
                    b.setNegativeButton("No Comment",  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(sw.isChecked())
                                Coms.add("No Comment, A side");
                            else
                                Coms.add("No Comment, B side");

                        }
                    });
                    lz.addView(options);
                    lz.addView(sw);

                    b.setMessage("Enter any comments and select the side");
                    b.setTitle("Comments");

                    AlertDialog comment = b.create();
                    comment.setView(lz);
                    comment.show();

                } else if (!isChecked) {
                    WW.remove(strWCBW[which]);
                }
            }
        });

        //toggle checkboxes compared to clicked Parent Checkbox
        if(wasRed) {
            new WWClient(this) {
                @Override
                protected void onPostExecute(List<JsonObject> results) {
                    String strWW = "";
                    String Combo = "";
                    for (JsonObject r : results) {
                        if (r.get("_id").getAsString().equals(Cid))
                            strWW = r.get("W_W").getAsString();
                    }

                    Combo = strWW.substring(0, 2);

                    //compare to checkboxes available
                    for (String choice : strWCBW) {
                        if (Combo.equals(choice)) {
                            //toggle multichoice item that matches

                        }
                    }


                }
            }.execute(Cid);
        }
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean wwADDed = false;
                for (int i = 0; i < WW.size(); i++) {
                    strWW.add(WW.get(i));
                    wwADDed = true;
                }

                if (!wwADDed)
                    c.toggle();
            }
        });

        AlertDialog AW = builder.create();


        AW.setView(ll);
        AW.show();

    }

    /**
     * @param view button clicked
     *             if the next button is clicked it makes you click yes to add it to the database
     */
    public void onClickNext(View view) {

        AlertDialog.Builder b = new AlertDialog.Builder(Inspection.this);
        ListView lv = new ListView(Inspection.this);
        b.setView(lv);

        if (strWW.size() > 0) {
            b.setTitle("Are you sure this is whats wrong with car: " + Cid + "?");

            String ww = "";
            for (String w : strWW) {
                ww += w + "\n Comment(s): " + Coms.get(strWW.indexOf(w)) + " \n";

            }

            b.setMessage(ww);

            b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Inspection.this, TrainList.class);
                    JsonObject j = makeWW(true);

                    //adds with Key: Carid, strWW to the database
                    new WWUpdate(Inspection.this).execute(j);

                    i.putExtra("Details", getIntent().getExtras().getStringArray("Details"));
                    strWW.clear();
                    startActivity(i);
                }
            });
            b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        } else if (!SW_True) {                                                  //for some reason this doesn't work, needs more testing
            Intent i = new Intent(Inspection.this, TrainList.class);           //should switch back to a green button if the defects switch is not turned on and the user hits next car
            i.putExtra("WW", strWW);
            i.putExtra("Details", getIntent().getExtras().getStringArray("Details"));
            Cid = "g" + Cid;
            CharSequence c = Cid;

            new CClient(Inspection.this).execute(c);

            startActivity(i);
        } else {
            b.setTitle("!!! Error !!!");
            b.setMessage("Either Turn off the defects switch or Select Whats Wrong!");

        }

        b.setCancelable(true);

        AlertDialog alertWW = b.create();
        alertWW.show();
    }

    /**
     * Makes the Whats Wrong jsonObject for storage in the Whats-Wrong database
     *
     * @param update boolean for the thread to know whether or not to write to the db or remove the object
     * @return JsonObject with the car id and boolean for either removal or writing.
     */
    public JsonObject makeWW(boolean update) {
        JsonObject j = new JsonObject();
        j.addProperty("_id", Cid);
        j.addProperty("trainid", TrainList.TrainID);

        String ww = "";
        String comments = "";
        for (String w : strWW) {

            ww += w + "|";
            comments += Coms.get(strWW.indexOf(w)) + "|";
        }
        j.addProperty("Comments", comments);
        j.addProperty("W_W", ww);
        j.addProperty("Update", update);


        return j;
    }

    public void onClickPrevious(View view) {
        Intent i = new Intent(Inspection.this, TrainList.class);
        String[] strDetails = new String[3];
        strDetails[0] = " ";
        strDetails[1] = " ";
        strDetails[2] = TrainList.TrainID;

        i.putExtra("Details", strDetails);
        startActivity(i);
    }
    public void toggleCheckboxes(String Cid, CheckBox[] CBs){
        /*WWCClient wwBoxes = new WWCClient(){

        onPostExcecute{
           array list for WW

           wtTog = substring 2 letters before -
           store in array



          for(Checkbox c:CBs){
          String id = c.getId();
          if(id.substring(0,2).equals(wtTog){
            c.toggle();
          }

        }.execute(Cid);



        */
    }

}
