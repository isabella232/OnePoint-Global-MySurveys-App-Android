package com.opg.my.surveys.lite.Fragments;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.common.MySurveysPreference;

import java.util.Locale;

import static com.opg.my.surveys.lite.common.Util.ABOUT_US_URL;
import static com.opg.my.surveys.lite.common.Util.PRIVACY_URL;
import static com.opg.my.surveys.lite.common.Util.TERMS_CONDITION_URL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WebViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebViewFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private int mParam1;
   private static  WebViewFragment fragment;
    WebView webView =null;


    public WebViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment WebViewFragment.
     */
    public static WebViewFragment newInstance(int param1) {
        if(fragment == null)
        {
            fragment = new WebViewFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_PARAM1, param1);
            fragment.setArguments(args);
        }
        else
        {
            fragment.getArguments().putInt(ARG_PARAM1, param1);
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
        }
        View view =  inflater.inflate(R.layout.fragment_web_view, container, false);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar_web_view);
        progressBar.setIndeterminate(true);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())), android.graphics.PorterDuff.Mode.MULTIPLY);
        webView =(WebView) view.findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl(getUrl(mParam1));

        webView.setWebViewClient( new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_UP )
                {
                    WebBackForwardList wbfl = webView.copyBackForwardList();
                    if(wbfl.getSize() >1 && webView.canGoBack())
                    {
                        webView.goBack();
                    }
                    else
                    {
                        getFragmentManager().popBackStack();
                    }
                    return true;
                }

                return false;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(fragment != null)
        {
            getView().setFocusableInTouchMode(true);
            getView().requestFocus();
            fragment.getView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK)
                    {
                        WebBackForwardList wbfl = webView.copyBackForwardList();
                        if(wbfl.getSize() >1 && webView.canGoBack())
                        {
                            webView.goBack();
                        }
                        else
                        {
                            getFragmentManager().popBackStack();
                        }

                        return true;
                    }
                    return false;
                }

            });

        }
    }

    private String getUrl(final int condition)
    {
      switch (condition)
      {
          //privacy
          case 1 : return PRIVACY_URL+getLocale();
          //Terms&Condition
          case 2 : return TERMS_CONDITION_URL+getLocale();
          //AboutUs
          case 3 : return ABOUT_US_URL+getLocale();
          default:return  "http://www.onepointglobal.com/";
      }
    }
    //finding the current locale of device
    private  String getLocale()
    {
        String locale = Locale.getDefault().toString().toLowerCase();
        locale = locale.replace("_","-");
        return locale;
    }

}
