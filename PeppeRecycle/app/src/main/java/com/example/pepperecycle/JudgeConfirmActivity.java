package com.example.pepperecycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;

import java.util.HashMap;

public class JudgeConfirmActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {//, CameraBridgeViewBase.CvCameraViewListener2{

    boolean isPepperTurn, isAnswerCorrect, pressed;
    byte wasteType, round;
    TextView selectedBinIs;
    ImageView selectedBin;
    HashMap<String, String> scores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_judge_confirm);

        selectedBinIs = findViewById(R.id.textViewSelectedBinIs);
        selectedBin = findViewById(R.id.selectedBin);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            wasteType = extras.getByte("wasteType"); // The key argument here must match that used in the other activity
            round = extras.getByte("round");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            //scores = (HashMap<String, String>) getIntent().getSerializableExtra("scores");
        }
        startJudgeConfirm();
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
    void startJudgeConfirm() {
        switch (wasteType) { // Modifica la label in base al tipo di bidone selezionato
            case 0: // case "organic":
                selectedBinIs.setText("Il bidone selezionato è quello\ndell'organico");
                selectedBin.setImageResource(R.drawable.bin_brown_shadow);
                break;
            case 1: // case "paper": case "cardboard":
                selectedBinIs.setText("Il bidone selezionato è quello\ndi carta e cartone");
                selectedBin.setImageResource(R.drawable.bin_blue_shadow);
                break;
            case 2: // case "plastic": case "metal":
                selectedBinIs.setText("Il bidone selezionato è quello\ndi plastica e metalli");
                selectedBin.setImageResource(R.drawable.bin_yellow_shadow);
                break;
            case 3: // case "glass":
                selectedBinIs.setText("Il bidone selezionato è quello\ndel vetro");
                selectedBin.setImageResource(R.drawable.bin_green_shadow);
                break;
            default:
                selectedBinIs.setText("ERRORE.");
                break;
        }

        if(pressed) {
            // TODO Incrementa score
            nextTurn();
        }


    }

    public void buttonYes(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        isAnswerCorrect = true;
        pressed = true;
        //TODO congratulazioni per la risp corretta
        nextTurn();
    }
    public void buttonNo(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        isAnswerCorrect = false;
        pressed = true;
        //TODO risposta sbagliata
        nextTurn();
    }
    public void nextTurn() { // Avvia la activity relativa al prossimo turno (o di Game Over)
        Intent activity2Intent;
        isPepperTurn = !isPepperTurn; // Turno successivo
        if (round < 6) {    // TODO sostituisci 6 con una costante
            if (isPepperTurn) {
                activity2Intent = new Intent(JudgeConfirmActivity.this, TodoActivity.class); // PlayPepperTurnActivity.class);
            } else {
                activity2Intent = new Intent(JudgeConfirmActivity.this, PlayUserTurnActivity.class);
            }
            activity2Intent.putExtra("round", round);
            //activity2Intent.putExtra("scores", scores);
        } else {            // Game over
            activity2Intent = new Intent(JudgeConfirmActivity.this, TodoActivity.class);//TODO GameOverActivity
        }
        startActivity(activity2Intent);
        finish();
    }

    public void buttonBack(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        Intent activity2Intent;//Per andare alla pagina principale
        if(isPepperTurn) { //TODO Rimuovi il bottone back se il turno era di Pepper o lascialo se si vuole ri-scattare la foto?
            activity2Intent = new Intent(getApplicationContext(), PlayPepperTurnActivity.class);
        } else {
            activity2Intent = new Intent(getApplicationContext(), PlayUserTurnActivity.class);
            activity2Intent.putExtra("round", round);
        }

        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
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
        //TODO Chiudi gioco */
    }

}