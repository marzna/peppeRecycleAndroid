package com.example.pepperecycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;

public class TutorialEndActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    Button buttonYes, buttonNo;
    ImageButton buttonBack, buttonHome, buttonClose;
    boolean endOfTutorial;
    int pgIndex; //Non serve??
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_end);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        buttonYes = findViewById(R.id.buttonTrialYes);
        buttonNo = findViewById(R.id.buttonTrialNo);
        buttonBack = findViewById(R.id.buttonBack);
        buttonHome = findViewById(R.id.buttonHome);
        buttonClose = findViewById(R.id.buttonClose);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            endOfTutorial = extras.getBoolean("endOfTutorial");
            pgIndex = extras.getInt("pgIndex");
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

    }

    public void playGame(boolean tutorialEnabled) {
        Intent activity2Intent = new Intent(TutorialEndActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        if(tutorialEnabled) { //Se il tutorial è attivo, toccherà all'utente
            activity2Intent.putExtra("isPepperTurn", false); //toccherà all'utente in quanto è tutorial
        }
        //TODO Servono?
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
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