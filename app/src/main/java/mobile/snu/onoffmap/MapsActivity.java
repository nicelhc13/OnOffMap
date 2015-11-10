package mobile.snu.onoffmap;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    private static final int GET_PLACE_CODE = 0;

    private GoogleMap mMap;
    private NetCheckDog baDuk;
    private IntentFilter mNetworkStateChangedFilter;

    /// UI
    private Button placeSearchB;
    private Button myPosB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        /// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /// First, create intent filter for grapping network change event
        mNetworkStateChangedFilter = new IntentFilter();
        mNetworkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        baDuk = new NetCheckDog(this);

        /// Instantiate UI
        placeSearchB = (Button) findViewById(R.id.searchB);
        myPosB = (Button) findViewById(R.id.mposB);

        placeSearchB.setOnClickListener(this);
        myPosB.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        this.registerReceiver(baDuk, mNetworkStateChangedFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(baDuk);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.searchB:
                Intent trsIntent = new Intent(this, PlaceSearchActivity.class);
                startActivityForResult(trsIntent, GET_PLACE_CODE);
                //overridePendingTransition(R.anim.callee_move, R.anim.caller_move);
                break;
            case R.id.mposB:
                Toast.makeText(MapsActivity.this, "My position search now!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode) {
            case GET_PLACE_CODE:
                PlaceBean srcPlace = data.getParcelableExtra("srcPlace");
                PlaceBean destPlace = data.getParcelableExtra("destPlace");

                mMap.clear();
                LatLng src = new LatLng(srcPlace.getLatitude(), srcPlace.getLongitude());
                mMap.addMarker(new MarkerOptions().position(src).title("SOURCE"));
                LatLng dest = new LatLng(destPlace.getLatitude(), destPlace.getLongitude());
                mMap.addMarker(new MarkerOptions().position(src).title("DESTINATION"));

                mMap.moveCamera(CameraUpdateFactory.newLatLng(src));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                mMap.animateCamera(zoom);
                break;
        }
    }

}
