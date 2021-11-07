package com.example.pepperecycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;

public class TutorialEndActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    Button buttonYes, buttonNo, buttonPlayEndTutorial;
    ImageButton buttonBack, buttonHome, buttonClose;
    boolean endOfTutorial;
    int pgIndex; //Non serve??
    byte trialState = -1;
    TextView tvTrialTitle, tvTrialQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_end);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        buttonYes = findViewById(R.id.buttonTrialYes);
        buttonNo = findViewById(R.id.buttonTrialNo);
        buttonPlayEndTutorial = findViewById(R.id.buttonPlayEndTutorial);
        buttonBack = findViewById(R.id.buttonBack);
        buttonHome = findViewById(R.id.buttonHome);
        buttonClose = findViewById(R.id.buttonClose);
        tvTrialTitle = findViewById(R.id.tvTrialTitle);
        tvTrialQuestion = findViewById(R.id.tvTrialQuestion);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            endOfTutorial = extras.getBoolean("endOfTutorial");
            pgIndex = extras.getInt("pgIndex");
            trialState = extras.getByte("trialState");
        }

        if(trialState == 2) {
            buttonPlayEndTutorial.setEnabled(true);
            buttonPlayEndTutorial.setVisibility(View.VISIBLE);
            buttonYes.setVisibility(View.INVISIBLE);
            buttonNo.setVisibility(View.INVISIBLE);

            tvTrialTitle.setText("Turno di prova - FINE");
            tvTrialQuestion.setText("Il turno di prova è finito.\nAdesso giochiamo sul serio!");
            trialState = -1;
        } else {
            buttonPlayEndTutorial.setEnabled(false);
            buttonPlayEndTutorial.setVisibility(View.INVISIBLE);

            tvTrialTitle.setText("Turno di prova - INIZIO");
            tvTrialQuestion.setText("Vuoi giocare un turno di prova?");
        }

        //OnClickListeners
        buttonYes.setOnClickListener(new View.OnClickListener() {
            // Si gioca un turno di prova
            @Override
            public void onClick(View view) {
                playGame(true);
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            // Si gioca direttamente, saltando il turno di prova

            @Override
            public void onClick(View view) {
                playGame(false);
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent activity2Intent = new Intent(getApplicationContext(), TutorialActivity.class);
                activity2Intent.putExtra("endOfTutorial", false);
                activity2Intent.putExtra("pgIndex", 3); //TODO ultima pagina?
                startActivity(activity2Intent); //Per andare alla pagina precedente
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                finish();
            }
        });

        buttonHome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(activity2Intent); //Per andare alla pagina principale
                finish();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CommonUtils.showDialogExit(TutorialEndActivity.this);
            }
        });

        buttonPlayEndTutorial.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playGame(false);
            }
        });

    }

    public void playGame(boolean tutorialEnabled) {
        Intent activity2Intent = new Intent(TutorialEndActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        //if(tutorialEnabled) { //Se il tutorial è attivo, toccherà all'utente
        if (tutorialEnabled) {
            activity2Intent.putExtra("isPepperTurn", false); //toccherà all'utente in quanto è tutorial
            trialState = 0;
        }

        //TODO Servono?
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        activity2Intent.putExtra("roundTutorial", true);
        activity2Intent.putExtra("trialState", trialState);

        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)

        startActivity(activity2Intent);
        finish();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        //TODO Aggiungi dialoghi
    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
}