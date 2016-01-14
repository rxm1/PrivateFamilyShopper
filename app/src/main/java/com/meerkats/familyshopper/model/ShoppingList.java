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

    public synchronized String getShoppingListName() {
        return innerShoppingList.shoppingListName;
    }

    @Override
    public synchronized boolean add(String item){
        super.add(item);
        innerShoppingList.add(new ShoppingListItem(item));
        return true;
    }
    public synchronized void add(ShoppingListItem shoppingListItem){
        super.add(shoppingListItem.getShoppingListItem());
        innerShoppingList.add(shoppingListItem);
    }

    @Override
    public synchronized String remove(int position){
        String item = super.remove(position);
        innerShoppingList.remove(position);
        return item;
    }
    public synchronized void markAsDeleted(int position){
        super.remove(position);
        innerShoppingList.markAsDeleted(position);
    }
    @Override
    public synchronized void clear(){
        super.clear();
        for (int i=0; i<innerShoppingList.getShoppingListItems().size(); i++) {
            innerShoppingList.markAsDeleted(i);
        }
    }
    public synchronized void clearCrossedOff(){
        for (int i = 0; i < innerShoppingList.getShoppingListItems().size(); i++) {
            ShoppingListItem listItem = innerShoppingList.getShoppingListItems().get(i);
            if(listItem.isCrossedOff()) {
                innerShoppingList.markAsDeleted(i);
                super.remove(i);
            }
        }
    }
    public synchronized void setItemCrossedOff(int position){
        ShoppingListItem shoppingListItem = innerShoppingList.getShoppingListItems().get(position);
        shoppingListItem.setIsCrossedOff(!shoppingListItem.isCrossedOff());
        innerShoppingList.setShoppingListItem(position, shoppingListItem);
    }


    public synchronized void loadShoppingList(String newGson){
            super.clear();
            if (newGson != null && !newGson.isEmpty())
                innerShoppingList = gson.fromJson(newGson, gsonType);
            else
                innerShoppingList = new InnerShoppingList("");

            int index = 0;
            for (int i = 0; i < innerShoppingList.shoppingListItems.size(); i++) {
                if (!innerShoppingList.shoppingListItems.get(i).getIsDeleted()) {
                    this.add(index, innerShoppingList.shoppingListItems.get(i).getShoppingListItem());
                    index++;
                }
        }
    }
    public synchronized String getJson(){
        return gson.toJson(innerShoppingList, gsonType);
    }


    public synchronized ShoppingListItem getShoppingListItem(int index){
        return innerShoppingList.shoppingListItems.get(index);
    }
    public synchronized ArrayList<ShoppingListItem> getShoppingListItems(){
        return innerShoppingList.getShoppingListItems();
    }

    public synchronized void setShoppingListItemEdit(ShoppingListItem shoppingListItem, int position){
        innerShoppingList.setShoppingListItem(position, shoppingListItem);
        this.set(position, shoppingListItem.toString());
    }

    public synchronized long getLastUpdated(){return innerShoppingList.getLastModified();}

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

        private void markAsDeleted(int postion){
            ShoppingListItem shoppingListItem = shoppingListItems.get(postion);
            shoppingListItem.setIsDeleted(true);
            shoppingListItems.set(postion, shoppingListItem);
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
        private String getShoppingListName(){return shoppingListName;}
        private void setShoppingListName(String shoppingListName){
            this.shoppingListName=shoppingListName;
            setLastModified();
        }
        private ArrayList<ShoppingListItem> getShoppingListItems(){return shoppingListItems;}
        private void setShoppingListItems(ArrayList<ShoppingListItem> shoppingListItems){
            this.shoppingListItems=shoppingListItems;
            setLastModified();
        }
        private long getLastModified(){return lastModified;}
        private void setLastModified(){this.lastModified = new Date().getTime();}

        @Override
        public synchronized String toString(){
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
