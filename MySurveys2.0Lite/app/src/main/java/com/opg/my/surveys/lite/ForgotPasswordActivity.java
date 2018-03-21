package com.opg.my.surveys.lite;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.sdk.exceptions.OPGException;
import com.opg.sdk.models.OPGForgotPassword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends RootActivity implements View.OnClickListener {

    private Context mContext;
    private TextInputLayout userNameTxtLayout;
    private EditText userNameEt;
    private Button send_btn;
    private TextView main_title, sub_title;
    private Dialog pDialog;
    private RelativeLayout forgotPwwContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mContext = this;
        if(!Util.isTablet(mContext)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        userNameTxtLayout = (TextInputLayout) findViewById(R.id.input_layout_forgot_password);
        userNameEt = (EditText) findViewById(R.id.edt_forgot_password);
        send_btn = (Button) findViewById(R.id.btn_send_forgot_password);
        main_title = (TextView) findViewById(R.id.forgot_pwd_mainTitle);
        sub_title = (TextView) findViewById(R.id.forgot_pwd_subTitle);
        forgotPwwContainer = (RelativeLayout) findViewById(R.id.container_forgot_password);
        ((ImageView) findViewById(R.id.btn_back)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                onBackPressed();
                return false;
            }
        });

        try {
            pDialog = Util.getProgressDialog(mContext);
        } catch (OPGException e) {
            e.printStackTrace();
        }

        main_title.setTypeface(Typeface.createFromAsset(this.getAssets(), "font/roboto_regular.ttf"));
        sub_title.setTypeface(Typeface.createFromAsset(this.getAssets(), "font/roboto_regular.ttf"));
        userNameEt.setTypeface(Typeface.createFromAsset(this.getAssets(), "font/roboto_regular.ttf"));
        send_btn.setTypeface(Typeface.createFromAsset(this.getAssets(), "font/roboto_bold.ttf"));
        try {
            int actionBtnColor = Color.parseColor(MySurveysPreference.getThemeActionBtnColor(mContext));
            send_btn.setTextColor(actionBtnColor);
            forgotPwwContainer.setBackgroundColor(actionBtnColor);
            Drawable background = send_btn.getBackground();
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setStroke(2, actionBtnColor, 0, 0);
        } catch (Exception ex) {
            if(BuildConfig.DEBUG) {
                Log.i(ForgotPasswordActivity.class.getName(), ex.getMessage());
            }
        }
        setViewActions();

    }

    private void setViewActions() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().show();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        else if(getActionBar() != null) {
            getActionBar().show();
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        userNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                verifyEmail(editable.toString());
            }
        });

        send_btn.setOnClickListener(this);
    }

    protected static boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\" + ".[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public void onClick(View view) {
        String email_id = userNameEt.getText().toString().trim();
        if(Util.isOnline(mContext) && email_id.length() != 0 && validateEmail(email_id)) {
            new ForgotPassword(email_id,userNameEt).execute();
        }
        else if(!Util.isOnline(mContext)) {
            Util.showMessageDialog(mContext, getString(R.string.no_network_msg),"");
        }
        else {
            verifyEmail(email_id);
        }
    }

    public void verifyEmail(String email_id) {
        if(!email_id.isEmpty() && !validateEmail(email_id.toString())) {
            userNameTxtLayout.setError(getString(R.string.err_invalid_email_id));
        } else {
            Util.validateEditext(userNameEt, userNameTxtLayout, getString(R.string.err_empty_email_id));
        }
    }

    private class ForgotPassword extends AsyncTask<Object, Object, OPGForgotPassword> {

        String emailID;
        EditText email_et;
        public ForgotPassword(String emailID,EditText email_et) {
            this.emailID = emailID;
            this.email_et = email_et;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(pDialog != null)
                pDialog.show();
        }

        @Override
        protected OPGForgotPassword doInBackground(Object... strings) {
            OPGForgotPassword opgForgotPassword = new OPGForgotPassword();
            try {
                opgForgotPassword = Util.getOPGSDKInstance().forgotPassword(emailID, mContext);
            } catch (Exception ex) {
                opgForgotPassword.setStatusMessage(ex.getMessage());
            }
            return opgForgotPassword;
        }

        @Override
        protected void onPostExecute(OPGForgotPassword opgForgotPassword) {
            super.onPostExecute(opgForgotPassword);
            if(pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            if(opgForgotPassword.isSuccess() && !opgForgotPassword.getStatusMessage().equals(Util.EMAIL_ID_NOT_EXIST))
            {
                Toast.makeText(mContext, getString(R.string.msg_success_mail), Toast.LENGTH_SHORT).show();
                finish();
            }
            else if(opgForgotPassword.getHttpStatusCode() == 200 && opgForgotPassword.getStatusMessage().equals(Util.EMAIL_ID_NOT_EXIST))
            {
                Toast.makeText(mContext,getString(R.string.email_id_not_exist), Toast.LENGTH_SHORT).show();
            }
            else  if(opgForgotPassword.getHttpStatusCode() == 400)
            {
                Toast.makeText(mContext, getString(R.string.err_invalid_email_id), Toast.LENGTH_SHORT).show();
            }
            else if(opgForgotPassword.getHttpStatusCode() == 406) {
                Toast.makeText(mContext, getString(R.string.err_invalid_email_id), Toast.LENGTH_SHORT).show();
            }
            else if(opgForgotPassword.getHttpStatusCode() == 404) {
                Toast.makeText(mContext, getString(R.string.email_id_not_found), Toast.LENGTH_SHORT).show();
            }
            else if(opgForgotPassword.getHttpStatusCode() == 500)
            {
                Toast.makeText(mContext, getString(R.string.internal_server_error), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(mContext, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        pDialog = null;
        super.onDestroy();
    }
}
