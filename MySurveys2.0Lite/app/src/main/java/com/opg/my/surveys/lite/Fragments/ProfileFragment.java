package com.opg.my.surveys.lite.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.opg.my.surveys.lite.BuildConfig;
import com.opg.my.surveys.lite.CountryActivity;
import com.opg.my.surveys.lite.FetchDataService;
import com.opg.my.surveys.lite.HomeActivity;
import com.opg.my.surveys.lite.LoginActivity;
import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.common.Aes256;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.RoundedImageView;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.my.surveys.lite.common.db.SaveOPGObjects;
import com.opg.sdk.exceptions.OPGException;
import com.opg.sdk.models.OPGDownloadMedia;
import com.opg.sdk.models.OPGPanellistProfile;
import com.opg.sdk.models.OPGUpdatePanellistProfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import OnePoint.Common.Utils;
import OnePoint.CordovaPlugin.Utils.FileUtils;

import static android.app.Activity.RESULT_OK;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.opg.my.surveys.lite.common.Util.REQUEST_CODE_PROFILE;
import static com.opg.my.surveys.lite.common.Util.TAG;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static ProfileFragment profileFragment;
    private int CAPTURE_IMAGE_REQUEST_CODE = 1;
    private int PICK_IMAGE_REQUEST_CODE = 2;
    private TextInputLayout input_layout_name;
    private RoundedImageView imgProfile;
    private TextView btnEdit;
    private EditText txtName;
    private EditText txtEmailID;
    private EditText etCountryName;
    private ImageView btnChangeImage;
    private TextView btnLogout;
    private ProgressBar progressBar;
    private static OPGPanellistProfile panellistProfile;
    private GoogleApiClient mGoogleApiClient;
    private LinearLayout containerGallery, containerCamera, containerRemove;
    private AsyncTask<String, Void, OPGDownloadMedia> downloadProfilePic;
    private AsyncTask<OPGPanellistProfile, Void, OPGUpdatePanellistProfile> updPanellistProAsyncTask;
    private Dialog progressDialog;
    private Dialog selectImageDialog;
    private String fileName;
    private File saveFile;// =  new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+ Utils.getApplicationName(getActivity())+File.separator+Util.PROFILE_PICS, fileName);

    private boolean updatedCountry = false;

    private boolean canUpload = true;
    private boolean isGallery = false;
    public String IMAGE_REGEX = "%%0%dd";
    public static final String IMAGE_GALLERY_ = "image_gallery_";
    public String JPEG = ".jpeg";
    private String tempDir = "temp/";
    private String imagesDir = "/images/";
    public String CANNOT_CREATE_DIR = "Cannot create dir ";
    private String _currentMediaPath = "";
    private static int MAX_IMAGE_SIZE = 1 * 1024 * 1024;//size in bytes
    private boolean fetchData = true;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Util.BROADCAST_ACTION_SAVE_DATA) || intent.getAction().equals(Util.BROADCAST_ACTION_REFRESH)) {
                if (MySurveysPreference.isDownloaded(getActivity())) {
                    getProfileImageFromLocalDB();
                    getProfileDataFromLocalDB();
                }
                if (Util.isServiceRunning(getActivity(), FetchDataService.class)) {
                    ((HomeActivity) getActivity()).showSnackBar(getString(R.string.sync_msg), Snackbar.LENGTH_INDEFINITE);
                } else {
                    ((HomeActivity) getActivity()).dismissSnackBar();
                }
            }
        }
    };

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }
        return profileFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        try {
            progressDialog = Util.getProgressDialog(getActivity());
        } catch (OPGException e) {
            e.printStackTrace();
        }
        input_layout_name = view.findViewById(R.id.input_layout_name);
        imgProfile = view.findViewById(R.id.img_profile);
        txtName = view.findViewById(R.id.et_name);
        txtEmailID = view.findViewById(R.id.et_emailid);
        etCountryName = view.findViewById(R.id.et_country);
        btnChangeImage = view.findViewById(R.id.btn_change_image);
        btnEdit = view.findViewById(R.id.tv_edit);
        progressBar = view.findViewById(R.id.progress_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnChangeImage.setOnClickListener(this);
        txtName.setEnabled(false);
        etCountryName.setEnabled(false);
        btnEdit.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        etCountryName.setOnClickListener(this);

        progressBar.setIndeterminate(true);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())), android.graphics.PorterDuff.Mode.MULTIPLY);//Color.parseColor(ThemeManager.getThemeManagerInstance().getActionBtn())
        Util.setTypeface(getActivity(),  view.findViewById(R.id.tv_profile), "font/roboto_regular.ttf");
        Util.setTypeface(getActivity(),  view.findViewById(R.id.tv_edit), "font/roboto_regular.ttf");

        Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.profile_circle);
        //putting default profile image
        if (bitmap != null) {
            imgProfile.setImageBitmap(bitmap);

        }

        //fetching  the panellist profile from local SQLITE db
        getProfileImageFromLocalDB();
        setUploadListener();
        File mediaDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Utils.getApplicationName(getActivity()));
        if (!mediaDir.exists()) {
            mediaDir.mkdir();
        }
        fileName = System.currentTimeMillis() / 1000L + ".jpeg";
        saveFile = new File(mediaDir, fileName);

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        txtName.setFilters(new InputFilter[]{filter});

        return view;
    }

    private void setUploadListener() {
        ((HomeActivity) getActivity()).setUploadProfilePicListener(new HomeActivity.UploadProfilePicListener() {
            @Override
            public void onStartUpload() {
                if (progressBar != null)
                    progressBar.setVisibility(View.VISIBLE);
                canUpload = false;
            }

            @Override
            public void onUploadCompleted(String mediaID, String picturePath, String errorMessage) {
                canUpload = true;
                try {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (mediaID != null) {
                        panellistProfile.setMediaID(mediaID);
                        Util.moveFile(getActivity(), picturePath, mediaID, Util.PROFILE_PICS);
                        File file = new File(picturePath);
                        if (file.exists()) {
                            file.delete();
                        }
                        picturePath = Util.searchFile(getActivity(), mediaID, Util.PROFILE_PICS);
                        encryptFile(picturePath);
                        setProfileImage(picturePath, false);
                    } else if (errorMessage.equals(Util.SESSION_TIME_OUT_ERROR)) {
                        Util.launchLoginActivity(getActivity());
                    } else {
                        showToast(getString(R.string.unknown_error));
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void onStop() {
        stopDownloadProfilePicAsync();
        downloadProfilePic = null;
        super.onStop();
    }

    /**
     *  Stops the already running downloadProfileImageAsyncTask
     */
    private void stopDownloadProfilePicAsync() {
        if (downloadProfilePic != null && downloadProfilePic.getStatus() != AsyncTask.Status.FINISHED) {
            downloadProfilePic.cancel(true);
        }
    }


    @Override
    public void onDestroyView() {
        progressDialog = null;
        imgProfile = null;
        txtName = null;
        txtEmailID = null;
        etCountryName = null;
        btnChangeImage = null;
        btnEdit = null;
        progressBar = null;
        btnLogout = null;
        containerGallery = null;
        containerCamera = null;
        containerRemove = null;
        selectImageDialog = null;
        downloadProfilePic = null;
        updPanellistProAsyncTask = null;
        input_layout_name = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter iff = new IntentFilter(Util.BROADCAST_ACTION_SAVE_DATA);
        iff.addAction(Util.BROADCAST_ACTION_REFRESH);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, iff);
        if (!updatedCountry) {
            //not getting the profile frm db when we change the country
            getProfileDataFromLocalDB();
        }
        updatedCountry = false;
        if (isUploading() && progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            canUpload = false;
        } else {
            canUpload = true;
        }
    }

    private boolean isUploading() {
        return ((HomeActivity) getActivity()).getUpdProfileImage() != null &&
                ((HomeActivity) getActivity()).getUpdProfileImage().getStatus() == AsyncTask.Status.RUNNING;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }


    private void getProfileDataFromLocalDB() {
        if (MySurveysPreference.isDownloaded(getActivity())) {
            try {
                Thread profileDataThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (fetchData) {
                            try {
                                panellistProfile = RetriveOPGObjects.getPanellistProfile();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(panellistProfile != null) {
                                            updateProfileViews();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                profileDataThread.setPriority(Thread.MIN_PRIORITY);
                profileDataThread.start();

            } catch (Exception ex) {
                Log.i(Util.TAG, ex.getMessage());
            }
        }
    }

    private void updateProfileViews() {
        if (panellistProfile != null && txtName != null) {
            txtName.setText(panellistProfile.getFirstName());
            txtEmailID.setText(panellistProfile.getEmailID());
            etCountryName.setText(panellistProfile.getCountryName());
            txtName.setSelection(panellistProfile.getFirstName().length());
            Util.validateEditext(txtName, input_layout_name, getString(R.string.err_username_msg));
        }
    }

    private void getProfileImageFromLocalDB() {
        Thread profileDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (fetchData) {
                    try {
                        panellistProfile = RetriveOPGObjects.getPanellistProfile();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateProfileImage();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        profileDataThread.setPriority(Thread.MIN_PRIORITY);
        profileDataThread.start();
    }

    private void updateProfileImage() {
        try {
            if (panellistProfile.getMediaID() != null && !panellistProfile.getMediaID().isEmpty() && !panellistProfile.getMediaID().equals("0")) {
                String profilePicPath = Util.searchFile(getActivity(), panellistProfile.getMediaID(), Util.PROFILE_PICS);
                if (profilePicPath != null) {
                    setProfileImage(profilePicPath, true);
                } else {
                    downloadMedia(panellistProfile.getMediaID());
                }
            }
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) {
                Log.i(Util.TAG, ex.toString());
            }
        }
    }

    private void setProfileImage(final String profilePicPath, final boolean retry) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (imgProfile != null) {
            imgProfile.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (imgProfile != null && profilePicPath != null) {
                            /*String desFilePath = decryptFile(profilePicPath);*/
                            // First decode with inJustDecodeBounds=true to check dimensions
                            final BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            // Calculate inSampleSize
                            options.inSampleSize = calculateInSampleSize(options, imgProfile.getMeasuredWidth(), imgProfile.getMeasuredHeight());
                            // Decode bitmap with inSampleSize set
                            options.inJustDecodeBounds = false;
                            /*final Bitmap bitmap = BitmapFactory.decodeFile(desFilePath, options);
                            deleteFile(desFilePath);*/
                            //Decrypting the bytearray of the image from the encrypted
                            byte[] decryptArray = Aes256.decryptFileData(profilePicPath);
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(decryptArray, 0, decryptArray.length, options);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressBar != null && !isUploading()) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    if (bitmap != null) {
                                        /**
                                         *  don't combine both statements as there is dependency for
                                         *  the bitmap and the imgProfile. So we need to check them seperately
                                         *  @Neeraj
                                         */
                                        if (imgProfile != null) {
                                            imgProfile.setImageBitmap(bitmap);
                                        }
                                    } else if (retry) {
                                        //fix - if corrupted image is present in sdcard then deleting it and downloading it again @Neeraj
                                        File file = new File(profilePicPath);
                                        if (file.exists() && file.delete()) {
                                            downloadMedia(panellistProfile.getMediaID());
                                        }
                                    }
                                }
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, 500);
        }
    }

    private boolean deleteFile(String desFilePath) {
        File file = new File(desFilePath);
        return file.delete();
    }

    @Override
    public void onClick(View view) {
        if (Util.isOnline(getActivity())) {
            switch (view.getId()) {
                case R.id.tv_edit:
                    onEditOrSaveClick();
                    break;

                case R.id.btn_change_image:
                    if (MySurveysPreference.isDownloaded(getActivity())) {
                        if (canUpload) {
                            selectImage();
                        }
                    } else if (MySurveysPreference.isDownloaded(getActivity())) {
                        Util.showSyncDialog(getActivity());
                    }
                    break;

                case R.id.btn_logout:
                    logout();
                    break;

                case R.id.et_country:
                    startCountryActivity();
                    break;
            }
        } else {
            showToast(getString(R.string.err_no_internet));
        }
    }

    private void onEditOrSaveClick() {
        if (MySurveysPreference.isDownloaded(getActivity())) {
            if (txtName.isEnabled()) {
                Util.validateEditext(txtName, input_layout_name, getString(R.string.err_name_msg));
                if (!txtName.getText().toString().trim().isEmpty()) {
                    txtName.setEnabled(false);
                    etCountryName.setEnabled(false);
                    btnEdit.setText(R.string.title_edit_profile);
                    panellistProfile.setFirstName(txtName.getText().toString());
                    //Disabled under Line
                    txtName.setBackgroundResource(R.drawable.editext_trans_bg);
                    etCountryName.setBackgroundResource(R.drawable.editext_trans_bg);
                    panellistProfile.setPostalCode(etCountryName.getText().toString());
                    updatePanellistProfile(panellistProfile);
                }
            } else {
                txtName.setEnabled(true);
                etCountryName.setEnabled(true);
                btnEdit.setText(R.string.title_save_profile);
                txtName.setBackgroundResource(R.drawable.singleline_et_bg_gray);
                etCountryName.setBackgroundResource(R.drawable.singleline_et_bg_gray);

            }
        } else {
            Util.showSyncDialog(getActivity());
        }
    }

    private void startCountryActivity() {
        //for hiding Keyboard onClick of editText
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (etCountryName != null) {
            etCountryName.requestFocus();
            imm.hideSoftInputFromWindow(etCountryName.getWindowToken(), 0);
        }
        Intent intent = new Intent(getActivity(), CountryActivity.class);
        intent.putExtra("panellistProfile", panellistProfile);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, Util.COUNTRY_RESULT_CODE);
        }
        updatedCountry = true;
    }

    private void showToast(String msg) {
        if (getActivity() != null)
            makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void downloadMedia(final String mediaId) {
        if (Util.isOnline(getActivity())) {
            downloadProfilePic = new DownloadProfilePic().execute(mediaId);
        } else {
            showToast(getString(R.string.err_no_internet));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }

    private void logout() {
        showLogoutDialog();
    }

    private void showLogoutDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_logout);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_confirm_logout);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel_logout);
        btnConfirm.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));
        btnCancel.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));
        TextView tvTitleLogoutDialog = (TextView) dialog.findViewById(R.id.tv_title_logout_dialog);
        tvTitleLogoutDialog.setText(getString(R.string.logout_message));
        final ProgressBar progressBar1 = (ProgressBar) dialog.findViewById(R.id.pb_upload_results);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar1.setVisibility(View.VISIBLE);
                logout(dialog);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void logout(final Dialog dialog) {
        stopDownloadProfilePicAsync();
        ((HomeActivity) getActivity()).logout(mGoogleApiClient, new HomeActivity.LogoutListener() {
            @Override
            public void onStart() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (progressDialog != null) {
                    progressDialog.show();
                }
            }

            @Override
            public void onCompleted() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //Clearing the task history
                getActivity().finish();
                startActivity(intent); //Launching LoginActivity
            }
        });
    }

    private void selectImage() {
        selectImageDialog = null;
        selectImageDialog = new Dialog(getActivity());
        selectImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectImageDialog.setContentView(R.layout.dialog_select_image);
        Button btnCancel = (Button) selectImageDialog.findViewById(R.id.btn_cancel_select_image);
        containerGallery = (LinearLayout) selectImageDialog.findViewById(R.id.container_gallery);
        containerCamera = (LinearLayout) selectImageDialog.findViewById(R.id.container_camera);
        containerRemove = selectImageDialog.findViewById(R.id.container_remove);
        TextView tvTitle = (TextView) selectImageDialog.findViewById(R.id.tv_select_image_title);
        tvTitle.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));

        containerGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });
        containerCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImageFromCamera();
            }
        });
        containerRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(panellistProfile!=null && !panellistProfile.getMediaID().equals("0"))
                    removeProfileImage();
                selectImageDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageDialog.dismiss();
            }
        });
        btnCancel.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));

        selectImageDialog.show();
    }

    private void captureImageFromCamera() {
        if (Util.checkPermission(getActivity(), Manifest.permission.CAMERA) && Util.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            isGallery = false;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName(), saveFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(saveFile));
            }
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
            } else {
                makeText(getActivity(), "No App found to launch camera", LENGTH_SHORT).show();
            }
            selectImageDialog.dismiss();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PROFILE);
        }
    }

    private void selectImageFromGallery() {
        if (Util.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) && Util.checkPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            isGallery = true;
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // -  we are not using "Intent.ACTION_PICK" becoz it needs Photos App of google
            galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            ComponentName componentName = galleryIntent.resolveActivity(getActivity().getPackageManager());
            if (componentName != null) {
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE);
            } else {
                Intent newIntent = new Intent(Intent.ACTION_GET_CONTENT);
                newIntent.setType("image/*");
                componentName = newIntent.resolveActivity(getActivity().getPackageManager());
                if (componentName != null) {
                    startActivityForResult(newIntent, PICK_IMAGE_REQUEST_CODE);
                } else {
                    makeText(getActivity(), "No App found to open image", LENGTH_SHORT).show();

                }
            }
            selectImageDialog.dismiss();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PROFILE);
        }

    }

    private void removeProfileImage() {
        if (panellistProfile != null && Util.isOnline(getActivity())) {
            panellistProfile.setMediaID("0");
            updatePanellistProfile(panellistProfile);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String path = FileUtils.getPath(getActivity(), uri);
            if (path != null && !path.isEmpty()) {
                _currentMediaPath = path;
                new CompressImage(path).execute();
            } else {
                try {
                    final InputStream ist = getActivity().getContentResolver().openInputStream(data.getData());
                    copyToFile(ist, saveFile);
                    _currentMediaPath = saveFile.getAbsolutePath();
                    new CompressImage(saveFile.getAbsolutePath()).execute();
                } catch (Exception ex) {

                }
            }
        } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            _currentMediaPath = saveFile.getAbsolutePath();
            new CompressImage(saveFile.getAbsolutePath()).execute();
        } else if (requestCode == Util.COUNTRY_RESULT_CODE && resultCode == RESULT_OK && data != null)/*&& data.getExtras() != null*/ {
            if (data.getParcelableExtra("panellistProfile") != null) {
                updatedCountry = true;
                OPGPanellistProfile newPanellistProfile = data.getParcelableExtra("panellistProfile");
                panellistProfile.setCountryName(newPanellistProfile.getCountryName());
                panellistProfile.setStd(newPanellistProfile.getStd());
                etCountryName.setText(panellistProfile.getCountryName());
            }
        }
    }

    private static boolean copyToFile(InputStream inputStream, File destFile) {
        if (inputStream == null || destFile == null) return false;
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void uploadProfileImage(final String profilePicturePath) {
        if (Util.isOnline(getActivity())) {
            if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                ((HomeActivity) getActivity()).startUploadPic(profilePicturePath);
            } else {
                Util.showMessageDialog(getActivity(), getResources().getString(R.string.unknown_error), "");
            }
        } else {
            showToast(getString(R.string.err_no_internet));
            makeText(getActivity(), "", LENGTH_SHORT).show();
        }
    }

    private void updatePanellistProfile(final OPGPanellistProfile panellistProfile) {

        if (Util.isOnline(getActivity())) {
            updPanellistProAsyncTask = new UpdatePanellistProfile(panellistProfile).execute(panellistProfile);
        } else {
            showToast(getString(R.string.err_no_internet));
        }
    }

    public class DownloadProfilePic extends AsyncTask<String, Void, OPGDownloadMedia> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressBar != null)
                progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected OPGDownloadMedia doInBackground(String... strings) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            return Util.getOPGSDKInstance().downloadMediaFile(getActivity(), strings[0], "JPEG");
        }

        @Override
        protected void onPostExecute(OPGDownloadMedia opgDownloadMedia) {
            super.onPostExecute(opgDownloadMedia);
            if(progressBar != null)
                progressBar.setVisibility(View.INVISIBLE);
            if(opgDownloadMedia != null && opgDownloadMedia.getMediaPath() != null && panellistProfile!=null && panellistProfile.getMediaID()!= null && !panellistProfile.getMediaID().isEmpty())
            {
                String mediaPath = Util.moveFile(getActivity(),opgDownloadMedia.getMediaPath(),panellistProfile.getMediaID(),Util.PROFILE_PICS);
                encryptFile(mediaPath);
                setProfileImage(mediaPath,false);
                deleteFile(opgDownloadMedia.getMediaPath());
            }
            else
            {
                showToast(getString(R.string.err_failed_to_load_profile_pic));
            }
        }
    }

    private void encryptFile(String mediaPath) {
        File profileImage = new File(mediaPath);
        String filePath = profileImage.getAbsolutePath().split(panellistProfile.getMediaID())[0];
        String fileName = profileImage.getName();
        Aes256.encryptFile(mediaPath,filePath,fileName);
    }

    private class UpdatePanellistProfile extends AsyncTask<OPGPanellistProfile, Void, OPGUpdatePanellistProfile> {

        OPGPanellistProfile panellistProfile;

        public UpdatePanellistProfile(OPGPanellistProfile panellistProfile)
        {
            this.panellistProfile = panellistProfile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(progressDialog != null)
                progressDialog.show();
        }

        @Override
        protected OPGUpdatePanellistProfile doInBackground(OPGPanellistProfile...profile)
        {
            OPGPanellistProfile panellistProfile = profile[0];
            OPGUpdatePanellistProfile updateProfile = new OPGUpdatePanellistProfile();
            try
            {
                updateProfile = Util.getOPGSDKInstance().updatePanellistProfile(getActivity(),panellistProfile);
            }
            catch (Exception ex)
            {
                updateProfile.setStatusMessage(ex.getMessage());
                Log.i(TAG, ex.getMessage());
            }
            return updateProfile;
        }

        @Override
        protected void onPostExecute(OPGUpdatePanellistProfile opgUpdatePanellistProfile)
        {
            super.onPostExecute(opgUpdatePanellistProfile);
            try
            {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if(opgUpdatePanellistProfile.isSuccess())
                {
                    SaveOPGObjects.storePanellistProfile(panellistProfile);
                    if(panellistProfile.getMediaID().equals("0")) {
                        setDefaultProfileImage();
                    }
                }
                else if(opgUpdatePanellistProfile.getStatusMessage().contains(Util.SESSION_TIME_OUT_ERROR))
                {
                    Util.launchLoginActivity(getActivity());
                    setOriginalValue(panellistProfile);
                }
                else  if(opgUpdatePanellistProfile.getStatusMessage().contains(Util.NO_INTERNET_ERROR))
                {
                    setOriginalValue(panellistProfile);
                    showToast(getString(R.string.no_network_msg)
                    );
                }
                else
                {
                    showToast(getString(R.string.unknown_error));
                    setOriginalValue(panellistProfile);
                }
            }
            catch (Exception ex)
            {
                Log.i(Util.TAG,ex.getMessage());
            }
        }
    }


    private void setDefaultProfileImage() {
        if(imgProfile!=null){
            imgProfile.setImageResource(R.drawable.profile_circle);
        }
    }

    private void setOriginalValue(OPGPanellistProfile panellistProfile)
    {
        txtName.setText(panellistProfile.getFirstName());
        etCountryName.setText(panellistProfile.getCountryName());
    }
    @Override
    public void onDestroy() {
        progressDialog = null;
        selectImageDialog = null;
        mGoogleApiClient = null;
        super.onDestroy();
    }

    private class CompressImage extends AsyncTask<Void,Void,String>{
        String imageURI;
        public CompressImage(String imageURI) {
            this.imageURI = imageURI;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(progressDialog != null) {
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            return compressImage(this.imageURI);
        }

        @Override
        protected void onPostExecute(String currentMediaPath) {
            super.onPostExecute(currentMediaPath);
            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (currentMediaPath != null) {
                    uploadProfileImage(currentMediaPath);
                }
            }catch (Exception e){
                if(BuildConfig.DEBUG)
                    Log.e(getActivity().getClass().getName(),e.toString());
            }

        }
    }

    /**
     * This method returns the image path of the compressed image.
     * @param imageUri
     * @return
     */
    public String compressImage(String imageUri) {
        try
        {
            String filePath = imageUri;
            Bitmap scaledBitmap = null;
            //String mediaFilename = "";
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;
            float maxHeight = 110.0f;
            float maxWidth = 110.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
                bmp = BitmapFactory.decodeFile(filePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2,
                    new Paint(Paint.FILTER_BITMAP_FLAG));

            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);

                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (OutOfMemoryError outOfMemoryError)
            {
                outOfMemoryError.printStackTrace();
            }
            FileOutputStream out = null;
            String filename = null;
            if (isGallery) {
                File vitaccessMediaDir = new File(Environment.getExternalStorageDirectory()+ File.separator + Utils.getApplicationName(getActivity())+ File.separator+tempDir);
                File vitaccessMediaImageDir = new File(vitaccessMediaDir + imagesDir);
                if (!vitaccessMediaImageDir.exists()) {
                    vitaccessMediaImageDir.mkdirs();
                }
                String format = String.format(Locale.ENGLISH,IMAGE_REGEX, 3);
                File saveFile;
                do {
                    String imagefilename = IMAGE_GALLERY_ + String.format(Locale.ENGLISH, format, System.currentTimeMillis() / 1000L) + JPEG;
                    saveFile = new File(vitaccessMediaImageDir, imagefilename);
                } while (saveFile.exists());
                File sourceFile = new File(_currentMediaPath);
                try {
                    filename = copyDirectory(sourceFile, saveFile.getAbsoluteFile()).getAbsolutePath();
                    _currentMediaPath = filename;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                filename = _currentMediaPath;
            }
            try {
                int compressionRate = 95;
                if(FileUtils.getFileSize(_currentMediaPath) > MAX_IMAGE_SIZE){
                    do{
                        out = new FileOutputStream(filename);
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compressionRate, out);
                        compressionRate = (int) (compressionRate *(.95));
                    }while (FileUtils.getFileSize(filename) > MAX_IMAGE_SIZE);
                }
                return _currentMediaPath;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return "";

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
            {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public File copyDirectory(File sourceLocation, File targetLocation) throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException(CANNOT_CREATE_DIR + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {

            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException(CANNOT_CREATE_DIR + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        return targetLocation;
    }

}