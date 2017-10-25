package com.opg.my.surveys.lite.Fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.opg.sdk.models.OPGPanel;
import com.opg.my.surveys.lite.R;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OPGPanel} and makes a call to the
 * specified {@linkOnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyOPGPanelRecyclerViewAdapter extends RecyclerView.Adapter<MyOPGPanelRecyclerViewAdapter.ViewHolder> {


private final List<OPGPanel> mValues;
//private final OnListFragmentInteractionListener mListener;

public MyOPGPanelRecyclerViewAdapter(List<OPGPanel> items) {
    mValues = items;
   // mListener = listener;
}

@Override
public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_change_panel, parent, false);
    return new ViewHolder(view);
}

@Override
public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.textView.setText(mValues.get(position).getName());
}

@Override
public int getItemCount() {
    return mValues.size();
}

public class ViewHolder extends RecyclerView.ViewHolder {
    public  ImageView imageView;
    public  TextView textView;
    OPGPanel opgPanel;

    public ViewHolder(View view) {
        super(view);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + textView.getText() + "'";
    }
}
}
