package com.example.pepperecycle;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;

import java.util.List;

public class MyQiChatExecutor extends BaseQiChatExecutor {

    String TAG = "MyQiChatExecutor";
    private int mCurrentPage;

    MyQiChatExecutor(QiContext context, int mCurrentPage) {
        super(context);
        this.mCurrentPage = mCurrentPage;
    }

    MyQiChatExecutor(QiContext context) {
        super(context);
    }

    //runWith will be called when ^execute is reached in the topic.
    /*@Override
    public void runWith(List<String> params) {
        // This is called when execute is reached in the topic
        Log.i(TAG, "Arm raised = " + params.get(0));
    }
    */
    @Override
    public void runWith(List<String> params) {
        // This is called when execute is reached in the topic
/*
        switch (params.get(0)) { //in base alla scelta, fa una cosa diversa
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            default:
                break;
        }
*/
    }


    //stop will be called when the chat that handles the qiChatbot is canceled.
    @Override
    public void stop() {
        // This is called when chat is canceled or stopped.
        Log.i(TAG, "execute stopped");
    }

    public int getmCurrentPage() {
        return mCurrentPage;
    }

    public void setmCurrentPage(int mCurrentPage) {
        this.mCurrentPage = mCurrentPage;
    }

    public void page0() {

    }

}