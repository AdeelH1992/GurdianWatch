package com.example.lenovossd.gurdianwatch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class customAdapter extends BaseAdapter {
    ArrayList<HashMap<String, String>> al = new ArrayList<HashMap<String,String>>();
    Activity activity;
    private static LayoutInflater inflater = null;
    public customAdapter(Activity a, ArrayList<HashMap<String, String>> al) {
        Log.e("check", "");
        activity = a;
        this.al = al;
        inflater = (LayoutInflater)activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public int getCount() {
       return al.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
        {
           // vi = inflater.inflate(R.layout.row_layout, null);
        }
      //  TextView name = (TextView) vi.findViewById(R.id.Name);
       // TextView number = (TextView) vi.findViewById(R.id.Number);

        HashMap<String, String> contains = new HashMap<String, String>();
        contains = al.get(position);
        Log.e( "contains",contains.toString() );

        Log.d("value check:----", contains.get("Name"));
       //name.setText(contains.get("Name"));
      //  number.setText(contains.get("Number"));

        return vi;

    }
}
