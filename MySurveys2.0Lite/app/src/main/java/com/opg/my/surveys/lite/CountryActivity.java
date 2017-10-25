package com.opg.my.surveys.lite;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.sdk.models.OPGCountry;
import java.util.ArrayList;

public class CountryActivity extends RootActivity {

    private RecyclerView recyclerView;
    private CountryAdapter countryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country);
        if(!Util.isTablet(this)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(this))));

        try
        {
            ArrayList<OPGCountry> countryList = (ArrayList<OPGCountry>) RetriveOPGObjects.getCountries();
            setTitle(getString(R.string.title_country_page));
            recyclerView = (RecyclerView) findViewById(R.id.country_recycler_view) ;
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addItemDecoration(new DividerItemDecoration(this,R.drawable.divider));
            countryAdapter = new CountryAdapter(countryList);
            recyclerView.setAdapter(countryAdapter);
        }
        catch (Exception ex)
        {
            if(BuildConfig.DEBUG) {
                Log.i(Util.TAG, ex.getMessage());
            }
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_country,menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_action));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                countryAdapter.getFilter().filter(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    private class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CountryViewHolder> implements Filterable
    {
        ArrayList<OPGCountry> countries;
        ArrayList<OPGCountry>filteredCountries;
        public  class CountryViewHolder extends RecyclerView.ViewHolder
        {
            TextView tvCountryName;
            LinearLayout containerCountryItem;
            public CountryViewHolder(View view)
            {
                super(view);
                tvCountryName = (TextView) view.findViewById(R.id.tv_country_name);
                containerCountryItem = (LinearLayout)view.findViewById(R.id.container_country_item);
            }
        }

        public CountryAdapter(ArrayList<OPGCountry> countries)
        {
            this.countries = countries;
            filteredCountries = countries;
        }

        @Override
        public CountryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country, parent, false);
            return   new CountryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CountryViewHolder holder, int position)
        {
            final OPGCountry opgCountry = filteredCountries.get(position);
            holder.tvCountryName.setText(opgCountry.getCountryName());
            holder.containerCountryItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent();
                    intent.putExtra("CountryName",opgCountry.getCountryName());
                    intent.putExtra("CountrySTD",opgCountry.getStd());
                    setResult(RESULT_OK,intent);
                    finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return filteredCountries.size();
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String countryName = constraint.toString();
                    if(countryName.isEmpty())
                    {
                        filteredCountries  = countries;
                    }
                    else
                    {
                        ArrayList<OPGCountry> filteredList = new ArrayList<>();
                        for (OPGCountry opgCountry : countries)
                        {
                            if(opgCountry.getCountryName().toLowerCase().contains(countryName.toLowerCase()))
                            {
                               filteredList.add(opgCountry);
                            }
                        }
                        filteredCountries = filteredList;
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filteredCountries;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredCountries = (ArrayList<OPGCountry>) results.values;
                    notifyDataSetChanged();
                }
            };
            return filter;
        }
    }

    @Override
    protected void onDestroy() {
        recyclerView = null;
        super.onDestroy();
    }
}
