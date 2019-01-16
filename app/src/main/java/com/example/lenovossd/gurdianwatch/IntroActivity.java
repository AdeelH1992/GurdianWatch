package com.example.lenovossd.gurdianwatch;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

public class IntroActivity extends MaterialIntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
       // setContentView( R.layout.activity_intro );
        hideBackButton();
        enableLastSlideAlphaExitTransition(false);
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.black)
                .buttonsColor(R.color.grey)
                .neededPermissions( new String[]{Manifest.permission.READ_CONTACTS} )
                .image(R.drawable.phoneimage)
                .title("Contact Permission")
                .description("This app need the Contact permission .In order to get the Phone Contact. This app ensure that We cant do antything with your granted Permission ")
                .build());
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.black)
                        .buttonsColor(R.color.grey)
                        .neededPermissions( new String[]{Manifest.permission.RECEIVE_SMS} )
                        .image(R.drawable.smsimage)
                        .title("SMS Permission")
                        .description("This app need the SMS Inbox permission .In order to Receive the Sms. This app ensure that We cant do antything with your granted Permission ")
                        .build());
        addSlide(new SlideFragmentBuilder()
                .backgroundColor( R.color.black)
                .buttonsColor(R.color.grey)
                .neededPermissions( new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION} )
                .image(R.drawable.locationimage)
                .title("LocationPermission")
                .description("This app need the Gps location permission .In order to ensure Your Current Location accurately. This app ensure that We cant do antything with your granted Permission ")
                .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.black)
                .buttonsColor(R.color.grey)
                .neededPermissions( new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} )
                .image(R.drawable.gallery_images)
                .title("Gallery Permission")
                .description("This app need the Gallery permission .In order to get the Images. This app ensure that We cant do antything with your granted Permission ")
                .build());
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.black)
                .buttonsColor(R.color.grey)
                .neededPermissions( new String[]{Manifest.permission.READ_CONTACTS} )
                .image(R.drawable.chid_protection)
                .title("Welcome!")
                .description("TO"+"\n"+"GUARDIAN WATCH")
                .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent( IntroActivity.this,MainActivity.class );
                        startActivity( intent );
                        finish();

                   }
                }, "Continue"));






    }

    @Override
    public void onFinish() {
        super.onFinish();
        Intent intent = new Intent( IntroActivity.this, MainActivity.class );
        startActivity( intent );
        finish();
    }
}
