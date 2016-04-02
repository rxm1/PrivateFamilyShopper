package com.meerkats.familyshopper;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.meerkats.familyshopper.Settings.Settings;
import com.meerkats.familyshopper.dialogs.EditShoppingItemDialog;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.ISynchronizeInterface;


/**
 * Created by Rez on 07/01/2016.
 */
public class MainController implements ISynchronizeInterface {
    Activity activity;
    DataHelper dataHelper;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    public static final String master_shopping_list_name = "Master_List";
    public String log_tag = "";


    HandlerThread handlerThread;
    Handler mainUIHandler;
    Handler mainControllerHandler;
    MainService mainService;
    boolean mainServiceBound = false;

    public MainController(Activity mainActivity, HandlerThread handlerThread, String log_tag) {
        FSLog.verbose(log_tag, "MainController constructor");

        this.activity = mainActivity;
        Firebase.setAndroidContext(activity);
        shoppingList = new ShoppingList("mainList");
        dataHelper = new DataHelper(mainActivity, log_tag);
        this.handlerThread = handlerThread;
        this.log_tag = log_tag;
    }

    public void init(){
        FSLog.verbose(log_tag, "MainController init");

        mainUIHandler = new Handler(Looper.getMainLooper());
        mainControllerHandler = new Handler(handlerThread.getLooper());

        shoppingList = new ShoppingList(master_shopping_list_name);
        shoppingListAdapter = new ShoppingListAdapter(activity, shoppingList);
        shoppingListAdapter.notifyDataSetChanged();
        connect(false);
    }

    public void deleteShoppingListItem(int position){
        FSLog.verbose(log_tag, "MainController deleteShoppingListItem");

        shoppingList.markAsDeleted(position);
        shoppingListAdapter.notifyDataSetChanged();
        sync(false, true);
        ((MainActivity) activity).setIsEditing(false);
    }
    public void editShoppingListItem(final int position, final Activity activity){
        FSLog.verbose(log_tag, "MainController editShoppingListItem");

        final ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(position);
        EditShoppingItemDialog cdd=new EditShoppingItemDialog(activity, shoppingListItem.getShoppingListItem());

        cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (((EditShoppingItemDialog) dialog).isCanceled()) {
                    ((MainActivity) activity).setIsEditing(false);
                    loadLocalShoppingList();
                    return;
                }


                String newData = ((EditShoppingItemDialog) dialog).getNewData();
                shoppingListItem.setShoppingListItem(newData);
                shoppingList.setShoppingListItemEdit(shoppingListItem, position);
                shoppingListAdapter.notifyDataSetChanged();
                sync(false, true);

                mainUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) activity).setIsEditing(false);
                        ((MainActivity) activity).shoppingListView.setSelection(position);
                    }
                });

            }
        });
        cdd.show();
    }
    public void crossOffShoppingItem(int position){
        FSLog.verbose(log_tag, "MainController crossOffShoppingItem");

        shoppingList.setItemCrossedOff(position);
        shoppingListAdapter.notifyDataSetChanged();
        sync(false, true);
    }
    public void addItemToShoppingList(String item){
        FSLog.verbose(log_tag, "MainController addItemToShoppingList");

        shoppingList.add(item);
        shoppingListAdapter.notifyDataSetChanged();
        sync(false, true);
    }
    public void clearShoppingList(){
        FSLog.verbose(log_tag, "MainController clearShoppingList");

        shoppingList.deleteAll();
        shoppingListAdapter.notifyDataSetChanged();
        sync(false, true);
    }
    public void clearCrossedOffShoppingList(){
        FSLog.verbose(log_tag, "MainController clearCrossedOffShoppingList");

        shoppingList.clearCrossedOff();
        shoppingListAdapter.notifyDataSetChanged();
        sync(false, true);
    }

    public synchronized void sync(final boolean showConnectionStatus, boolean saveShoppingList){
        FSLog.verbose(log_tag, "MainController sync");

        if(saveShoppingList)
            dataHelper.saveGsonToLocalStorage(shoppingList.getJson());

        if(mainServiceBound && mainService.postIsValidFirebaseConnection()) {
                mainService.postSyncTaskFromActivity(shoppingList, MainController.this, activity);
        }
        else{
            if(showConnectionStatus) {
                mainUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity.getApplicationContext(), "Firebase not connected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public void connect(boolean reconnect){
        FSLog.verbose(log_tag, "MainController connect");

        if(mainServiceBound) {
            if(reconnect)
                mainService.postReconnectToFirebaseTaskFromActivity(activity);
            else
                mainService.postConnectToFirebaseTaskFromActivity();
        }
    }

    public void disconnect(){
        FSLog.verbose(log_tag, "MainController disconnect");

        if(mainServiceBound) {
            mainService.postDisconnectFromFirebaseTaskFromActivity(activity);
        }
    }

    public void loadSettings(){
        Settings.loadSettings(activity, log_tag);
        if(mainServiceBound) {
            mainService.postLoadSettingsFromActivity(activity);
        }
    }
    public synchronized void loadLocalShoppingList(){
        FSLog.verbose(log_tag, "MainController loadLocalShoppingList");
        if(((MainActivity)activity).isEditing()) {
            return;
        }

        mainControllerHandler.post(new Runnable() {
            @Override
            public void run() {
                final String localFile = dataHelper.loadGsonFromLocalStorage().trim();
                if (!localFile.isEmpty()) {
                    mainUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            shoppingList.loadShoppingList(localFile, log_tag);
                            shoppingListAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        });
    }

    public ShoppingList getShoppingList(){
        FSLog.verbose(log_tag, "MainController getShoppingList");

        return shoppingList;
    }
    public ShoppingListAdapter getShoppingListAdapter(){
        FSLog.verbose(log_tag, "MainController getShoppingListAdapter");

        return shoppingListAdapter;
    }

    public void notifyFileChanged(NotificationEvents occuredNotifications, ShoppingList mergedList){
        FSLog.verbose(log_tag, "MainController notifyFileChanged");

        ((MainActivity)activity).setIsEditing(false);
        loadLocalShoppingList();
    }

    public void setMainService(MainService mainService, boolean bound){
        FSLog.verbose(log_tag, "MainController setMainService");

        this.mainService = mainService;
        this.mainServiceBound = bound;
    }

    public void postSynchronize(){
        FSLog.verbose(log_tag, "MainController postSynchronize");

        ((MainActivity)activity).setIsEditing(false);
        loadLocalShoppingList();
    }

    public void firebaseConnected(){
        FSLog.verbose(log_tag, "MainController firebaseConnected");

        if(mainServiceBound && mainService.postIsValidFirebaseConnection()){
            mainService.postSyncTaskFromActivity(shoppingList, MainController.this, activity);
            mainUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mainService.postIsValidFirebaseConnection())
                        Toast.makeText(activity.getApplicationContext(), "Firebase connected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void cleanUp(){
        FSLog.verbose(log_tag, "MainController cleanUp");

        handlerThread.quit();
        dataHelper.cleanUp();
    }

}
