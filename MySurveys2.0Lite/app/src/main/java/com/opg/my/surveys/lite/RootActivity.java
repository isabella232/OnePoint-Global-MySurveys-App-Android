package com.opg.my.surveys.lite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.opg.my.surveys.lite.common.Util;

public class RootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        Util.setStatusBarColor(this);
    }
    public  boolean isOnline(Context context)
    {
        ConnectivityManager cm =(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public  void showAlertNoInterNet(final Context context)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("No Network...");

        // Setting Dialog Message
        alertDialog.setMessage("Please check the network connection...");

        // Setting Icon to Dialog
        // alertDialog.setIcon(R.drawable.info);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
                ((Activity)context).finish();
            }
        });

        alertDialog.show();
    }
    public void showAlert(String title, String message)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @SuppressWarnings("deprecation")
    public void setDrawable(Drawable drawable, View view){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        }else {
            view.setBackground(drawable);
        }
    }

    /**
     * Method to set font for a particular view
     * @param view
     * @param type
     */
    public void setTypeface(View view, String type)
    {
        Typeface myTypeface  =   Typeface.createFromAsset(getAssets(),type)  ;

        if(view instanceof TextView)
            ((TextView)view).setTypeface(myTypeface);

        if(view instanceof Button)
            ((Button)view).setTypeface(myTypeface);

        if(view instanceof EditText)
            ((EditText)view).setTypeface(myTypeface);

        if(view instanceof TextInputLayout)
            ((TextInputLayout)view).setTypeface(myTypeface);
    }
}
