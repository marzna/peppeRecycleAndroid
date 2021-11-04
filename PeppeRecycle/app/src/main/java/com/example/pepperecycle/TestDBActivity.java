package com.example.pepperecycle;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TestDBActivity extends AppCompatActivity {
    DatabaseHelper peopleDB;

    Button btnAddData, btnViewData,btnUpdateData,btnDelete;
    EditText etName, etPlayedMatches, etWonMatches, etPercWonMatches, etID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        peopleDB = new DatabaseHelper(this);

        etID = (EditText) findViewById(R.id.etID);

        etName = (EditText) findViewById(R.id.etNewName);
        etPlayedMatches = (EditText) findViewById(R.id.etNPlayedMatches);
        etWonMatches = (EditText) findViewById(R.id.etNWonMatches);
        etPercWonMatches = (EditText) findViewById(R.id.etPercWonMatches);

        btnAddData = (Button) findViewById(R.id.btnAddData);
        btnViewData = (Button) findViewById(R.id.btnViewData);
        btnUpdateData = (Button) findViewById(R.id.btnUpdateData);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        AddData();
        ViewData();
        UpdateData();
        DeleteData();*/
    }
    /*

    public void AddData() {
        btnAddData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = etName.getText().toString();
                String nPlayedMatches = etPlayedMatches.getText().toString();
                String nWonMatches = etWonMatches.getText().toString();
                String percWonMatches = etPercWonMatches.getText().toString();
                
                boolean insertData = peopleDB.addData(name, email, tvShow);

                if (insertData == true) {
                    Toast.makeText(TestDBActivity.this, "Data Successfully Inserted!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TestDBActivity.this, "Something went wrong :(.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void ViewData(){
        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cursor data = peopleDB.showData();

                if (data.getCount() == 0) {
                    display("Error", "No Data Found.");
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while (data.moveToNext()) {
                    buffer.append("ID: " + data.getString(0) + "\n");
                    buffer.append("Name: " + data.getString(1) + "\n");
                    buffer.append("Email: " + data.getString(2) + "\n");
                    buffer.append("Favorite TV Show: " + data.getString(3) + "\n");

                    display("All Stored Data:", buffer.toString());
                }
            }
        });
    }

    public void display(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void UpdateData(){
        btnUpdateData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = etID.getText().toString().length();
                if (temp > 0) {
                    boolean update = peopleDB.updateData(etID.getText().toString(), etName.getText().toString(),
                            etEmail.getText().toString(), etTVShow.getText().toString());
                    if (update == true) {
                        Toast.makeText(MainActivity.this, "Successfully Updated Data!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Something Went Wrong :(.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "You Must Enter An ID to Update :(.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void DeleteData(){
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = etID.getText().toString().length();
                if(temp > 0){
                    Integer deleteRow = peopleDB.deleteData(etID.getText().toString());
                    if(deleteRow > 0){
                        Toast.makeText(MainActivity.this, "Successfully Deleted The Data!", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(MainActivity.this, "Something went wrong :(.", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "You Must Enter An ID to Delete :(.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }*/
}