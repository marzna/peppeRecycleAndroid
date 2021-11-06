package com.example.pepperecycle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class CommonUtils {
    private static final String TAG = "CommonUtils";
//    static Dialog dialog;

    public static void showDialogExit(Activity activity) { //desc sarà il contenuto della finestra di dialogo
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_exit_confirm_layout);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        Log.e(TAG, "Entrata nella showDialog");
        //dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        ImageButton dialogButtonCloseYes = (ImageButton) dialog.findViewById(R.id.dialogButtonCloseYes);
        ImageButton dialogButtonCloseNo = (ImageButton) dialog.findViewById(R.id.dialogButtonCloseNo);

        dialogButtonCloseYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finish();
            }

        });
        dialogButtonCloseNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }

        });
        Log.e(TAG, "Prima di dialog.show");
        dialog.show();
        Log.e(TAG, "Dopo dialog.show");

    }


    public static void showDialog(Activity activity, String mex) { //desc sarà il contenuto della finestra di dialogo
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_tutorial_layout);
        Log.e(TAG, "Entrata nella showDialog");
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        TextView textViewDialogTutorial = (TextView) dialog.findViewById(R.id.textViewDialogTutorial);
        ImageButton dialogButtonClose = (ImageButton) dialog.findViewById(R.id.dialogButtonClose);
//TODOhttps://youtu.be/vDAO7H5w4_I
        Log.e(TAG, "Prima di settext");
        textViewDialogTutorial.setText(mex);
        Log.e(TAG, "Dopo settext");

        dialogButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }

        });

        Log.e(TAG, "Prima di dialog.show");
        dialog.show();
        Log.e(TAG, "Dopo dialog.show");

    }

    /*public static void showScore(Activity activity) {
//TODO
    }*/



}

/*public void showDialogExit(Activity activity, Dialog dialog) { //desc sarà il contenuto della finestra di dialogo

        dialog.setContentView(R.layout.dialog_exit_confirm_layout);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        Log.e(TAG, "Entrata nella showDialog");
        //dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        ImageButton dialogButtonCloseYes = (ImageButton) dialog.findViewById(R.id.dialogButtonCloseYes);
        ImageButton dialogButtonCloseNo = (ImageButton) dialog.findViewById(R.id.dialogButtonCloseNo);

        dialogButtonCloseYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finish();
            }

        });
        dialogButtonCloseNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }

        });
        Log.e(TAG, "Prima di dialog.show");
        dialog.show();
        Log.e(TAG, "Dopo dialog.show");

    }*/