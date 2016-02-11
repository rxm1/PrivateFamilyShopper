package com.meerkats.familyshopper.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meerkats.familyshopper.R;
import com.meerkats.familyshopper.model.ShoppingListMembers;

import java.util.ArrayList;

/**
 * Created by Rez on 28/01/2016.
 */
public class DiagnosticsAdapter extends ArrayAdapter<ShoppingListMembers.ShoppingListMember> {
    public DiagnosticsAdapter(Context context, ArrayList<ShoppingListMembers.ShoppingListMember> arrayList) {
        super(context, 0, arrayList);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnostics_list_member, parent, false);
        }
        ShoppingListMembers.ShoppingListMember member = getItem(position);
        TextView name = (TextView)convertView.findViewById(R.id.shoppingListMemberName);
        TextView lastSeen = (TextView)convertView.findViewById(R.id.shoppingListMemberLastSeen);
        name.setText(member.getName());
        lastSeen.setText(Long.toString(member.getLastSeen()));

        return convertView;
    }


}
