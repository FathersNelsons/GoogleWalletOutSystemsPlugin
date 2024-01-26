package net.rutabagatechnologies.googlewalletoutsystemsplugin;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.pay.Pay;
import com.google.android.gms.pay.PayClient;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CreateWalletObjectsRequest;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import com.google.android.gms.pay.PayApiAvailabilityStatus;
import com.google.android.gms.wallet.wobs.WalletObjects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleWallet extends CordovaPlugin{
    private static final int ADD_TO_WALLET_REQUEST_CODE = 999;

    private CallbackContext callbackContext;

    private PayClient walletClient;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
        Activity activity = cordova.getActivity();
        walletClient = Pay.getClient(activity);
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        if (action.equals("canAddToWallet")) {
            this.canAddToWallet(args, callbackContext);
            return true;
        }
        if (action.equals("addToWallet")) {
            String memberCardJson = args.getString(0);
            this.addToWallet(memberCardJson, activity, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void canAddToWallet(JSONArray args, CallbackContext callbackContext) {
        walletClient
                .getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                .addOnSuccessListener(status -> {
                    if (status == PayApiAvailabilityStatus.AVAILABLE) {
                        callbackContext.success(1);
                    } else {
                        callbackContext.success(0);
                    }
                })
                .addOnFailureListener(exception -> {
                    callbackContext.error("API availability cannot be verified.");
                });
    }

    private void addToWallet(String memberCardJson, Activity activity, CallbackContext callbackContext) throws JSONException {
        walletClient.savePasses(memberCardJson, activity, ADD_TO_WALLET_REQUEST_CODE);
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode  Result code returned by the Google Pay API.
     * @param data        Intent from the Google Pay API containing payment or error data.
     * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
     * from an Activity</a>
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // value passed in AutoResolveHelper
        if (requestCode != ADD_TO_WALLET_REQUEST_CODE) {
            return;
        }

        switch (resultCode) {

            case Activity.RESULT_OK:
                callbackContext.success(1);
                break;

            case Activity.RESULT_CANCELED:
                callbackContext.error("Payment cancelled");
                break;

            case AutoResolveHelper.RESULT_ERROR:
                Status status = AutoResolveHelper.getStatusFromIntent(data);
                callbackContext.error(status.getStatusMessage());
                break;
        }
    }

}
