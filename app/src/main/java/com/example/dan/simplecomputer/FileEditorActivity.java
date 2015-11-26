package com.example.dan.simplecomputer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * FileEditorActivity allows user to specify file path to save data in
 */
public class FileEditorActivity extends Activity
{
    String dataString;
    String[] dataStringSplit;

    TextView dataStringBox;
    EditText filePathbox;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fileeditor_layout);

        //Assign Our TextViews
        this.dataStringBox = (TextView) findViewById(R.id.StringsAppendList);
        this.filePathbox = (EditText) findViewById(R.id.fileName);

        //Grab a box full of goodies
        Bundle activityBundle = getIntent().getExtras();

        //Grab String from bundle
        dataString = activityBundle.getString("dataString");


        String temp = "";
        //Split the data and now reformat it with new lines for text box
        if (dataString != null) {
            dataStringSplit = dataString.split("-");

            for (int i = 0; i < dataStringSplit.length; i++) {
                temp = temp + String.format("[%02d] %s\n", i, dataStringSplit[i]);
            }
        }

        //Set text box to formatted data
        dataStringBox.setText(temp);

        //Our Flags from bundle will dictate button layout
        Button savebutton = (Button) findViewById(R.id.button_savefile_execute);
        Button loadbutton = (Button) findViewById(R.id.button_loadfile_execute);

        if (activityBundle.getBoolean("save")) {
            loadbutton.setVisibility(View.INVISIBLE);
        }
        else {
            savebutton.setVisibility(View.INVISIBLE);
        }

    }

    //Advanced URI Parse code for file read and write
    public void onClick(View view)
    {
        switch (view.getId()) {

            case R.id.button_savefile_execute:
                String data = getIntent().getStringExtra("dataString");

                try {
                    FileOutputStream fileOutputStream = openFileOutput(filePathbox.getText().toString(), MODE_PRIVATE);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                    outputStreamWriter.write(data);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();

                    Intent intent = new Intent();
                    intent.setData(Uri.parse(filePathbox.getText().toString()));
                    setResult(RESULT_OK, intent);
                }
                catch (FileNotFoundException e) {
                    popToast("FileNotFoundException Save " + e.getMessage());

                    Intent intent = new Intent();
                    intent.setData(Uri.parse("NULL"));
                    setResult(RESULT_FIRST_USER, intent);
                }
                catch (IOException e) {
                    popToast("IOException Save " + e.getMessage());

                    Intent intent = new Intent();
                    intent.setData(Uri.parse("NULL"));
                    setResult(RESULT_FIRST_USER, intent);
                }
                break;
            case R.id.button_loadfile_execute:

                try {
                    FileInputStream fileInputStream = openFileInput(filePathbox.getText().toString());
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String thisLine = "";


                    while ((thisLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(thisLine);
                    }

                    inputStreamReader.close();
                    bufferedReader.close();

                    Intent intent = new Intent();
                    intent.setData(Uri.parse(stringBuilder.toString()));
                    setResult(RESULT_OK, intent);

                }
                catch (FileNotFoundException e) {
                    popToast("FileNotFoundException Load " + e.getMessage());

                    Intent intent = new Intent();
                    intent.setData(Uri.parse("NULL"));
                    setResult(RESULT_FIRST_USER, intent);
                }
                catch (IOException e) {
                    popToast("IOException Load " + e.getMessage());

                    Intent intent = new Intent();
                    intent.setData(Uri.parse("NULL"));
                    setResult(RESULT_FIRST_USER, intent);
                }
                break;

            default:
                break;
        }//Switch

        finish(); //Finish the activity
    }//OnClick

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.setData(Uri.parse("Cancelled Action"));
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void popToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }//popToast
}
