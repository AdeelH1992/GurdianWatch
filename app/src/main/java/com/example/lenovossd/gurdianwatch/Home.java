package com.example.lenovossd.gurdianwatch;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lenovossd.gurdianwatch.Common.Common;
import com.example.lenovossd.gurdianwatch.Remote.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import es.dmoral.toasty.Toasty;

import android.Manifest;

import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;

import android.support.annotation.Nullable;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.Marker;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class Home extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;



    FirebaseAuth auth;

    FirebaseDatabase db;
    FirebaseStorage storage;

    DatabaseReference users;

    int temp;

    // map

    private static final int MY_PERMISSION_REQUEST_CODE = 7192;

    private static final int PLAY_SERVICE_RES_REQUEST = 300193;


    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    IGoogleAPI mService;
    Marker mUserMarker;
    ImageView uploadImage;
    DatabaseReference drivers,servicelocation;
    GeoFire geoFire;
    private BroadcastReceiver broadcastReceiver;
    private static Home inst;

    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ArrayList<String> messagestore = new ArrayList <String>(  );
    ArrayAdapter arrayAdapter;
    StorageReference storageReference;



    @Override
    protected void onResume() {
        super.onResume();

       servicelocation =FirebaseDatabase.getInstance().getReference(Common.Child_information_tb1).child( FirebaseAuth.getInstance().getCurrentUser().getUid() );


        if (broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final Object latitude = intent.getExtras().get( "latitude" );
                    final Object longitude = intent.getExtras().get( "longitude" );


                    servicelocation.child( "Service Location" ).child( "Latitude")
                            .setValue( latitude )
                            .addOnCompleteListener( new OnCompleteListener <Void>() {
                                @Override
                                public void onComplete(@NonNull Task <Void> task) {
                                    Toasty.info( Home.this,"\n"+latitude +" "+longitude,Toast.LENGTH_LONG, true).show();
                                    servicelocation.child( "Service Location" ).child( "Longitude" ).setValue(  longitude )
                                            .addOnCompleteListener( new OnCompleteListener <Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task <Void> task) {

                                                }
                                            } );
                                }
                            } );


                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter( "location_update" ));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver !=null)
            unregisterReceiver( broadcastReceiver );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home );
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );

        mService = Common.getGooogleAPI();
        // init View


        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        inst = this;
        uploadImage = (ImageView) findViewById( R.id.upload ) ;
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        users = db.getReference( Common.Child_information_tb1 );


        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
      //  smsListView.setAdapter(arrayAdapter);
        uploadImage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseImageandUpload();
            }
        } );



       if (!runtime_permissions())
       {
           Intent i= new Intent( getApplicationContext(),GPS_Service.class );
           startService( i );
       }
       else {
           runtime_permissions();
       }




        drivers = users.child( FirebaseAuth.getInstance().getCurrentUser().getUid() );
        geoFire = new GeoFire( drivers );
        setUpLoaction();
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {


            // Todo : If Permission Granted Then Show SMS
            // testing in waqas mobile
           refreshSmsInbox();

        } else {
            // Todo : Then Set Permission
            final int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(Home.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    private void ChooseImageandUpload() {
        Toasty.custom(Home.this, getString( R.string.CutomToastString), getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp),
                Color.GRAY, 5000, true, false).show();
        Intent intent = new Intent(  );
        intent.setType( "image/*" );
        intent.setAction( Intent.ACTION_GET_CONTENT );
        startActivityForResult( Intent.createChooser(  intent,"Select Picture"),Common.PICK_IMAGE_REQUEST );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)

        {
            final Uri saveUri = data.getData();
            Toast.makeText( this,saveUri.toString(),Toast.LENGTH_LONG).show();
            if (saveUri != null)
            {
                final ProgressDialog progressDialog = new ProgressDialog( this );

                progressDialog.setMessage( "Uploading..." );
                progressDialog.show();

                String imageName = UUID.randomUUID().toString();

                final StorageReference imageFolder = storageReference.child( "images/"+imageName );

                imageFolder.putFile( saveUri )
                        .addOnSuccessListener( new OnSuccessListener <UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();

                                imageFolder.getDownloadUrl().addOnSuccessListener( new OnSuccessListener <Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Map<String,Object> update = new HashMap<>(  );

                                        update.put( "avatarUrl",uri.toString() );

                                        DatabaseReference ChildUser = FirebaseDatabase.getInstance().getReference(Common.Child_information_tb1);
                                        ChildUser.child( Common.Child_user_tb1 ).child( FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren( update ).addOnCompleteListener( new OnCompleteListener <Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toasty.success( Home.this, "Image Was Uploaded", Toast.LENGTH_LONG, true ).show();
                                                }
                                                else
                                                    Toasty.error( Home.this ,"Image wasn't Updated !",Toast.LENGTH_LONG,true).show();
                                            }
                                        } ).addOnFailureListener( new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toasty.error( Home.this ,e.getMessage(),Toast.LENGTH_LONG,true).show();

                                            }
                                        } );
                                        ;                               }
                                } );

                            }
                        } ).addOnProgressListener( new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                        progressDialog.setMessage( "Uploaded " +progress +" %" );
                    }
                } );
            }
        }
    }

    public static Home instance() {
        return inst;
    }

    public void refreshSmsInbox() {

        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query( Uri.parse("content://sms/sent"), null, null, null, null);

        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        //  String name = smsInboxCursor.getString( 0 );
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        String str;
        do {

            str = "SMS Sent to : " +smsInboxCursor.getString( indexAddress )+"  "+
                      smsInboxCursor.getString(indexBody) ;
            Log.e( "str",str );
            messagestore.add( str );
            users = db.getReference( Common.Child_information_tb1 );

            users.child( "SgTttQc7EJXDe9C2K0mTH1YIfHz1" )
                    .child( "Message" )
                    .setValue( "123" )
                    .addOnSuccessListener( new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    } )
                    .addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error( Home.this, "Failed ! " + e.getMessage(), Toast.LENGTH_LONG, true ).show();

                        }
                    } );

            arrayAdapter.add(str);

        } while (smsInboxCursor.moveToNext());



    }
    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }
    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >=23 && ContextCompat.checkSelfPermission( this,Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission( this,Manifest.permission.ACCESS_COARSE_LOCATION )!=
                PackageManager.PERMISSION_GRANTED){
            requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100 );
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        if (requestCode==100){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                Toasty.success( Home.this,"You are ready to go ",Toast.LENGTH_LONG,true ).show();

            }
            else {
                runtime_permissions();
            }
        }
    }

    private void setUpLoaction() {
        if (ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            //Request Runtime permission
            ActivityCompat.requestPermissions( this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE );
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();


            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient );
        if (mLastLocation != null)

        {


            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();


            // Add Marker

            // update to firebase
            geoFire.setLocation( Common.child_Loacion, new GeoLocation( latitude, longitude ), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (mUserMarker != null)
                        mUserMarker.remove();  // remove old marker

                    mUserMarker = mMap.addMarker( new MarkerOptions()
                            .position( new LatLng( latitude, longitude ) )
                            .icon( BitmapDescriptorFactory.fromResource( R.drawable.marker ) )
                            .title( String.format( "You" ) )
                    );

                    // Move Camera to this positon
                    CameraPosition cameraPosition = new CameraPosition.Builder().
                            target( new LatLng( latitude, longitude ) ).
                            tilt( 60 ).
                            zoom( 15 ).
                            bearing( 90 ).
                            build();
                    mMap.animateCamera( CameraUpdateFactory.newCameraPosition( cameraPosition ) );
                    // Draw animation to rotate marker

                    rotateMarker( mUserMarker, 360, mMap );


                    Log.d( "Location Change ", String.format( "Your Location was Changed :%f/%f", latitude, longitude ) );
                }
            } );

        } else {
            Log.d( "Error", "Cannot get your Location" );
        }
    }

    private void rotateMarker(final Marker mcurrent, final float i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mcurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post( new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation( (float) elapsed / duration );
                float rot = t * i + (1 - t) * startRotation;
                mcurrent.setRotation( -rot > 180 ? rot / 2 : rot );

                if (t < 1.0) {
                    handler.postDelayed( this, 16 );
                }
            }
        } );
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval( UPDATE_INTERVAL );
        mLocationRequest.setFastestInterval( FASTEST_INTERVAL );
        mLocationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
        mLocationRequest.setSmallestDisplacement( DISPLACEMENT );
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this );

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError( resultCode ))
                GooglePlayServicesUtil.getErrorDialog( resultCode, this, PLAY_SERVICE_RES_REQUEST ).show();
            else {
                Toasty.error( Home.this, "This device is not supported ", Toast.LENGTH_SHORT, true ).show();
                finish();
            }
            return false;
        }
        return true;
    }






    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled( true );
        mMap.getUiSettings().setZoomGesturesEnabled( true );
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled( true );
        mMap.getUiSettings().setMyLocationButtonEnabled( true );
        mMap.setMaxZoomPreference( 17 );
        mMap.setMinZoomPreference( 15 );

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();

    }

    private void startLocationUpdate() {
        if(ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION  )!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION   )!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient,mLocationRequest,this );
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }



}
