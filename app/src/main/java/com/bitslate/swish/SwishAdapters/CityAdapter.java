package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.City;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 20/9/15.
 */
public class CityAdapter extends BaseAdapter {

    Context context;
    ArrayList<City> list;

    public CityAdapter(Context c, ArrayList<City> list) {
        this.context = c;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = LayoutInflater.from(context).inflate(R.layout.airport_list_item, null);
        TextView textView = (TextView)v.findViewById(R.id.airport_name);
        textView.setText(list.get(i).name);
        return v;
    }
}
