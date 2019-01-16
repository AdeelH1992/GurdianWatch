package com.example.lenovossd.gurdianwatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lenovossd.gurdianwatch.Common.Common;
import com.example.lenovossd.gurdianwatch.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;

import static com.example.lenovossd.gurdianwatch.R.string.string;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnRegister;

    FirebaseAuth auth;

    FirebaseDatabase db;

    DatabaseReference users;

    RelativeLayout rootlayout;

    AlertDialog waitingDialog;

    TextView txt_forgot_pwd;

    StorageReference storageReference;


    private ArrayList<String> images;
    FirebaseStorage storage;
    int size;
    int temp;
    private static final int PICK_FROM_GALLERY = 1;
    ArrayList <HashMap <String, String>> al = new ArrayList <HashMap <String, String>>();
    User user = new User(  );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Paper.init( this );
        auth = FirebaseAuth.getInstance();

        db = FirebaseDatabase.getInstance();

        users = db.getReference( Common.Child_information_tb1);

        btnSignIn = (Button) findViewById( R.id.btn_sign_in );
        btnRegister = (Button) findViewById( R.id.btn_Register );
        rootlayout = (RelativeLayout) findViewById( R.id.root_layout );
        txt_forgot_pwd = (TextView) findViewById( R.id.txt_forgot_password );



        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        waitingDialog = new SpotsDialog( MainActivity.this );

        txt_forgot_pwd.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showDialogForgotpwd();
                return false;
            }
        } );

        btnRegister.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        } );

        btnSignIn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        } );

    }




    private void showDialogForgotpwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( this );
        alertDialog.setTitle( "FORGOT PASSWORD" );
        alertDialog.setMessage( "Please enter your email Address" );

        LayoutInflater inflater = LayoutInflater.from( MainActivity.this );
        View forgot_psw_layout = inflater.inflate( R.layout.forgot_pwd,null );
        final MaterialEditText edtEmail = (MaterialEditText)forgot_psw_layout.findViewById( R.id.edt_email );
        alertDialog.setView( forgot_psw_layout );
        //set button

        alertDialog.setPositiveButton( "RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                waitingDialog.show();

                auth.sendPasswordResetEmail( edtEmail.getText().toString().trim() )
                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();
                                Toasty.success( MainActivity.this,"Reset password link has been sent", Toast.LENGTH_LONG,true ).show();
                            }
                        } ).addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();
                        Toasty.error( MainActivity.this," "+e.getMessage(),Toast.LENGTH_LONG,true ).show();

                    }
                } );
            }
        } );
        alertDialog.setNegativeButton( "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        } );
        alertDialog.show();
    }

    // uses this method to sign in to application
    private void showLoginDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this  );
        dialog.setTitle( "SIGN IN" );
        dialog.setMessage("Please Use Email To sign in");

        LayoutInflater inflater= LayoutInflater.from(this);

        View login_layout=inflater.inflate(R.layout.layout_signin,null);

        final MaterialEditText edtEmail = login_layout.findViewById( R.id.edt_email );
        final MaterialEditText edtpassword = login_layout.findViewById( R.id.edt_password );


        dialog.setView( login_layout );

        dialog.setPositiveButton( "SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                btnSignIn.setEnabled( false );
                if (TextUtils.isEmpty( edtEmail.getText().toString() ))
                {
                    Toasty.error( MainActivity.this, "Please Enter Your Email address", Toast.LENGTH_LONG, true ).show();
                    btnSignIn.setEnabled( true );
                    return;
                }
                if (TextUtils.isEmpty( edtpassword.getText().toString() ))
                {
                    Toasty.error( MainActivity.this, "Please Enter Your Password", Toast.LENGTH_LONG, true ).show();
                    btnSignIn.setEnabled( true );
                    return;
                }

                if (edtpassword.getText().toString().length() < 6)
                {
                    Toasty.error( MainActivity.this, "Your Password is too Short", Toast.LENGTH_LONG, true ).show();
                    btnSignIn.setEnabled( true );
                    return;
                }
                if((!Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()))
                {                       Toasty.error( MainActivity.this, "Please enter a valid Email (youremail@gmail.com)", Toast.LENGTH_LONG, true ).show();
                    btnSignIn.setEnabled( true );
                    return;
                }
                //Login


                waitingDialog.show();


                auth.signInWithEmailAndPassword( edtEmail.getText().toString() ,edtpassword.getText().toString() )
                        .addOnSuccessListener( new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                         try {
                                    loadallImages();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                FirebaseDatabase.getInstance().getReference(Common.Child_information_tb1)
                                        .child( Common.Child_user_tb1 )
                                        .child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                                        .addListenerForSingleValueEvent( new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                Log.e( "datasnpshot",dataSnapshot.toString() );


                                                Paper.book().write( Common.user_field,edtEmail.getText().toString() );
                                                Paper.book().write( Common.pwd_field,edtpassword.getText().toString() );
                                                Intent intent = new Intent( MainActivity.this,Home.class );




                                                waitingDialog.dismiss();
                                                Toasty.success(MainActivity.this, "Login SucessFully", Toast.LENGTH_SHORT, true).show();
                                               startActivity( intent );
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        } );




                            }
                        } )
                        .addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Toasty.error( MainActivity.this, "Failed !!" + e.getMessage(), Toast.LENGTH_LONG, true ).show();
                                btnSignIn.setEnabled( true );
                            }
                        } );



            }
        });

        dialog.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        } );





        dialog.show();

    }


    private void loadallContact() {
        HashMap <String, String> nameNumberMap = new HashMap <String, String>();
        Cursor phones = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC" );
        temp = 0;
        while (phones.moveToNext()) {
            String contactName = phones.getString( phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME ) );
            String image = phones.getString( phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI ) );
            String contactNumber = phones.getString( phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER ) );
            nameNumberMap.put( "Name", contactName );
            nameNumberMap.put( "Number", contactNumber );
            al.add( nameNumberMap );


            users.child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                    .child( "PhoneBook" )
                    .push()
                    .setValue( nameNumberMap )
                    .addOnSuccessListener( new OnSuccessListener <Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    } )
                    .addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error( MainActivity.this, "Failed ! " + e.getMessage(), Toast.LENGTH_LONG, true ).show();

                        }
                    } );
        }


        Log.e( "check", "as2" );
        customAdapter adapter = new customAdapter( this, al );


        //  list.setAdapter(adapter);
        Log.e( "check", String.valueOf( al.size() ) );
    }
    private void showRegisterDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this  );
        dialog.setTitle( "REGISTER" );
        dialog.setMessage("Please Use Email To Register");

        LayoutInflater inflater= LayoutInflater.from(this);

        View register_layout=inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText edtEmail = register_layout.findViewById( R.id.edt_email );
        final MaterialEditText edtpassword = register_layout.findViewById( R.id.edt_password );
        final MaterialEditText edtName = register_layout.findViewById( R.id.edt_name );
        final MaterialEditText edtPhone = register_layout.findViewById( R.id.edt_phone );



        dialog.setView( register_layout );


        dialog.setPositiveButton( "Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if(TextUtils.isEmpty( edtEmail.getText().toString() ))
                {
                    Toasty.error(MainActivity.this, "Please Enter Your Email address", Toast.LENGTH_LONG, true).show();

                    return; }
                if(TextUtils.isEmpty( edtpassword.getText().toString() ))
                {
                    Toasty.error(MainActivity.this, "Please Enter Your Password", Toast.LENGTH_LONG, true).show();
                    return;
                }
                if(TextUtils.isEmpty( edtName.getText().toString() ))
                {
                    Toasty.error(MainActivity.this, "Please Enter Your Name", Toast.LENGTH_LONG, true).show();
                    return;
                }
                if(TextUtils.isEmpty( edtPhone.getText().toString() ))
                {
                    Toasty.error(MainActivity.this, "Please Enter Your Phone Number", Toast.LENGTH_LONG, true).show();
                    return;
                }
                if((!Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()))
                {                       Toasty.error( MainActivity.this, "Please enter a valid Email (youremail@gmail.com)", Toast.LENGTH_LONG, true ).show();

                    return;
                }

                if(edtpassword.getText().toString().length()<6)
                {
                    Toasty.error(MainActivity.this, "Your Password is too Short", Toast.LENGTH_LONG, true).show();
                    return;

                }

                // Register User

                waitingDialog.show();
                auth.createUserWithEmailAndPassword( edtEmail.getText().toString(),edtpassword.getText().toString() )
                        .addOnSuccessListener( new OnSuccessListener <AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                // save user to db


                                user.setEmail( edtEmail.getText().toString());
                                user.setName(edtName.getText().toString());
                                user.setPassword( edtpassword.getText().toString() );
                                user.setPhone( (edtPhone.getText().toString()) );
                                user.setAvatarUrl("");
                                user.setkey(FirebaseAuth.getInstance( ).getCurrentUser().getUid());

                                users.child( Common.Child_user_tb1 )
                                        .child( FirebaseAuth.getInstance( ).getCurrentUser().getUid())
                                        .setValue( user )
                                        .addOnSuccessListener( new OnSuccessListener <Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                loadallContact();
                                                waitingDialog.dismiss();
                                                Toasty.success(MainActivity.this, "Register SucessFully", Toast.LENGTH_SHORT, true).show();

                                            }
                                        } )
                                        .addOnFailureListener( new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                waitingDialog.dismiss();
                                                Toasty.error(MainActivity.this, "Failed ! "+ e.getMessage(), Toast.LENGTH_LONG, true).show();

                                            }
                                        } );


                            }
                        } )

                        .addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Toasty.error(MainActivity.this, "Failed ! "+ e.getMessage(), Toast.LENGTH_LONG, true).show();

                            }
                        } );

            }
        } );

        dialog.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        } );

        dialog.show();


    }
    private void loadallImages() throws InterruptedException {

        new ImageAdapter( this );

        try {
            if (ActivityCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions( MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FROM_GALLERY );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (images != null) {
            FirebaseUser user = auth.getCurrentUser();
            String UserId = user.getUid();

            if (images.size() > 100) {
                size = 30;
            } else {
                size = images.size();
            }


            for (int i = 0; i < size - 1; i++) {
                Uri uri = Uri.fromFile( new File( images.get( i ) ) );
                String imageName = UUID.randomUUID().toString();

                final StorageReference imageFolder = storageReference.child( "images/" + imageName );
                temp = 0;
                imageFolder.putFile( uri )
                        .addOnSuccessListener( new OnSuccessListener <UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                imageFolder.getDownloadUrl().addOnSuccessListener( new OnSuccessListener <Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Map<String, Object> update = new HashMap<>();
                                        update.put( "image", uri.toString() );
                                        DatabaseReference gallery = FirebaseDatabase.getInstance().getReference( Common.Child_information_tb1 );
                                        gallery.child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                                                .child( Common.Gallery )
                                                .push()
                                                .updateChildren( update ).addOnCompleteListener( new OnCompleteListener <Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task <Void> task) {
                                                Toasty.success( MainActivity.this, "Image Was Uploaded" + temp, Toast.LENGTH_LONG, true ).show();
                                                temp++;

                                            }
                                        } )
                                                .addOnFailureListener( new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toasty.error( MainActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_LONG, true ).show();
                                                    }
                                                } );

                                    }
                                } );


                            }
                        } );

                // TimeUnit.SECONDS.sleep( 5 );
                System.gc();

            }
        }


    }
    private class ImageAdapter extends BaseAdapter {

        /** The context. */
        private Activity context;

        /**
         * Instantiates a new image adapter.
         *
         * @param localContext
         *            the local context
         */
        public ImageAdapter(Activity localContext) {
            context = localContext;
            images = getAllShownImagesPath(context);
        }

        @Override
        public  int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView
                        .setLayoutParams(new GridView.LayoutParams(270, 270));

            } else {
                picturesView = (ImageView) convertView;
            }

            Glide.with(context).load(images.get(position))
                    // .placeholder(R.drawable.ic_launcher).centerCrop()
                    .into(picturesView);

            return picturesView;
        }
    }
    private ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow( MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }
}
