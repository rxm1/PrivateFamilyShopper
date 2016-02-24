package com.meerkats.familyshopper.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListMembers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rez on 28/01/2016.
 */
public class Diagnostics {
    private static final String ShoppingListMembersFilename = "ShoppingListMembersFilename";
    private static final String logTag = "Diagnostics";

    public synchronized static void saveLastSyncedBy(Context context, ShoppingList shoppingList) {
        FSLog.verbose(logTag, "Diagnostics saveLastSyncedBy");

        ShoppingListMembers shoppingListMembers = getShoppingListMembers(context);

        String lastSyncedBy = shoppingList.getLastSyncedBy();
        long lastSeen = shoppingList.getLastSyncedBySeen();
        if (shoppingListMembers.containsMember(lastSyncedBy)) {
            shoppingListMembers.updateMember(lastSyncedBy, lastSeen);
        } else {
            shoppingListMembers.addMember(lastSyncedBy, lastSeen);
        }

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(ShoppingListMembersFilename, Context.MODE_PRIVATE));
            Gson gson = new Gson();
            Type gsonType = new TypeToken<ShoppingListMembers>() {
            }.getType();
            outputStreamWriter.write(gson.toJson(shoppingListMembers, gsonType));
            outputStreamWriter.close();
        } catch (IOException e) {
            FSLog.error(logTag, "Diagnostics saveLastSyncedBy", e);
        }
    }

    private static ShoppingListMembers getShoppingListMembers(Context context){
        FSLog.verbose(logTag, "Diagnostics getShoppingListMembers");

        ShoppingListMembers shoppingListMembers = new ShoppingListMembers();

        StringBuilder text = new StringBuilder();
        File file = new File(context.getFilesDir(), ShoppingListMembersFilename);
        if(!file.exists())
            return shoppingListMembers;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            FSLog.error(logTag, "DataHelper loadGsonFromLocalStorage", e);
            return shoppingListMembers;
        }
        Gson gson = new Gson();
        Type gsonType = new TypeToken<ShoppingListMembers>() {}.getType();
        shoppingListMembers = gson.fromJson(text.toString(), gsonType);

        return shoppingListMembers;
    }

    public static void deleteMembersListFromLocalStorage(Context context){
        FSLog.verbose(logTag, "Diagnostics DeleteMembersListFromLocalStorage");

        File file = new File(context.getFilesDir(), ShoppingListMembersFilename);
        if(file.exists())
            file.delete();
    }
    public static ArrayList<Map<String, String>> getShoppingListMembersArray(Context context) {
        FSLog.verbose(logTag, "Diagnostics getShoppingListMembersArray");

        ArrayList<Map<String, String>> membersArray = new ArrayList<Map<String, String>>();
        ShoppingListMembers shoppingListMembers = getShoppingListMembers(context);

        for (ShoppingListMembers.ShoppingListMember shoppingListMember: shoppingListMembers.getShoppingListMembers()) {
            Map<String, String> memberMap = new HashMap<String, String>();

            Date date=new Date(shoppingListMember.getLastSeen());
            SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss MM/dd/yy");
            String dateText = df2.format(date);

            memberMap.put("Device ID", shoppingListMember.getName());
            memberMap.put("Last Seen", dateText);
            //memberMap.put("Last Seen", Long.toString(shoppingListMember.getLastSeen()));
            membersArray.add(memberMap);
        }

        return membersArray;
    }
}
