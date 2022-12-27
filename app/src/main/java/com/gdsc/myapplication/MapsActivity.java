package com.gdsc.myapplication;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gdsc.myapplication.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String > permissionLauncher;
    LocationManager lm;
    LocationListener ls;
    FirebaseFirestore db;
    ArrayList<LatLng> CoffecordArrayList;
    ArrayList<String> CoffeNaame;
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        int index = 0;
        CoffecordArrayList = new ArrayList<>();
        CoffeNaame = new ArrayList<>();
        getData();

        PermissionLauncher();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



         lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         ls = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                LocationListener.super.onProviderDisabled(provider);
                //GPS bilgisi kapalıysa İstanbula zoom atasın
                LatLng istanbul = new LatLng(41.006640786734415, 28.98136046813158);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(istanbul,10));
            }
        };
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //izin istemeliyiz
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.getRoot(),"haritalar için izin vermelisiniz!",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //İzin isteyeceğiz
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                }).show();
            }else{
                //izin isteyecez
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            //İzin vermediği zaman istanbula zoom atıyor
            LatLng ist = new LatLng(41.01387641749699, 28.978164202113327);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ist,10));
        }else{
            //İzin daha önce verildiyse bu blok kullanılır
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,10,100,ls);
            //son konumunu alarak uygulama açılınca o konuma gidiyor
            Location lastloc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastloc != null){
                LatLng lastUserLoc = new LatLng(lastloc.getLatitude(),lastloc.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLoc,16));
            }
        }
        mMap.setMyLocationEnabled(true);





    }

    //uygulama başlayınca izin isteği gönderiyoruz.
    private void PermissionLauncher(){
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //izin verildiyse
                    if(ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED){
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,10,100,ls);
                        //kullanıcının son bilinen konumunu alma
                        Location lastloc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(lastloc != null){
                            LatLng lastUserLoc = new LatLng(lastloc.getLatitude(),lastloc.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLoc,16));
                        }
                    }
                }else{
                    //izin verilmediyse
                    Toast.makeText(MapsActivity.this, "haritaları kullanabilmeniz için izin vermelisiniz!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void getData(){
        db = FirebaseFirestore.getInstance();
        db.collection("Coffeas").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(MapsActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                if(value != null){
                    for(DocumentSnapshot snapshot : value.getDocuments()){

                        Map<String ,Object> data = snapshot.getData();

                        assert data != null;
                        String xCoord = (String) data.get("x");
                        String  yCoord = (String) data.get("y");
                        String CoffeName = (String) data.get("Caffe name");

                        assert xCoord != null;
                        Double xDoubleCord = Double.parseDouble(xCoord);
                        assert yCoord != null;
                        Double yDoubleCord = Double.parseDouble(yCoord);

                        LatLng coffes = new LatLng(xDoubleCord,yDoubleCord);
                        CoffecordArrayList.add(coffes);

                        CoffeNaame.add(CoffeName);


                        mMap.addMarker(new MarkerOptions().position(CoffecordArrayList.get(index)).title(CoffeNaame.get(index)));
                        index++;
                    }

                }
            }
        });
    }
// Custom pin but not working
    private BitmapDescriptor bitmap(Context context,int VectorResId){
        Drawable vectorDrawable = ContextCompat.getDrawable(context,VectorResId);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicHeight(),vectorDrawable.getIntrinsicWidth());
        Bitmap bitmap =Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getMinimumHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}