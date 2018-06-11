package br.com.roadmaps.mapapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.android.gms.ads.MobileAds;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import org.json.JSONArray;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final int REQUEST_PLACE_PICKER = 2;
    protected LocationManager locationManager;
    public GoogleMap mGoogleMap;
    private SupportMapFragment map;
    private static final int INITIAL_REQUEST = 200;
    public Location mAtual;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView mAdView;
    private Network net;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);
        loadMap();
        net = new Network(MainActivity.this);

        //ca-app-pub-39chave Ads

        MobileAds.initialize(this, "Chave ads");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.i("TOKEN", ""+errorCode);
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                Log.i("Error","Ad");
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });


        if (isGoogelPlayInstalled()) {
            String fcmRegId = FirebaseInstanceId.getInstance().getToken();
            if (fcmRegId != null) {
                Log.i("TOKEN", fcmRegId);
//                sendToken(fcmRegId);
            }
        }
        //ca-app-pub-6027874390848714~9084127308

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseInstanceId.getInstance().getToken();
    }

    private boolean isGoogelPlayInstalled() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1).show();
            } else {
                Toast.makeText(getApplicationContext(), "Google Play Service is not installed", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(MainActivity.this, "MainActivity", null);

    }

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) && hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, perm));
    }

    private void loadMap() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // pra obter a localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, INITIAL_REQUEST);
            }
            return;
        }
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment));
        map.getMapAsync(this);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment));
                    map.getMapAsync(this);
                }
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        mAtual = location;
        LatLng latLng = new LatLng(mAtual.getLatitude(), mAtual.getLongitude());
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(it,851);
        }
        else {
            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng));

            }
        }


        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLn) {
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLn)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.sentinela_pin))
                );
                progress = ProgressDialog.show(MainActivity.this, "", "Buscando endereço.", true, true);
            net.checkAddress(latLn.latitude, latLn.longitude, new Network.HttpCallback() {
                @Override
                public void onSuccess(final String response) {
                    progress.dismiss();

                }

                @Override
                public void onFailure(String response, final Throwable throwable) {
                    progress.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            new PersistentCookieStore(getApplicationContext()).removeAll();
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            try {
                                if (throwable == null || throwable.getMessage() == null) {
                                    alert.setTitle("Erro ao autenticar o fiscal!");
                                } else if (throwable != null) {
                                    if (throwable.getMessage().contains("Unable to resolve host")) {//
                                        alert.setTitle("Sem internet!");
                                    } else {
                                        alert.setTitle("Falha na conexão, tente novamente.");
                                    }
                                }
                            } catch (NullPointerException e) {}
                            alert.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            });
                            alert.show();
                        }
                    });
                }

                @Override
                public void onSuccess(final JSONArray response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {}
                    });
                }
            });

            }
        });
    }

    public void centerLocation(View view) {
        if(mAtual != null) {
            LatLng latLng = new LatLng(mAtual.getLatitude(), mAtual.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        }
    }

    public void callPlaces(View view) {
        mFirebaseAnalytics.setUserProperty("Evento loja", "Loja 1");
        Intent intent = null;
        try {
            intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(MainActivity.this);
            startActivityForResult(intent, REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
//                this.onPlaceSelected(place);

                LatLng latLng= new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);

                mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng));
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            }
        }
    }
}
