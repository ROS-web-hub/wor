package com.app.wor;

import android.app.ProgressDialog;
import androidx.appcompat.app.AppCompatActivity;

//This activity works as Super activity for all other activities who want to show loading progress dialog.
public class BaseActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog;

    //This function show progress dialog on screen with user custom message.
    public void showProgressDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    //This function hide progress dialog from screen.
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }
}