package com.example.pepperecycle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScoresManager {
    /* Contiene l'id univoco (numero)
    che rappresenta un utente e i valori per quell'id*/
    Map<Integer, ArrayList> users = new HashMap<>();
/*ArrayList userData =


    //per ora, si contano solo 2 giocatori: pepper e l'utente
    public ScoresManager( Map<Integer, ArrayList> userData) {
        this.userData = userData;
        scores.put("pepperScore", (byte) 0);
        scores.put("userScore0", (byte) 0);
    }*/
   /* Posizione
            username
    partite giocate
    partite vinte
    percentuale vittorie*/

    String fileName = "scores.bin";
    public void writeScoresFile(String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeInt(2048); //per scrivere
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readScoresFile(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream is = new ObjectInputStream(fis);



            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
/*
    SharedPreferences preferences = getSharedPreferences("PREFS:", 0);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putInt("lastScore", score);
    editor.apply();*/

    /*
    Map<String, Byte> scores = new HashMap<>();

    //per ora, si contano solo 2 giocatori: pepper e l'utente
    public ScoresManager(Map<String, Byte> scores) {
        this.scores = scores;
        scores.put("pepperScore", (byte) 0);
        scores.put("userScore0", (byte) 0);
    }

    public void updateScore(boolean isPepperTurn) {
        if(isPepperTurn) {
            scores.put("pepperScore", (byte) (scores.get("pepperScore") + 1)); //Incrementa il punteggio di Pepper
        } else {
            scores.put("userScore0", (byte) (scores.get("userScore0") + 1)); //Incrementa il punteggio di Pepper
        }
    }

*//*    private List<Items> itemsList = new ArrayList<Items>();
    private ListView listView;
    private CustomListAdapter adapter;*//*
//TODO https://stackoverflow.com/questions/34518421/adding-a-scoreboard-to-an-android-studio-application

    public void saveScores(Activity activity) {
        SQLiteDatabase leaderboard = null;

        try {
            // Creazione database (oppure, se esiste, lo apre)
            leaderboard = activity.openOrCreateDatabase("leaderboard", activity.getApplicationContext().MODE_PRIVATE, null);

            // Creazione tabella (se non esiste già)
            leaderboard.execSQL("CREATE TABLE IF NOT EXISTS scores (name TEXT, score TEXT);");

            // Selezione tutte righe dalla tabella
            Cursor cursor = leaderboard.rawQuery("SELECT * FROM scores", null);

            // Se non ci sono dati, ne inserisce alcuni
            if (cursor != null) {
                if (cursor.getCount() == 0) {

                    leaderboard.execSQL("INSERT INTO scores (name, score) VALUES ('Maria', '5');");
                    leaderboard.execSQL("INSERT INTO scores (name, score) VALUES ('Andrea', '4');");
                    leaderboard.execSQL("INSERT INTO scores (name, score) VALUES ('Lucia', '1');");
                    leaderboard.execSQL("INSERT INTO scores (name, score) VALUES ('Matteo', '1');");
                }
            }
        } catch (Exception e) {

        } finally {
            // Inizializza e crea un nuovo  ? con la list TODO EH?
            //Initialize and create a new adapter with layout named list found in activity_main layout
            *//*listView = (ListView) findViewById(R.id.list);
            adapter = new CustomListAdapter(this, itemsList);
            listView.setAdapter(adapter);*//*

            Cursor cursor = leaderboard.rawQuery("SELECT * FROM scores", null);

            if (cursor.moveToFirst()) {

                //read all rows from the database and add to the Items array

                while (!cursor.isAfterLast()) {

                    Items items = new Items();

                    items.setName(cursor.getString(0));
                    items.setScore(cursor.getString(1));

                    itemsList.add(items);
                    cursor.moveToNext();


                }
            }


            //All done, so notify the adapter to populate the list using the Items Array

            adapter.notifyDataSetChanged();
        }

    }

}*/
