package com.meerkats.familyshopper.model;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Rez on 28/01/2016.
 */
public class ShoppingListMembers {
    private String shoppingListName="";
    private ArrayList<ShoppingListMember> shoppingListMembers=new ArrayList<ShoppingListMember>();


    public class ShoppingListMember{
        private String name;
        private long lastSeen;

        public ShoppingListMember(String name, long lastSeen){
            this.name = name;
            this.lastSeen = lastSeen;
        }
        @Override
        public String toString(){
            return "ShoppingListMember [name=" + name + ", "
                    + "last_seen=" + lastSeen + "]";
        }
        public String getName(){return name;}
        public long getLastSeen(){return lastSeen;}
    }

    public void addMember(String name, long lastSeen){
        shoppingListMembers.add(new ShoppingListMember(name, lastSeen));
    }

    public boolean containsMember(String name){
        for (ShoppingListMember shoppingListMember:shoppingListMembers) {
            if (shoppingListMember.name.equals(name))
                return true;
        }
        return false;
    }

    public void updateMember(String name, long lastSeen){
        for (int i=0; i<shoppingListMembers.size(); i++) {
            if (shoppingListMembers.get(i).name.equals(name)) {
                ShoppingListMember member = shoppingListMembers.get(i);
                member.lastSeen = lastSeen;
                shoppingListMembers.set(i, member);
            }
        }
    }

    @Override
    public String toString(){
        String members = "";
        for (int i=0; i < shoppingListMembers.size(); i++) {
            members += shoppingListMembers.get(i).toString();
        }
        return "ShoppingListMembers [shopping_list_name=" + shoppingListName + ", "
                + members + "]";

    }

    public ArrayList<ShoppingListMember> getShoppingListMembers(){return shoppingListMembers;}
}

