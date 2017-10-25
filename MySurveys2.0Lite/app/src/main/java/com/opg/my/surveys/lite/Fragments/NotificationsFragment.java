package com.opg.my.surveys.lite.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.opg.my.surveys.lite.DividerItemDecoration;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.my.surveys.lite.common.db.UpdateOPGObjects;
import com.opg.prom.model.AppNotification;
import com.opg.my.surveys.lite.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link NotificationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationsFragment extends Fragment implements View.OnClickListener
{
    private RecyclerView recyclerView;
    private TextView btnDelete;
    private TextView tvNoNotification;
    private TextView tvNotificationTitle;
    private NotificationAdapter adapter;
    GestureDetectorCompat gestureDetector;
    private boolean deleteEnabled = false;
    private List<Long> selectedItemIDs;
    LinearLayoutManager layoutManager;

    private static NotificationsFragment notificationsFragment;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    public static NotificationsFragment newInstance() {
        notificationsFragment = null;
        notificationsFragment = new NotificationsFragment();
        return notificationsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedItemIDs = new ArrayList<>();
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter iff= new IntentFilter();
        iff.addAction(Util.BROADCAST_ACTION_NOTIFICATION);
        iff.addAction(Util.BROADCAST_ACTION_REFRESH);
        iff.addAction(Util.BROADCAST_ACTION_SAVE_DATA);
        getActivity().registerReceiver(mReceiver, iff);
    }


    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        btnDelete =(TextView) view.findViewById(R.id.btn_delete);
        btnDelete.setVisibility(View.INVISIBLE);
        btnDelete.setOnClickListener(this);
        recyclerView = (RecyclerView)view.findViewById(R.id.notification_recycler_view);
        tvNoNotification =  ((TextView)view.findViewById(R.id.tv_no_notification));
        tvNotificationTitle = (TextView)view.findViewById(R.id.tv_title_notification);
        Util.setTypeface(getActivity(),tvNoNotification,"font/roboto_regular.ttf");
        Util.setTypeface(getActivity(),tvNotificationTitle,"font/roboto_regular.ttf");
        onCreateView();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        btnDelete= null;
        recyclerView= null;
        tvNoNotification = null;
        tvNotificationTitle= null;
        adapter = null;
        layoutManager = null;
        selectedItemIDs.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        selectedItemIDs = null;
    }

    private void onCreateView()
    {
        try
        {
            List<AppNotification> notificationList = getNotifications() ;
            if(notificationList.size()>0)
            {
                tvNoNotification.setVisibility(View.GONE);
                adapter = new NotificationAdapter(notificationList);
                recyclerView.setAdapter(adapter);
                layoutManager = new LinearLayoutManager(getActivity());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                // you can set the first visible item like this:
                layoutManager.scrollToPosition(0);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.divider));
                //Swipe to Delete
                ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
                    {
                        adapter.remove(viewHolder.getAdapterPosition());
                    }

                });
                swipeToDismissTouchHelper.attachToRecyclerView(recyclerView);
            }
            else
            {
                tvNoNotification.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception ex)
        {
            Log.i(Util.TAG,ex.getMessage());
        }
    }


    private List<AppNotification> getNotifications() throws Exception
    {
        List<AppNotification> arrayList = new ArrayList<>();
        if(MySurveysPreference.isDownloaded(getActivity())){
            arrayList = RetriveOPGObjects.getNotificationList();
        }
        return arrayList;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Util.BROADCAST_ACTION_NOTIFICATION) || intent.getAction().equals(Util.BROADCAST_ACTION_SAVE_DATA)
                    || intent.getAction().equals(Util.BROADCAST_ACTION_REFRESH))
            {
                onCreateView();
            }
        }
    };

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_delete :
                adapter.removeItems();
                break;
        }
    }


    private  class NotificationAdapter extends RecyclerView.Adapter<NotificationsFragment.NotificationAdapter.NotificationViewHolder>
    {
        List<AppNotification> notificationList;

        protected NotificationAdapter(List<AppNotification> notificationList)
        {
            if(notificationList == null)
            {
                throw new IllegalArgumentException("notification List can not be null.");
            }
            this.notificationList = notificationList;
            selectedItemIDs.clear();
        }

        @Override
        public NotificationsFragment.NotificationAdapter.NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new NotificationsFragment.NotificationAdapter.NotificationViewHolder(itemView);
        }

        private void removeItems()
        {
            if(selectedItemIDs.size() > 0)
            {
                for (int i = selectedItemIDs.size() - 1;i >= 0;i--)
                {

                    adapter.removeData(selectedItemIDs.get(i));

                }
            }
            selectedItemIDs.clear();
            btnDelete.setVisibility(View.GONE);
            deleteEnabled = false;
            onCreateView();
        }
        public void remove(int position) {
            try
            {
                AppNotification appNotification = notificationList.get(position);
                notificationList.remove(position);
                notifyItemRemoved(position);
                UpdateOPGObjects.deleteNotification(appNotification.getAppNotificationID());
                recyclerView.requestLayout();
                if(notificationList.size() == 0)
                {
                    tvNoNotification.setVisibility(View.VISIBLE);
                }
            }catch (Exception ex)
            {
                Log.i(Util.TAG,ex.getMessage());
            }

        }
        public void removeData(long notificationID)
        {
            try
            {
                UpdateOPGObjects.deleteNotification(notificationID);
                swap(RetriveOPGObjects.getNotificationList());
            }
            catch (Exception ex)
            {
                Log.i(Util.TAG,ex.getMessage());
            }

        }



        @Override
        public void onBindViewHolder(final NotificationViewHolder holder, final int position)
        {
            try
            {
                final AppNotification appNotification = notificationList.get(position);
                String title = appNotification.getTitle().split("~")[0];
                holder.title.setText(title);
                int textColor ;
                if(appNotification.getIsRead()){
                    textColor = Util.getColor(getActivity(),R.color.primary_text_color);
                }else{
                    textColor = Util.getColor(getActivity(),android.R.color.black);
                }
                holder.title.setTextColor(textColor);

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        try
                        {
                            if(deleteEnabled){
                                try
                                {
                                    if(selectedItemIDs.size() > 0 && selectedItemIDs.contains(appNotification.getAppNotificationID()))
                                    {
                                        //Deselect
                                        selectedItemIDs.remove(appNotification.getAppNotificationID());
                                    }
                                    else
                                    {
                                        //Select
                                        selectedItemIDs.add(appNotification.getAppNotificationID());
                                    }
                                }catch (Exception ex)
                                {
                                    Log.i(Util.TAG,ex.getMessage());
                                }
                                notifyDataSetChanged();
                            }else {
                                try {
                                    FragmentManager fragmentManager = getFragmentManager();
                                    FragmentTransaction ft = fragmentManager.beginTransaction();
                                    ft.replace(R.id.root_frame, NotificationDetailFragment.newInstance(appNotification.getAppNotificationID(),appNotification.getType() + "", appNotification.getBody(),
                                            ((appNotification.getTitle().contains("~")) ?(appNotification.getTitle().split("~")[1]):"")));
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                    ft.addToBackStack(null);
                                    ft.commit();
                                }catch (Exception ex)
                                {
                                  Log.i(NotificationsFragment.class.getName(),ex.getMessage());
                                }

                            }
                        }
                        catch (Exception ex)
                        {
                            Log.i(Util.TAG,ex.getMessage());
                        }

                    }
                });


                holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view)
                    {
                        if(!deleteEnabled){
                            deleteEnabled = true;
                            /*selectedItemPositions.clear();*/
                            selectedItemIDs.clear();
                        }
                        try
                        {
                            if(selectedItemIDs.size() > 0 && selectedItemIDs.contains(appNotification.getAppNotificationID()))
                            {
                                //Deselect
                                selectedItemIDs.remove(appNotification.getAppNotificationID());
                            }
                            else
                            {
                                //Select
                                selectedItemIDs.add(appNotification.getAppNotificationID());
                            }
                            notifyDataSetChanged();
                        }catch (Exception ex)
                        {
                            Log.i(Util.TAG,ex.getMessage());
                        }
                        return true;
                    }
                });


                try
                {
                    Bitmap selectedBitmap;
                    if(selectedItemIDs.size() > 0 && selectedItemIDs.contains(appNotification.getAppNotificationID()) && deleteEnabled)
                    {
                        selectedBitmap = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.notification_selection);
                        holder.view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.selected_item));
                    }
                    else
                    {
                        holder.view.setBackgroundColor(Color.TRANSPARENT);
                        switch (notificationList.get(position).getType())
                        {
                            //Notification Reminder
                            case  100 :
                                selectedBitmap = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.icon_approx_time);
                                break;
                            //Notification  alert/others
                            case  101 :
                                selectedBitmap = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.notif_others_icon);
                                break;
                            case  102 :
                                selectedBitmap = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.notif_others_icon);
                                break;
                            default:
                                selectedBitmap = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.notif_others_icon);
                                break;
                        }
                    }
                    holder.imageView.setImageBitmap(selectedBitmap);
                    if( selectedItemIDs.size() > 0)
                    {
                        btnDelete.setVisibility(View.VISIBLE);
                        deleteEnabled = true;
                    }else {
                        btnDelete.setVisibility(View.INVISIBLE);
                        deleteEnabled = false;

                    }
                }catch (Exception ex)
                {
                    Log.i(Util.TAG,ex.getMessage());
                }

            }catch (Exception ex)
            {
                Log.i(Util.TAG,ex.getMessage());
            }

        }

        public void swap(List<AppNotification> appNotifications){
            notificationList = appNotifications;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return notificationList.size();
        }

        protected class NotificationViewHolder extends RecyclerView.ViewHolder{
            TextView title;
            ImageView imageView;
            View view ;
            public NotificationViewHolder(View itemView)
            {
                super(itemView);
                title = (TextView)itemView.findViewById(R.id.notification_title_tv);
                imageView = (ImageView) itemView.findViewById(R.id.notification_image_view);
                view = itemView;
                Util.setTypeface(getActivity(),title,"font/roboto_regular.ttf");
            }
        }
    }


    @Override
    public boolean getUserVisibleHint() {
        return super.getUserVisibleHint();
    }

    public boolean resetDelete(){
        if(deleteEnabled){
            selectedItemIDs.clear();
            btnDelete.setVisibility(View.GONE);
            deleteEnabled = false;
            return false;
        }else{
            return true;
        }
    }

}
