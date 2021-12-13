package com.example.pepperecycle.utils;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.pepperecycle.R;

/* Classe contenente metodi comuni per tutto il gioco.
    Al momento, sono presenti solamente:
        - showDialogExit(...)
        - showDialog(...)
 */
public class CommonUtils {
    private static final String TAG = "CommonUtils";

    //mostra finestra di dialogo che chiede conferma quando si vuole chiudere il gioco
    public static void showDialogExit(Activity activity) { //desc sarà il contenuto della finestra di dialogo
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_exit_confirm_layout);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        //Log.d(TAG, "showDialogExit");
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        ImageButton dialogButtonCloseYes = dialog.findViewById(R.id.dialogButtonCloseYes);
        ImageButton dialogButtonCloseNo = dialog.findViewById(R.id.dialogButtonCloseNo);

        dialogButtonCloseYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finishAffinity(); // Chiusura di tutte le activity
                System.exit(0);  // Rilascio risorse
            }

        });
        dialogButtonCloseNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }

        });
        dialog.show();

    }

    /* Mostra finestra di dialogo più generica. Attualmente, è utilizzata solo
     * per la spiegazione del turno corrente quando si preme sul bottone di "?"
     */
    public static void showDialog(Activity activity, String mex) { //desc sarà il contenuto della finestra di dialogo
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_tutorial_layout);
        //Log.d(TAG, "showDialog");
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        TextView textViewDialogTutorial = dialog.findViewById(R.id.textViewDialogTutorial);
        ImageButton dialogButtonClose = dialog.findViewById(R.id.dialogButtonClose);

        textViewDialogTutorial.setText(mex);

        dialogButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }

        });

        dialog.show();

    }
}