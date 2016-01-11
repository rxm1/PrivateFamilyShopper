package com.meerkats.familyshopper.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Rez on 17/12/2015.
 */
public class ShoppingList extends ArrayList<String>{
    Gson gson = new Gson();
    Type gsonType = new TypeToken<InnerShoppingList>() {}.getType();

    private InnerShoppingList innerShoppingList;

    public ShoppingList(String name){
        innerShoppingList = new InnerShoppingList(name);
    }
    public ShoppingList(){
        innerShoppingList = new InnerShoppingList("");
    }

    public String getShoppingListName() {
        return innerShoppingList.shoppingListName;
    }

    @Override
    public boolean add(String item){
        super.add(item);
        innerShoppingList.add(new ShoppingListItem(item));
        return true;
    }
    @Override
    public String remove(int position){
        String item = super.remove(position);
        innerShoppingList.remove(position);
        return item;
    }
    @Override
    public void clear(){
        super.clear();
        innerShoppingList.clear();
    }

    public void setItemCrossedOff(int position){
        ShoppingListItem shoppingListItem = innerShoppingList.getShoppingListItems().get(position);
        shoppingListItem.setIsCrossedOff(!shoppingListItem.isCrossedOff());
        innerShoppingList.setShoppingListItem(position, shoppingListItem);
    }


    public void loadShoppingList(String newGson){
        super.clear();
        if(newGson != null && !newGson.isEmpty())
            innerShoppingList = gson.fromJson(newGson, gsonType);
        else
            innerShoppingList = new InnerShoppingList("");

        for (int i = 0; i < innerShoppingList.shoppingListItems.size(); i++){
            this.add(i, innerShoppingList.shoppingListItems.get(i).getShoppingListItem());
        }
    }
    public String getJson(){
        return gson.toJson(innerShoppingList, gsonType);
    }


    public ShoppingListItem getShoppingListItem(int index){
        return innerShoppingList.shoppingListItems.get(index);
    }

    public void setShoppingListItemEdit(ShoppingListItem shoppingListItem, int position){
        innerShoppingList.setShoppingListItem(position, shoppingListItem);
        this.set(position, shoppingListItem.toString());
    }

    public long getLastUpdated(){return innerShoppingList.getLastModified();}

    private class InnerShoppingList
    {
        private String shoppingListName;
        private ArrayList<ShoppingListItem> shoppingListItems;
        private long lastModified;

        private InnerShoppingList(String shoppingListName)
        {
            shoppingListItems = new ArrayList<ShoppingListItem>();
            this.shoppingListName = shoppingListName;
            lastModified = 0;
        }

        private void add(ShoppingListItem shoppingListItem){
            shoppingListItems.add(shoppingListItem);
            setLastModified();
        }

        private void remove(int postion){
            shoppingListItems.remove(postion);
            setLastModified();
        }

        private void clear(){
            shoppingListItems.clear();
            setLastModified();
        }

        private void setShoppingListItem(int position, ShoppingListItem shoppingListItem){
            shoppingListItems.set(position, shoppingListItem);
            setLastModified();
        }
        public String getShoppingListName(){return shoppingListName;}
        public void setShoppingListName(String shoppingListName){
            this.shoppingListName=shoppingListName;
            setLastModified();
        }
        public ArrayList<ShoppingListItem> getShoppingListItems(){return shoppingListItems;}
        public void setShoppingListItems(ArrayList<ShoppingListItem> shoppingListItems){
            this.shoppingListItems=shoppingListItems;
            setLastModified();
        }
        public long getLastModified(){return lastModified;}
        public void setLastModified(){this.lastModified = new Date().getTime();}

        @Override
        public String toString(){
            String items = "";
            for (int i=0; i < shoppingListItems.size(); i++) {
                items += shoppingListItems.get(i).toString();
            }
            return "ShoppingList [shopping_list_name=" + shoppingListName + ", "
                    + "last_modified=" + lastModified + ", "
                    + items + "]";
        }
    }
}
