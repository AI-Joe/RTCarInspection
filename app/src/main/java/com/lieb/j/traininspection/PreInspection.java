package com.lieb.j.traininspection;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

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

        String[] strDetails = new String[3];

        Intent n = new Intent(PreInspection.this, WWList.class);

        EditText Loc = (EditText)findViewById(R.id.txtLocation);
        EditText comp = (EditText)findViewById(R.id.txtCompany);
        EditText tid = (EditText)findViewById(R.id.txtTrain);

        if(!(Loc.getText().toString()=="")&& !(comp.getText().toString()=="") && !(tid.getText().toString()=="")){
            strDetails[0]= Loc.getText().toString();
            strDetails[1]= comp.getText().toString();
            strDetails[2]= tid.getText().toString();

            n.putExtra("Details",strDetails);
            startActivity(n);
       }
        else{
            TextView error = (TextView) findViewById(R.id.txtErrorbox);
            error.setVisibility(View.VISIBLE);

        }

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
