/**
 * Joe Lieberman
 * 8/1/2017
 * Penn State University
 */

package com.lieb.j.traininspection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.pdf.PrintedPdfDocument;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Document;
import com.cloudant.client.api.model.FindByIndexOptions;
import com.cloudant.client.api.model.IndexField;
import com.cloudant.client.org.lightcouch.DocumentConflictException;
import com.google.gson.JsonObject;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import static com.ibm.mobilefirstplatform.clientsdk.android.push.internal.MFPInternalPushMessage.LOG_TAG;

/**
 * Prints whats wrong in a list form
 */
public class WWList extends AppCompatActivity {
    static CharSequence Trainid = "d";
    /**
     * gets details extra and prints cars with issues
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wwlist);
        final String[] tid = getIntent().getExtras().getStringArray("Details");

        if (tid.length > 4) {
            Button btnStartOver = (Button) findViewById(R.id.btnNext);
            btnStartOver.setText("Next Train");
            btnStartOver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(WWList.this, PreInspection.class);
                    i.putExtra("Username", tid[3]);
                    startActivity(i);
                }
            });

        } else {
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

        try {
            if (tid[tid.length - 1].equals("f")) {
                new CClient(this) {
                    @Override
                    protected void onPostExecute(final List<JsonObject> result) {
                        new WWClient(WWList.this) {
                            @Override
                            protected void onPostExecute(final List<JsonObject> WWresult) {
                                try {
                                    finalReport(Trainid, tid, result, WWresult);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.execute(tid);

                    }
                }.execute(tid);
            }
            else
                populateList(Trainid);
        } catch (Exception e) {
            //needs to catch error
        }
    }

    /**
     * prints cars that are in the whats wrong database
     *
     * @param tid train id charsequence
     */
    @SuppressLint("StaticFieldLeak")
    public void populateList(CharSequence tid) {
        new WWClient(this) {
            @Override
            protected void onPostExecute(List<JsonObject> results) {
                LinearLayout lv = (LinearLayout) findViewById(R.id.llWrong);

                TextView lblWW = (TextView) findViewById(R.id.lblwwList);
                lblWW.setText("Cars with issues:");
                lblWW.setTextSize(lv.getWidth() / 20);

                if (results != null) {
                    for (JsonObject r : results) { //for each Jsonobject in the query results
                        String obj = r.get("W_W").toString();
                        String coms = r.get("Comments").toString();
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
                        String issues = "Issues: \n";
                        int indexWW = obj.indexOf("|");
                        int indexCom = coms.indexOf("|");
                        int startWW = 0, startCom = 1;

                        do {
                            issues += obj.substring(startWW, indexWW) + "\nComment(s): ";
                            issues += coms.substring(startCom, indexCom) + "\n";

                            startCom = indexCom + 1;
                            startWW = indexWW + 1;
                            indexWW = obj.indexOf("|", indexWW + 1);
                            indexCom = coms.indexOf("|", indexCom + 1);
                        } while (indexWW < obj.length() && indexWW != -1);


                        txtOb.setText(car + issues + "\n_________________________________________\n");
                        txtOb.setTextSize(30);
                        lv.addView(txtOb);
                    }
                    ProgressBar pg = (ProgressBar) findViewById(R.id.progressBar);
                    pg.setVisibility(View.INVISIBLE);


                }
            }
        }.execute(tid);

    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    @SuppressLint("StaticFieldLeak")
    public boolean finalReport(final CharSequence tid, final String[] details, final List<JsonObject> BOL, final List<JsonObject> BOD) throws FileNotFoundException, IOException, DocumentException {

        try {
            verifyStoragePermissions(this);

            File folder = new File(Environment.getExternalStorageDirectory() + "/Reports");
            Font font = new Font(Font.FontFamily.HELVETICA);
            font.setSize(30);
            font.setStyle(Font.BOLD);
            Date d = new Date();
            DateFormat t = new SimpleDateFormat("HH:mm:ss");


            if (!folder.exists()) {
                folder.mkdirs();
                folder.createNewFile();
                Log.e(LOG_TAG, "Folder was created");
            }

            File pdf = new File(folder.getAbsolutePath(), "FinalReport" + t.format(d) + ".pdf");
            if(!pdf.exists()){
                pdf.createNewFile();
                Log.e(LOG_TAG, "PDF was created");
            }
            OutputStream output = new FileOutputStream(pdf);
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document();

            PdfWriter.getInstance(doc, output);
            doc.open();

            Phrase p = new Phrase("\bAIR(1)F", font);
            Paragraph title = new Paragraph(p);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            font.setSize(10);
            font.setStyle(Font.NORMAL);
            font.setStyle(Font.ITALIC);
            p = new Phrase("Automated Inspection Reporting for Freight Cars",font);
            title = new Paragraph(p);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            font.setSize(20);
            font.setStyle(Font.NORMAL);
            font.setStyle(Font.BOLD);
            Phrase j = new Phrase("\bGeneral Info\n", font);
            doc.add(j);

            int BoSize = 0;
            for(JsonObject r: BOL){
                if(r.get(("S_W")).getAsBoolean() == true){
                   BoSize++;
                }
            }

            doc.add(createGenInfo(details, BoSize));

            j = new Phrase("\bBad Order Locations\n", font);

            doc.add(j);
            doc.add(createBOL(BOL));

            j = new Phrase("\bBad Order Details\n", font);

            doc.add(j);
            doc.add(createBOD(BOD, BOL));

            j = new Phrase("\bPictures (optional)\n", font);

            doc.add(j);

            ProgressBar pg = (ProgressBar) findViewById(R.id.progressBar);
            pg.setVisibility(View.INVISIBLE);

            doc.close();
            previewDoc(pdf);

            return true;
        } catch (DocumentException de){
            de.printStackTrace();
        }
        return false;


    }
    public PdfPTable createBOD(List<JsonObject> BOD, List<JsonObject> BOL) throws DocumentException{
        PdfPTable tBOD = new PdfPTable(6);
        tBOD.setWidths(new int[]{1,2,2,2,4,4});
        tBOD.setPaddingTop(20);
        tBOD.addCell(new Phrase("No."));
        tBOD.addCell(new Phrase("Car No."));
        tBOD.addCell(new Phrase("Car Type"));
        tBOD.addCell(new Phrase("Location"));
        tBOD.addCell(new Phrase("Defect Type"));
        tBOD.addCell(new Phrase("Comments"));

        List<JsonObject> nBOL = new ArrayList<>();

        for(JsonObject c: BOL){
            for(JsonObject j: BOD){
                if(j.get("_id").getAsString().equals(c.get("_id").getAsString())){
                    nBOL.add(c);
                }
            }
        }
        for(int i= 1; i<=BOD.size(); i++){
            JsonObject j = BOD.get(i-1);
            JsonObject k = nBOL.get(i-1);

            tBOD.addCell(new Phrase("" + i));
            tBOD.addCell(new Phrase( j.get("_id").getAsString()));
            //tBOD.addCell(new Phrase(k.get("type").getAsString()));
            tBOD.addCell(new Phrase(k.get("type").getAsString()));
            tBOD.addCell(new Phrase(k.get("loc").getAsString()));
            tBOD.addCell(new Phrase(j.get("W_W").getAsString().replace("|","\n")));

            tBOD.addCell(new Phrase(j.get("Comments").getAsString().replace("|", "\n")));

        }



        return tBOD;
    }
    public PdfPTable createBOL(List<JsonObject> BOL){
        PdfPTable tBO = new PdfPTable(BOL.size());
        String CarID = "";
        tBO.setWidthPercentage(50);
        tBO.setPaddingTop(20);

        Font font = new Font(Font.FontFamily.TIMES_ROMAN);
        font.setColor(BaseColor.GREEN);

        Font font2 = new Font(Font.FontFamily.TIMES_ROMAN);
        font2.setColor(BaseColor.RED);
        Paragraph p;
        PdfPCell[] cars = new PdfPCell[BOL.size()];

        for(JsonObject c: BOL){

            cars[BOL.indexOf(c)] = new PdfPCell();

            if(c.get("S_W").getAsBoolean()==false){
                CarID = c.get("_id").toString();
                CarID = CarID.substring(1, CarID.length() - 1);
                cars[BOL.indexOf(c)].addElement(new Phrase(CarID, font));

                Log.e(LOG_TAG, cars[BOL.indexOf(c)].getRotation() + "");

            }
            else{
                CarID = c.get("_id").toString();
                CarID = CarID.substring(1, CarID.length() - 1);
                cars[BOL.indexOf(c)].addElement(new Phrase(CarID, font2));

                Log.e(LOG_TAG, cars[BOL.indexOf(c)].getRotation() + "");

            }

            cars[BOL.indexOf(c)].setRotation(90);
            tBO.addCell(cars[BOL.indexOf(c)]);

        }

        return tBO;
    }
    public void previewDoc(File pdf){

        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(pdf);
            intent.setDataAndType(uri, "application/pdf");

            startActivity(intent);
        }else{
            Toast.makeText(this,"Final Report PDF saved",Toast.LENGTH_LONG).show();
        }
    }
    public static PdfPTable createGenInfo(String[] info, int NoBos) throws DocumentException {
        PdfPTable gi = new PdfPTable(2);
        gi.setTotalWidth(400);
        gi.setPaddingTop(50);
        gi.setLockedWidth(true);
        gi.setWidths(new float[]{3, 8});
        Date date = new Date();
        DateFormat t = new SimpleDateFormat("HH:mm:ss");
        DateFormat df = new SimpleDateFormat("MM-dd-yy");

        gi.addCell("Inspector Name");
        gi.addCell(info[3]);
        gi.addCell("Date");
        gi.addCell(df.format(date));
        gi.addCell("Time (completed)");
        gi.addCell(t.format(date));
        gi.addCell("Company");
        gi.addCell(info[1]);
        gi.addCell("No. of Bad Orders");
        gi.addCell(NoBos+"");
        gi.addCell("Report No.");
        gi.addCell("PSU_"+info[2]+"_"+df.format(date)+"_1");

        return gi;
    }
}
class WWClient extends AsyncTask<CharSequence, Void, List<JsonObject>> {
    public Context context;
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


                Database dbCars = client.database(dbname, false); //database client
                /**dbCars.createIndex("trainid", "trainid", "json", new IndexField[]{ //queriable index
                        new IndexField("trainid", IndexField.SortOrder.asc),
                        new IndexField("W_W", IndexField.SortOrder.asc),
                        new IndexField("_id", IndexField.SortOrder.asc),
                        new IndexField("Comments", IndexField.SortOrder.asc)
                });**/

                List<JsonObject> cars = dbCars.findByIndex("\"selector\": {" +
                                "\"trainid\": \"" + tid + "\" }", JsonObject.class,
                        new FindByIndexOptions().fields("_id").fields("W_W").fields("Comments"));

                return cars; //query results

            } catch (Exception e) {
                Log.e("Error", e.toString());
                return null;
            }
        }


}
