package com.opg.my.surveys.lite.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.opg.my.surveys.lite.HomeActivity;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.sdk.models.OPGChangePassword;
import com.opg.my.surveys.lite.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChangePasswordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChangePasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangePasswordFragment extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String POSITION_KEY = "FragmentPositionKey";

    private String mParam1;
    private String mParam2;
    private TextView tvChangePwdTitle;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnConfirm;
    private Button btnCancel;
    private TextView tvEmailId;
    private TextInputLayout inputLayoutNewPassword, inputLayoutOldPassword, inputLayoutConfirmPassword;
    private OnFragmentInteractionListener mListener;
    private static ChangePasswordFragment fragment;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChangePasswordFragment.
     */
    public static ChangePasswordFragment newInstance(String param1, String param2)
    {
        if(fragment == null)
        {
            fragment = new ChangePasswordFragment();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            args.putString(ARG_PARAM2, param2);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view =  inflater.inflate(R.layout.fragment_change_password, container, false);
        etOldPassword = (EditText)view.findViewById(R.id.et_old_password);
        etNewPassword = (EditText)view.findViewById(R.id.et_new_password);
        etConfirmPassword = (EditText)view.findViewById(R.id.et_confirm_password);
        btnConfirm = (Button)view.findViewById(R.id.btn_confirm_change_password);
        btnCancel = (Button)view.findViewById(R.id.btn_cancel_change_password);
        inputLayoutOldPassword = (TextInputLayout)view.findViewById(R.id.input_layout_old_password_text);
        inputLayoutNewPassword = (TextInputLayout)view.findViewById(R.id.input_layout_new_password_text);
        inputLayoutConfirmPassword = (TextInputLayout)view.findViewById(R.id.input_layout_confirm_password_text);
        tvEmailId = (TextView)view.findViewById(R.id.tv_email_id);
        tvChangePwdTitle = (TextView)view.findViewById(R.id.tv_title_change_pwd);
        Util.setTypeface(getActivity(),tvEmailId,"font/roboto_regular.ttf");
        Util.setTypeface(getActivity(),etOldPassword,"font/roboto_regular.ttf");
        Util.setTypeface(getActivity(),etNewPassword,"font/roboto_regular.ttf");
        Util.setTypeface(getActivity(),etConfirmPassword,"font/roboto_regular.ttf");
        Util.setTypeface(getActivity(),tvChangePwdTitle,"font/roboto_regular.ttf");
        btnConfirm.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));
        btnCancel.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));
        setEmptyText();
        try
        {
            tvEmailId.setText(RetriveOPGObjects.getPanellistProfile().getEmailID());
        }
        catch (Exception ex)
        {
            Log.i(Util.TAG,ex.getMessage());
        }

        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setEmptyText(); // Reason why we keep it in onResume() https://stackoverflow.com/questions/13303469/edittext-settext-not-working-with-fragment
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        fragment.getView().setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    getFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        } );
    }

    @Override
    public void onDestroyView() {
        etOldPassword = null;
        etNewPassword = null;
        etConfirmPassword = null;
        btnConfirm = null;
        btnCancel = null;
        inputLayoutOldPassword = null;
        inputLayoutNewPassword = null;
        inputLayoutConfirmPassword = null;
        tvEmailId = null;
        tvChangePwdTitle = null;
        super.onDestroyView();
    }
    private void setEmptyText()
    {
        etOldPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
        etConfirmPassword.clearFocus();
    }
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_confirm_change_password :
                if(Util.isOnline(getActivity()))
                {
                    if(etOldPassword.getText().toString().trim().isEmpty() || etNewPassword.getText().toString().trim().isEmpty() || etConfirmPassword.getText().toString().trim().isEmpty() )
                    {

                        if(etOldPassword.getText().toString().trim().isEmpty())
                        {
                            etOldPassword.setError(getString(R.string.required_error_msg));
                        }
                        if(etNewPassword.getText().toString().trim().isEmpty())
                        {
                            etNewPassword.setError(getString(R.string.required_error_msg));
                        }
                        if(etConfirmPassword.getText().toString().trim().isEmpty())
                        {
                            etConfirmPassword.setError(getString(R.string.required_error_msg));
                        }
                    }
                    else  if(!etOldPassword.getText().toString().isEmpty() && !etNewPassword.getText().toString().isEmpty() && !etConfirmPassword.getText().toString().isEmpty() )
                    {

                        if(! etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString()))
                        {
                            Toast.makeText(getActivity(),getString(R.string.err_new_and_confirm_pwd_not_same),Toast.LENGTH_SHORT).show();
                        }
                        else if(etNewPassword.getText().toString().equals(etOldPassword.getText().toString()))
                        {
                            Toast.makeText(getActivity(),getString(R.string.err_old_and_new_pwd_same),Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            changePassword(etOldPassword.getText().toString(),etNewPassword.getText().toString());

                        }
                    }
                    else
                    {
                        Toast.makeText(getActivity(),getString(R.string.err_empty_pwd),Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    // Util.showNoNetworkDialog(getActivity());
                    ((HomeActivity)getActivity()).showSnackBar(getString(R.string.no_network_msg), Snackbar.LENGTH_LONG);
                }

                break;
            case R.id.btn_cancel_change_password :
                getFragmentManager().popBackStack();
                break;
        }

    }

    private void changePassword(final String oldPassword,final String newPassword)
    {
        final Dialog pDialog =Util.getProgressDialog(getActivity());  new ProgressDialog(getActivity());
        //pDialog.setIndeterminate(true);
        //pDialog.setCancelable(true);
        new AsyncTask<String,Void,OPGChangePassword>()
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog.show();
            }

            @Override
            protected OPGChangePassword doInBackground(String... strings)
            {
                OPGChangePassword opgChangePassword = new OPGChangePassword();

                try
                {
                    opgChangePassword = Util.getOPGSDKInstance().changePassword(getActivity(),strings[0],strings[1]);
                }
                catch (Exception ex)
                {
                    Log.i(Util.TAG,ex.getMessage());
                    opgChangePassword.setStatusMessage(ex.getMessage());
                }
                return opgChangePassword;
            }

            @Override
            protected void onPostExecute(OPGChangePassword opgChangePassword)
            {
                super.onPostExecute(opgChangePassword);
                if(pDialog != null && pDialog.isShowing())
                {
                    pDialog.dismiss();
                }
                if(opgChangePassword.isSuccess())
                {
                    Toast.makeText(getActivity(),opgChangePassword.getStatusMessage(),Toast.LENGTH_SHORT).show();
                    //getFragmentManager().popBackStack();
                    setEmptyText();
                }
                else  if(opgChangePassword.getHttpStatusCode() == 404)
                {
                    Toast.makeText(getActivity(), getString(R.string.current_password_does_not_exist), Toast.LENGTH_SHORT).show();
                }
                else if(opgChangePassword.getHttpStatusCode() == 500)
                {
                    Toast.makeText(getActivity(), getString(R.string.internal_server_error), Toast.LENGTH_SHORT).show();
                }
                else if(opgChangePassword.getStatusMessage().contains(Util.SESSION_TIME_OUT_ERROR))
                {
                    Util.launchLoginActivity(getActivity());
                }
                else
                {
                    Toast.makeText(getActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(oldPassword, newPassword);
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if(mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private boolean validateEditext(EditText editText, TextInputLayout inputLayout, String errMsg) {
        if (editText!=null && editText.getText().toString().trim().isEmpty())
        {
            inputLayout.setError(errMsg);
            inputLayout.setErrorEnabled(true);
            editText.setBackgroundResource(R.drawable.singleline_et_err_bg);
            return false;
        }
        else
        {
            inputLayout.setErrorEnabled(false);
            editText.setBackgroundResource(R.drawable.singleline_et_bg_gray);
        }

        return true;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
