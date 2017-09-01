/**
 * Joe Lieberman
 * 8/1/2017
 * Penn State University
 */

package com.lieb.j.traininspection;


import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.IndexField;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * @Cid the Car ID number
 * @SW_True boolean which describes if something is wrong.
 * The class inspection assigns UI for whats wrong to each of the cars in the database whats-wrong
 */
public class Inspection extends AppCompatActivity {
    String Cid = "";
    boolean SW_True;

    public static ArrayList<String> strWW = new ArrayList<>();
    @Override
    /**
     * Instantiates all the checkboxes for what could be wrong and gets the car id for the car that is being inspected
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        final String str_id = getIntent().getExtras().getString("CarID");
        Cid = str_id;

        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        TextView specs = (TextView)findViewById(R.id.txtDetails);
        specs.setText(str_id + "   |   " + (m+1) + "/" + d);

        final GridLayout ww = (GridLayout)findViewById(R.id.gridWW);

        Switch sw = (Switch)findViewById(R.id.swtchWrong);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    ww.setVisibility(View.VISIBLE);
                    SW_True = true;
                }else {
                    ww.setVisibility(View.INVISIBLE);
                    SW_True = false;
                }
            }
        });

        CheckBox app = (CheckBox)findViewById(R.id.App);
        CheckBox brakes = (CheckBox)findViewById(R.id.Brakes);
        CheckBox coupler = (CheckBox)findViewById(R.id.Coupler);
        CheckBox DG = (CheckBox)findViewById(R.id.DG);
        CheckBox ERB = (CheckBox)findViewById(R.id.ERB);
        CheckBox frame = (CheckBox)findViewById(R.id.Frame);
        CheckBox truck = (CheckBox)findViewById(R.id.KTruck);
        CheckBox other = (CheckBox)findViewById(R.id.Other);

        //sets all the listeners for the cars
        CheckBox[] checks = {app, brakes,coupler, DG, ERB, frame, truck, other};
        for(CheckBox ch:checks){
            SetListener(ch);
        }
        findCarSpecs(str_id);
    }

    /**
     *
     * @param str_id is the car id
     * findCarSpecs autofills the inspection page with reporting mark and type information
     */
    public void findCarSpecs(final String str_id){
        new CClient(this){
            @Override
            protected List<JsonObject> doInBackground(CharSequence... Cars) {

                    CloudantClient client = ClientBuilder.account("68210c1a-d572-410c-a691-0e05d6aa78ad-bluemix")
                            .username("68210c1a-d572-410c-a691-0e05d6aa78ad-bluemix")
                            .password("0a7515ddc83c641eb087259025e244e5f3b3d80e7fe8ff2dd871940cbf028993")
                            .build();

                    String dbname = "train-inspection";

                    Database dbCars = client.database(dbname, true);
                    dbCars.createIndex("trainid", "trainid", "json", new IndexField[]{
                            new IndexField("trainid", IndexField.SortOrder.asc),
                            new IndexField("loc", IndexField.SortOrder.asc),
                            new IndexField("_id", IndexField.SortOrder.asc),
                            new IndexField("reportingmark", IndexField.SortOrder.asc),
                            new IndexField("S_W", IndexField.SortOrder.asc),
                            new IndexField("type", IndexField.SortOrder.asc)
                    });

                    JsonObject j = (JsonObject)(dbCars.find(JsonObject.class, str_id));

                    j.addProperty("reportingmark", j.get("reportingmark").toString());
                    j.addProperty("type", j.get("type").toString());
                    j.addProperty("S_W", j.get("S_W").getAsBoolean());

                    List<JsonObject> L_j = new ArrayList<JsonObject>();
                    L_j.add(j);



                    return L_j;

            }
            @Override
            /**
             * adds the car information to the inspection UI
             */
            protected void onPostExecute(List<JsonObject> results){
                JsonObject j = (JsonObject) results.get(0);
                String rm = j.get("reportingmark").getAsString();
                String type = j.get("type").getAsString();

                rm = rm.substring(1,rm.length()-1);
                type = type.substring(1,type.length()-1);

                //the text areas should match these results
                EditText txt_rm = (EditText)findViewById(R.id.txtRM);
                EditText txt_Type = (EditText)findViewById(R.id.txtType);

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
     *
     * @param c the checkbox clicked
     * sets checked change listeners for each checkbox.
     */
    public void SetListener(final CheckBox c){
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checked(c);

                } else if (!isChecked) {
                    unchecked(c);
                }
            }
        });
    }

    /**
     *
     * @param c the checkbox checked
     * to make sure that if a checkbox is unchecked it will remove that detail from the list of Whats wrong.
     */
    public void unchecked(CheckBox c){
        String firstLetter = c.getText().toString().substring(0,1);

        List<String> toRemove = new ArrayList<>();

        for(String w : strWW){
            if(w.substring(0,1).equals(firstLetter)){
                toRemove.add(w);
            }else if(firstLetter.substring(0,1).equals("O") && (w.substring(0,1).equals("L") ||
                    w.substring(0,1).equals("R") || w.substring(0,1).equals("T"))){

                toRemove.add(w);
            }
        }
        strWW.removeAll(toRemove);

    }

    /**
     *
     * @param c all the possible outputs for WCBW
     * populates the WCBW with the proper choices
     */
    public void checked(CheckBox c){

        ArrayList<String> WCBW = new ArrayList<>();
        String[] ALapp = {"AL - Ladder","AS - Sill Step","AH - Hand Hold","AR - Hand Rail/Cable", "AB - Running Board", "AC - Crossover and Step"};
        String[] ALbrakes = {"BF - Cut Out","BC - Cylinder","BR - Reservoir","BV - Valves","BP - Pipe, Fittings, Brkts, Angle Cock, Trainline",
                            "BB - B.Beam/Hanger/Support","BL - Brake Rod & Lever", "BT - Piston Travel/Not Set","BH - Hand Brake",
                            "BW - Hand Brake Wheel","BA - Slack Adjuster"};
        String[] ALcoupler ={"CB - Body","CC - Components(head)","CD - Center Device","CP - Vertical Pin","CL - Operating Lever"};
        String[] ALdg ={"DY - Yoke","DG - Gear","DD - Cushion Device","DR - Return Spring","DC - Carrier Iron","DS - Striker"};
        String[] ALerb ={"EE - End","ES - Side","EH - Hopper Slope Sheet","ED - Door & Gate","EA - Top Chord Angle","ER - Roof",
                        "EC - Hatch Dome Cover","EP - Stake Pocket","EM - Metal Stake & Post"};
        String[] ALframe={"FB - Bolster Body","FP - Center Plate","FO - Off Center","FF - Floor Bearer","FC - Center Sill","FS - Side Sill","FE - End Sill"};
        String[] ALktruck={"KH - Hot Box","KR - Roller Bearing","KA - RB Adapter","KO - Wheel Out of Round","KW - Wheel Set","KB - Bolster",
                          "KF - Side Frame","KS - Spring & Snubber", "KD - Side Bring Defect","KC - Side Bring Clearance"};
        String[] ALother={"LB - Bulkhead Door","RR - Refrigeration","TA - Load Adjustment/Securement"};

        if(c.getId() == R.id.App){
            WCBW.addAll(addWCBW(ALapp));
        }
        else if(c.getId() == R.id.Brakes){
            WCBW.addAll(addWCBW(ALbrakes));
        }
        else if(c.getId() == R.id.Coupler){
            WCBW.addAll(addWCBW(ALcoupler));
        }
        else if(c.getId() == R.id.DG){
            WCBW.addAll(addWCBW(ALdg));
        }
        else if(c.getId() == R.id.ERB){
            WCBW.addAll(addWCBW(ALerb));
        }
        else if(c.getId() == R.id.Frame){
            WCBW.addAll(addWCBW(ALframe));
        }
        else if(c.getId() == R.id.KTruck){
            WCBW.addAll(addWCBW(ALktruck));
        }
        else if(c.getId() == R.id.Other){
            WCBW.addAll(addWCBW(ALother));
        }

        populate(WCBW);
    }

    public ArrayList<String> addWCBW(String[] a){
        ArrayList<String> e = new ArrayList<>();
        for(String i: a){
            e.add(i);
        }
        return e;
    }

    /**
     *
     * @param WCBW a list of strings that are options for what could be wrong with a car
     * makes an alert dialog with multichoice checkboxes that add to an arraylist which is what actually is wrong with a car
     */
    public void populate(final ArrayList<String> WCBW){

        final ArrayList<String> WW = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        boolean[] is_checked = new boolean[WCBW.size()];
        ListView lv = new ListView(this);


        final String[] strWCBW = new String[WCBW.size()];
        for(int i =0; i<WCBW.size(); i++){
            strWCBW[i]=WCBW.get(i);
        }


        builder.setTitle("Select What is Wrong:");
        builder.setView(lv);
        builder.setMultiChoiceItems(strWCBW, is_checked, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked){
                if(isChecked){
                    WW.add(strWCBW[which]);
                }
                else if(!isChecked){
                    WW.remove(strWCBW[which]);
                }
            }
        });
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(int i= 0; i<WW.size(); i++) {
                    strWW.add(WW.get(i));
                }
            }});

        AlertDialog AW = builder.create();
        AW.show();

    }

    /**
     *
     * @param view button clicked
     * if the next button is clicked it makes you click yes to add it to the database
     */
    public void onClickNext(View view){

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        ListView lv = new ListView(this);
        b.setView(lv);

        if(strWW.size()>0) {
            b.setTitle("Are you sure this is whats wrong with car: " + Cid + "?");

            String ww = "";
            for (String w : strWW) {
                ww += w + " \n";
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

        }else if(!SW_True) {                                                  //for some reason this doesn't work, needs more testing
            Intent i = new Intent(Inspection.this, TrainList.class);           //should switch back to a green button if the defects switch is not turned on and the user hits next car
            i.putExtra("WW", strWW);
            i.putExtra("Details", getIntent().getExtras().getStringArray("Details"));
            Cid = "g" + Cid;
            CharSequence c = Cid;

            new CClient(this).execute(c);

            startActivity(i);
        }else{
            b.setTitle("!!! Error !!!");
            b.setMessage("Either Turn off the defects switch or Select Whats Wrong!");

        }

        b.setCancelable(true);

        AlertDialog alertWW = b.create();
        alertWW.show();
    }

    /**
     * Makes the Whats Wrong jsonObject for storage in the Whats-Wrong database
     * @param update boolean for the thread to know whether or not to write to the db or remove the object
     * @return JsonObject with the car id and boolean for either removal or writing.
     */
    public JsonObject makeWW (boolean update){
        JsonObject j = new JsonObject();
        j.addProperty("_id", Cid);
        j.addProperty("trainid", TrainList.TrainID);

        String ww = "";
        for(String w: strWW){

            ww += w + "  ";

        }

        j.addProperty("W_W", ww);
        j.addProperty("Update", update);


        return j;
    }

    public void onClickPrevious(View view){
        Intent i = new Intent(this, PreInspection.class);
        startActivity(i);
    }

}
