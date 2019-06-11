package com.lieb.j.traininspection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.net.InetAddress;

/**
 * sends the user entered company, location and trainid to WWList.class
 */
public class PreInspection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_inspection);


        TextView error = (TextView) findViewById(R.id.txtErrorbox);
        error.setVisibility(View.INVISIBLE);
    }

    /**
     * gets what the users enter in the Edit texts and makes it an extra for the WWlist activity
     * @param v the view btnNext
     */
    public void onClickNext(View v){

        String[] strDetails = new String[4];

        Intent n = new Intent(PreInspection.this, WWList.class);

        EditText Loc = (EditText)findViewById(R.id.txtLocation);
        EditText comp = (EditText)findViewById(R.id.txtCompany);
        EditText tid = (EditText)findViewById(R.id.txtTrain);

        if(!(Loc.getText().toString()=="")&& !(comp.getText().toString()=="") && !(tid.getText().toString()=="") && isNetworkAvailable(this)==true){
            strDetails[0]= Loc.getText().toString();
            strDetails[1]= comp.getText().toString();
            strDetails[2]= tid.getText().toString();
            strDetails[3]= getIntent().getExtras().getString("Username");

            n.putExtra("Details",strDetails);

            startActivity(n);
       }
        else if(isNetworkAvailable(this)==false) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            ListView lv = new ListView(this);
            b.setView(lv);

            TextView alert = new TextView(this);

            b.setMessage("There is an Issue with your network connection!");
            b.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                }
            });

            AlertDialog a = b.create();
            a.show();
        }
        else{
            TextView error = (TextView) findViewById(R.id.txtErrorbox);
            error.setVisibility(View.VISIBLE);

        }

    }
        public static boolean isNetworkAvailable(Context context) {
            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            return  isConnected;
        }

    /**
     * Exits back to the login screen
     * @param v btnQuit
     */
    public void onClickQuit(View v){
        Intent q = new Intent(PreInspection.this, Login.class);
        startActivity(q);


    }
}
