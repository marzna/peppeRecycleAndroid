package com.example.pepperecycle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String  DATABASE_NAME = "users.db";
    public static final String  TABLE_NAME = "users_table";
    public static final String  COL1 = "ID";
    public static final String  COL2 = "USERNAME";
    public static final String  COL3 = "PLAYED_MATCHES"; //n. partite giocate
    public static final String  COL4 = "WON_MATCHES"; //n. partite vinte
    public static final String  COL5 = "PERC_WIN"; //percentuale vittorie
    private static final String TAG = "DatabaseHelper" ;


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME_TEXT, PLAYED_MATCHES, WON_MATCHES, PERC_WIN)";
        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP  TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String username, int playedMatches, int wonMatches, int percWin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, username);
        contentValues.put(COL3, playedMatches);         //TODO incrementare partite giocate
        contentValues.put(COL4, wonMatches);            //TODO incrementare partite vinte
        contentValues.put(COL5, percWin);               //TODO aggiornare percentuale partite vinte

        long result = db.insert(TABLE_NAME, null, contentValues); //ritorna -1 se ci sono eccezioni

        if (result == -1) {
            //vuol dire che ci sono state eccezioni
            return false;
        } else {
            return true;
        }


    }

    /*
    public int getScore( String name){

        String scoreQuery="SELECT IFNULL(MAX(id),0)+1 FROM "+
                TableInfo.TABLE_NAME+ " WHERE appliance="+name;

        SQLiteDatabase SQ=this.getReadableDatabase();
        Cursor CR=SQ.rawQuery(scoreQuery,null);


        int id = -1;

        if (CR != null && CR.getCount() > 0) {
            CR.moveToFirst();
            id = CR.getInt(c.getColumnIndex(COL3));
            CR.close();
        }
        return id;
    }
*/

    // Incrementa il numero di partite giocate
    public void addPlayedMatch(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int nPlayedMatch = getPlayedMatch(userId);
        nPlayedMatch = nPlayedMatch + 1;

        String query = "UPDATE " + TABLE_NAME + " SET " + COL3 +
                " = '" + nPlayedMatch + "' WHERE " + COL1 + " = '" + userId + "'";
        Log.d(TAG, "Query incremento partite giocate: " + query);
        Log.d(TAG, "Tot. partite giocate [aggiornato]: " + nPlayedMatch);

        db.execSQL(query);
    }

    // Per ottenere il numero di partite giocate
    public int getPlayedMatch(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL3 + " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = '" + userId + "'";
        Cursor data = db.rawQuery(query, null);

        int nPlayedMatch = data.getInt(data.getColumnIndexOrThrow(COL3)); //int nPlayedMatch = data.getInt(data.getColumnIndex(COL3)); dà errori , forse è un errore con questa versione di Android Studio

        return nPlayedMatch;
    }
    // Incrementa il numero di partite giocate
    public void addWonMatch(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int nWon = getWonMatch(userId);
        nWon = nWon + 1;

        String query = "UPDATE " + TABLE_NAME + " SET " + COL3 +
                " = '" + nWon + "' WHERE " + COL1 + " = '" + userId + "'";
        Log.d(TAG, "Query incremento partite vinte: " + query);
        Log.d(TAG, "Tot. partite vinte [aggiornato]: " + nWon);

        db.execSQL(query);
    }

    // Per ottenere il numero di partite giocate
    public int getWonMatch(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL3 + " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = '" + userId + "'";
        Cursor data = db.rawQuery(query, null);

        int nWon = data.getInt(data.getColumnIndexOrThrow(COL3)); //int nPlayedMatch = data.getInt(data.getColumnIndex(COL3)); dà errori , forse è un errore con questa versione di Android Studio

        Log.d(TAG, "Query per ottenere num partite vinte: " + query);
        Log.d(TAG, "Tot. partite vinte [non aggiornato]: " + nWon);

        return nWon;
    }

    // Per ottenere il contenuto di tutto il db
    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    // Ottenere l'id partendo dal nome
    public Cursor getUserID(String username){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME +
                " WHERE " + COL2 + " = '" + username + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    // Aggiunge un nuovo utente //TODO inutile in quanto non ha senso aggiungere chi non ha giocato
    public boolean addNewUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, username);
        contentValues.put(COL3, 0);
        contentValues.put(COL4, 0);
        contentValues.put(COL5, 0);

        long result = db.insert(TABLE_NAME, null, contentValues); //ritorna -1 se ci sono eccezioni

        if (result == -1) {
            //vuol dire che ci sono state eccezioni
            return false;
        } else {
            return true;
        }

    }
}

