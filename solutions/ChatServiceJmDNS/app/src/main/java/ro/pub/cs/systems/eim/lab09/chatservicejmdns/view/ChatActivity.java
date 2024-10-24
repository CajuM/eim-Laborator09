package ro.pub.cs.systems.eim.lab09.chatservicejmdns.view;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import ro.pub.cs.systems.eim.lab09.chatservicejmdns.R;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.model.NetworkService;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations.NetworkServiceDiscoveryOperations;

public class ChatActivity extends AppCompatActivity {

    private NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations = null;

    private boolean serviceRegistrationStatus = false;
    private boolean serviceDiscoveryStatus = false;

    private ArrayList<NetworkService> discoveredServices = null;
    private ArrayList<NetworkService> conversations = null;

    private Handler handler = null;

    private WifiManager.MulticastLock multicastLock = null;
    private WifiManager wifiManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "onCreate() callback method was invoked!");
        setContentView(R.layout.activity_chat);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifiManager.createMulticastLock(Constants.TAG);
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        discoveredServices = new ArrayList<>();
        conversations = new ArrayList<>();


        setHandler(new Handler());

        setNetworkServiceDiscoveryOperations(new NetworkServiceDiscoveryOperations(this));
        setChatNetworkServiceFragment(new ChatNetworkServiceFragment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(Constants.TAG, "onResume() callback method was invoked!");
        if (networkServiceDiscoveryOperations != null) {
            if (serviceDiscoveryStatus) {
                networkServiceDiscoveryOperations.startNetworkServiceDiscovery();
                if (getChatNetworkServiceFragment() != null) {
                    getChatNetworkServiceFragment().startServiceDiscovery();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        Log.i(Constants.TAG, "onPause() callback method was invoked!");
        if (networkServiceDiscoveryOperations != null) {
            if (serviceDiscoveryStatus) {
                networkServiceDiscoveryOperations.stopNetworkServiceDiscovery();
                if (getChatNetworkServiceFragment() != null) {
                    getChatNetworkServiceFragment().stopServiceDiscovery();
                }
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "onDestroy() callback method was invoked!");
        if (networkServiceDiscoveryOperations != null) {
            if (serviceDiscoveryStatus) {
                serviceDiscoveryStatus = false;
                networkServiceDiscoveryOperations.stopNetworkServiceDiscovery();
                if (getChatNetworkServiceFragment() != null) {
                    getChatNetworkServiceFragment().stopServiceDiscovery();
                }
            }
            if (serviceRegistrationStatus) {
                serviceDiscoveryStatus = false;
                networkServiceDiscoveryOperations.unregisterNetworkService();
                if (getChatNetworkServiceFragment() != null) {
                    getChatNetworkServiceFragment().stopServiceRegistration();
                }
            }
            networkServiceDiscoveryOperations.closeJmDNS();
        }

        if (multicastLock != null) {
            multicastLock.release();
        }

        super.onDestroy();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setNetworkServiceDiscoveryOperations(NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations) {
        this.networkServiceDiscoveryOperations = networkServiceDiscoveryOperations;
    }

    public NetworkServiceDiscoveryOperations getNetworkServiceDiscoveryOperations() {
        return networkServiceDiscoveryOperations;
    }

    public void setChatNetworkServiceFragment(Fragment chatNetworkServiceFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container_frame_layout, chatNetworkServiceFragment, Constants.FRAGMENT_TAG)
                .commit();
    }

    public ChatNetworkServiceFragment getChatNetworkServiceFragment() {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag(Constants.FRAGMENT_TAG);
        if (fragment instanceof  ChatNetworkServiceFragment) {
            return (ChatNetworkServiceFragment)fragment;
        }
        return null;
    }

    public void setServiceRegistrationStatus(boolean serviceRegistrationStatus) {
        this.serviceRegistrationStatus = serviceRegistrationStatus;
    }

    public boolean getServiceRegistrationStatus() {
        return serviceRegistrationStatus;
    }

    public void setServiceDiscoveryStatus(boolean serviceDiscoveryStatus) {
        this.serviceDiscoveryStatus = serviceDiscoveryStatus;
    }

    public boolean getServiceDiscoveryStatus() {
        return serviceDiscoveryStatus;
    }

    public void setDiscoveredServices(final ArrayList<NetworkService> discoveredServices) {
        this.discoveredServices = discoveredServices;
        handler.post(() -> {
            ChatNetworkServiceFragment chatNetworkServiceFragment = getChatNetworkServiceFragment();
            if (chatNetworkServiceFragment != null && chatNetworkServiceFragment.isVisible()) {
                chatNetworkServiceFragment.getDiscoveredServicesAdapter().setData(discoveredServices);
                chatNetworkServiceFragment.getDiscoveredServicesAdapter().notifyDataSetChanged();
            }
        });
    }

    public ArrayList<NetworkService> getDiscoveredServices() {
        return discoveredServices;
    }

    public void setConversations(final ArrayList<NetworkService> conversations) {
        this.conversations = conversations;
        handler.post(() -> {
            ChatNetworkServiceFragment chatNetworkServiceFragment = getChatNetworkServiceFragment();
            if (chatNetworkServiceFragment != null && chatNetworkServiceFragment.isVisible()) {
                chatNetworkServiceFragment.getConversationsAdapter().setData(conversations);
                chatNetworkServiceFragment.getConversationsAdapter().notifyDataSetChanged();
            }
        });
    }

    public ArrayList<NetworkService> getConversations() {
        return conversations;
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }
}
