package mobile.snu.onoffmap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

/**
 * Created by lhc on 2015-11-07.
 *  This is about the Wifi-Status
 */
public class NetCheckDog extends BroadcastReceiver{
    private final static int WIFI_TYPE = ConnectivityManager.TYPE_WIFI;
    private final static int MOBILE_TYPE = ConnectivityManager.TYPE_MOBILE;

    /// I need to change activitys' view
    private Activity targetActivity;

    public NetCheckDog () {

    }

    public NetCheckDog (Activity activity) {
        targetActivity = activity;
    }

    /**
     * isOnline
     * @param context
     * @return whether phone is connecting with outer wifi
     */
    public static boolean isOnline (Context context) {
        try {
            /// Get connect manager
            ConnectivityManager connManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            /// Get network state, type as Wi-Fi
            NetworkInfo.State wifiState = connManager.getNetworkInfo(WIFI_TYPE).getState();
            /// Check Wifi-Status
            if (wifiState == NetworkInfo.State.CONNECTED
                    || wifiState == NetworkInfo.State.CONNECTING) {
                return true;
            }

            ///Get mobile state, (It means whether phone is connecting with 3G or 4G somethings like
            /// that)
            NetworkInfo.State mobileState = connManager.getNetworkInfo(MOBILE_TYPE).getState();
            /// Check Mobile-Status
            if (mobileState == NetworkInfo.State.CONNECTED
                    || mobileState == NetworkInfo.State.CONNECTING) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    private void changeView(Activity _targetActivity) {
        _targetActivity.setContentView(R.layout.static_maps);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // When the network status is changed now..
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            // If outer network is not connected, change the view as Basic image
            if (!isOnline(context)) {
                changeView(targetActivity);
            }
        }
    }
}
