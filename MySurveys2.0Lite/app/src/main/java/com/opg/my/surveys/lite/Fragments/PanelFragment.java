package com.opg.my.surveys.lite.Fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opg.my.surveys.lite.FetchDataService;
import com.opg.my.surveys.lite.HomeActivity;
import com.opg.my.surveys.lite.ThemeManager;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.sdk.models.OPGPanel;
import com.opg.my.surveys.lite.R;


import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PanelFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private ProgressDialog pDialog;
    private TextView tvCurrentPanel;
    private TextView tvPanelTitle;
    private List<OPGPanel> panelsList;
    private PanelAdapter panelAdapter;
    private static PanelFragment panelFragment;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PanelFragment() {
    }

    public static PanelFragment newInstance(int columnCount) {
        if(panelFragment == null)
        {
            panelFragment = new PanelFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_COLUMN_COUNT, columnCount);
            panelFragment.setArguments(args);
        }
        return panelFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(true);
        pDialog.setMessage("Loading Panel...");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_change_panel, container,false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_panel);
        tvCurrentPanel = (TextView) view.findViewById(R.id.et_current_panel_name);
        tvPanelTitle = (TextView)view.findViewById(R.id.tv_title_panel);
        Util.setTypeface(getActivity(), tvCurrentPanel,"font/roboto_regular.ttf");
        Util.setTypeface(getActivity(),tvPanelTitle,"font/roboto_regular.ttf");
        return view;
    }

    @Override
    public void onDestroyView() {
        if (panelAdapter != null) {
            panelAdapter.clearAdapterData();
        }
        recyclerView = null;
        tvCurrentPanel = null;
        tvPanelTitle = null;
        panelAdapter = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        panelsList = null;
        panelAdapter = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        setPanelsList();
        IntentFilter iff= new IntentFilter();
        iff.addAction(Util.BROADCAST_ACTION_SAVE_DATA);
        iff.addAction(Util.BROADCAST_ACTION_REFRESH);
        getActivity().registerReceiver(mReceiver, iff);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        if(panelFragment != null) {

            panelFragment.getView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        getFragmentManager().popBackStack();
                        return true;
                    }
                    return false;
                }
            });

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    /**
     * Sets the panels to the recyclerview
     */
    public void setPanelsList(){
        panelsList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        panelAdapter = new PanelAdapter(panelsList);
        recyclerView.setAdapter(panelAdapter);
        refreshPanelList();
        displayCurrentPanel();
    }

    /**
     * Refresh the panels in the recyclerview
     */
    public void refreshPanelList(){
        panelsList = getPanels();
        panelAdapter.swap(panelsList);
    }

    /**
     * Gets the current panelname and displays on the textview
     */
    public void  displayCurrentPanel(){
        if(MySurveysPreference.getCurrentPanelID(getActivity())!=0)
        {
            tvCurrentPanel.setText(MySurveysPreference.getCurrentPanelName(getActivity()));
        }
    }

    /**
     * Gets the list of panels from db
     * @return
     */
    public List<OPGPanel> getPanels(){
        List<OPGPanel> opgPanels = new ArrayList<>();
        if(MySurveysPreference.isDownloaded(getActivity())){
            try {
                opgPanels = RetriveOPGObjects.getPanels();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return opgPanels;
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
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // void onListFragmentInteraction(OP item);
    }

    private class PanelAdapter extends RecyclerView.Adapter<PanelAdapter.PanelViewHolder>
    {
        List<OPGPanel> opgPanelList;
        public PanelAdapter(List<OPGPanel> list)
        {
            this.opgPanelList = list;
        }

        @Override
        public PanelViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panel, parent, false);
            Util.setTypeface(getActivity(),(TextView)itemView.findViewById(R.id.tv_panel_name),"font/roboto_regular.ttf");
            return new PanelViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final PanelViewHolder holder, final int position)
        {
            final OPGPanel opgPanel = opgPanelList.get(position);
            holder.tvPanelTitle.setText(opgPanel.getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MySurveysPreference.setCurrentPanelID(getActivity(),opgPanel.getPanelID());
                    MySurveysPreference.setCurrentPanelName(getActivity(),opgPanel.getName());
                    ThemeManager.getThemeManagerInstance().init(getActivity());
                    ((HomeActivity)getActivity()).updateTheme();
                    if(tvCurrentPanel != null)
                    {
                        tvCurrentPanel.setText(opgPanel.getName());
                    }
                }
            });
            if(opgPanel.isLogoIDSpecified())
            {
                String filePath = Util.searchFile(getActivity(),opgPanel.getLogoID()+"",Util.THEME_PICS);
                if( filePath != null)
                {
                    holder.progressBar.setVisibility(View.GONE);
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    if(bitmap != null )
                    {
                        holder.imgPanelImage.setImageBitmap(bitmap);
                    }
                }
                else
                {
                    if(Util.isOnline(getActivity()))
                    {
                        if( !Util.mediaDownloadingList.contains(opgPanel.getLogoID()))
                        {
                            ((HomeActivity) getActivity()).downloadMedia(new HomeActivity.DownloadMediaListener() {
                                @Override
                                public void onStartDownload()
                                {
                                    holder.progressBar.setVisibility(View.VISIBLE);
                                    Util.mediaDownloadingList.add(opgPanel.getLogoID());
                                }

                                @Override
                                public void onDownloadCompleted(String filePath) {
                                    Util.moveFile(getActivity(),filePath,opgPanel.getLogoID()+"",Util.THEME_PICS);
                                    holder.progressBar.setVisibility(View.GONE);
                                    if(panelsList.size()>position)
                                    {
                                        refreshView();
                                    }
                                }

                                @Override
                                public void onDownloadFailed(String errorMsg)
                                {
                                    holder.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(),errorMsg,Toast.LENGTH_SHORT).show();
                                    if(panelsList.size()>position)
                                    {
                                        refreshView();
                                    }
                                }
                            },opgPanel.getLogoID());
                        }
                        else
                        {
                            holder.progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        holder.imgPanelImage.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.default_logo_panel));
                        holder.progressBar.setVisibility(View.GONE);
                        Util.mediaDownloadingList.clear();
                    }
                }
            }
            else
            {
                holder.imgPanelImage.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.default_logo_panel));
                holder.progressBar.setVisibility(View.GONE);
            }
            if(opgPanel.isMediaIDSpecified())
            {
                String filePath = Util.searchFile(getActivity(),opgPanel.getMediaID()+"",Util.THEME_PICS);
                if( filePath != null)
                {
                    holder.progressBarBg.setVisibility(View.GONE);
                    Drawable drawable = Drawable.createFromPath(filePath);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && drawable != null)
                    {
                        holder.imageLogoContainer.setBackground(drawable);
                    }
                }
                else
                {
                    if(Util.isOnline(getActivity())  )
                    {
                       if (!Util.mediaDownloadingList.contains(opgPanel.getMediaID()))
                       {
                           ((HomeActivity) getActivity()).downloadMedia(new HomeActivity.DownloadMediaListener() {
                               @Override
                               public void onStartDownload()
                               {
                                   holder.progressBarBg.setVisibility(View.VISIBLE);
                                   Util.mediaDownloadingList.add(opgPanel.getMediaID());
                               }

                               @Override
                               public void onDownloadCompleted(String mediaPath) {
                                       Util.moveFile(getActivity(),mediaPath,opgPanel.getMediaID()+"",Util.THEME_PICS);
                                       holder.progressBarBg.setVisibility(View.GONE);
                                   if(panelsList.size()>position)
                                   {
                                       refreshView();
                                   }
                               }

                               @Override
                               public void onDownloadFailed(String errorMsg)
                               {
                                   holder.progressBarBg.setVisibility(View.GONE);
                                   Toast.makeText(getActivity(),errorMsg,Toast.LENGTH_SHORT).show();
                                   if(panelsList.size()>position)
                                   {
                                       refreshView();
                                   }
                               }
                           },opgPanel.getMediaID());
                       }else
                       {
                           holder.progressBarBg.setVisibility(View.VISIBLE);
                       }
                    }
                    else
                    {
                        holder.imageLogoContainer.setBackgroundResource(R.drawable.default_bg_panel);
                        holder.progressBarBg.setVisibility(View.INVISIBLE);
                        Util.mediaDownloadingList.clear();
                    }
                }
            }
            else
            {
                holder.imageLogoContainer.setBackgroundResource(R.drawable.default_bg_panel);
                holder.progressBarBg.setVisibility(View.INVISIBLE);
            }
        }

        private void clearAdapterData()
        {
            panelsList.clear();
            notifyDataSetChanged();
        }
        private void refreshView()
        {
            if(recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE)
            {
                notifyDataSetChanged();
            }
        }
        @Override
        public int getItemCount() {
            return opgPanelList.size();
        }

        public class PanelViewHolder extends RecyclerView.ViewHolder
        {
            TextView tvPanelTitle;
            ImageView imgPanelImage;
            RelativeLayout imageLogoContainer;
            View itemView;
            ProgressBar progressBar;
            ProgressBar progressBarBg;

            public PanelViewHolder(View view)
            {
                super(view);
                tvPanelTitle = (TextView) view.findViewById(R.id.tv_panel_name);
                imgPanelImage = (ImageView) view.findViewById(R.id.img_logo_panel);
                imageLogoContainer = (RelativeLayout)view.findViewById(R.id.image_logo_container);
                itemView = view.findViewById(R.id.item_view_panel);
                progressBar = (ProgressBar) view.findViewById(R.id.progress_panel);
                progressBar.setIndeterminate(true);
                progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())), android.graphics.PorterDuff.Mode.MULTIPLY);
                progressBarBg = (ProgressBar) view.findViewById(R.id.progress_panel_bg);
                progressBarBg.setIndeterminate(true);
                progressBarBg.getIndeterminateDrawable().setColorFilter(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
        }

        public void swap(List<OPGPanel> opgPanels){
            opgPanelList = opgPanels;
            notifyDataSetChanged();
        }

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Util.BROADCAST_ACTION_SAVE_DATA) || intent.getAction().equalsIgnoreCase(Util.BROADCAST_ACTION_REFRESH)) {
                refreshPanelList();
                displayCurrentPanel();
                if (Util.isServiceRunning(getActivity(), FetchDataService.class)) {
                    ((HomeActivity) getActivity()).showSnackBar(getString(R.string.sync_msg), Snackbar.LENGTH_INDEFINITE);
                }else {
                    ((HomeActivity)getActivity()).dismissSnackBar();
                }
            }
        }
    };

    public void popBackStack()
    {
        getFragmentManager().popBackStack();
    }
}
