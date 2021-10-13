package com.example.pepperecycle;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;

public class PlayUserTurnActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {//, CameraBridgeViewBase.CvCameraViewListener2{
    byte round;
    boolean isPepperTurn = false;
    byte wasteType=-1; //0=Organico, 1=Carta/Cartone, 2=Plastica/Metalli, 3=Vetro
    boolean binPressed=false;
    //TODO sposta le costanti in una posizione adeguata
    static final byte TYPE_ORGANIC = 0;
    static final byte TYPE_PAPER_CARDBOARD = 1;
    static final byte TYPE_PLASTIC_METAL = 2;
    static final byte TYPE_GLASS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_play_user_turn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            round = extras.getByte("round");
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }

   /* void startUserTurn() {
        if(binPressed) {
            Intent activity2Intent = new Intent(PlayUserTurnActivity.this, JudgeConfirmActivity.class);
            activity2Intent.putExtra("wasteType", wasteType);
            activity2Intent.putExtra("round", round);
            activity2Intent.putExtra("isPepperTurn", isPepperTurn);
            startActivity(activity2Intent);
            finish();
        }
*//*
        Intent activity2Intent = new Intent(PlayGameActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);*//*
     *//*        Intent activity2Intent = new Intent(getApplicationContext(), JudgeConfirmActivity.class);
        startActivity(activity2Intent);
        finish();*//*
     *//* TODO Turno dell'utente:
        Schermata con i bidoni. L'utente deve selezionare il bidone corretto.
        Successivamente, c'è la schermata di richiesta conferma per il giudice
        Se la risposta è affermativa, l'utente guadagna un punto.
        *//*
    }*/

    void askForConfirm() {
        Intent activity2Intent = new Intent(PlayUserTurnActivity.this, JudgeConfirmActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        startActivity(activity2Intent);
        finish();
        /* TODO Turno dell'utente:
        Schermata con i bidoni. L'utente deve selezionare il bidone corretto.
        Successivamente, c'è la schermata di richiesta conferma per il giudice
        Se la risposta è affermativa, l'utente guadagna un punto.
        */
    }

    public void selectBinBrown(View v) {    // 0 = Organico
        wasteType = TYPE_ORGANIC;
        // binPressed=true;
        askForConfirm();

    }
    public void selectBinBlue(View v) {     // 1 = Carta / Cartone
        wasteType = TYPE_PAPER_CARDBOARD;
        // binPressed=true;
        askForConfirm();
    }
    public void selectBinYellow(View v) {   // 2 = Plastica / Metalli
        wasteType = TYPE_PLASTIC_METAL;
        // binPressed=true;
        askForConfirm();
    }
    public void selectBinGreen(View v) {    // 3 = Vetro
        wasteType = TYPE_GLASS;
        // binPressed=true;
        askForConfirm();
    }
    public void buttonHelp(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        //TODO
    }
    public void buttonHome(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }
    public void buttonClose(View v) { //Pressione tasto "Chiudi" TODO Togli perché è un duplicato? [???]
        finish();
        /* Intent activity2Intent = new Intent(getApplicationContext(), TodoActivity.class);
        startActivity(activity2Intent);
        //TODO Chiudi gioco
           */
    }
}