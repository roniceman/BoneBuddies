package com.example.bonebuddies;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.FirebaseApp;

import java.util.Locale;

public class SignInUpActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private Switch switchSignInUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_up);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new SignUpFragment(), "Sign Up");
        pagerAdapter.addFragment(new SignInFragment(), "Sign In");
        FirebaseApp.initializeApp(this);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale("en"));
        res.updateConfiguration(conf, dm);
        switchSignInUp = findViewById(R.id.switchSignInUp);
        switchSignInUp.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show Sign In Fragment
                viewPager.setCurrentItem(1);
            } else {
                // Show Sign Up Fragment
                viewPager.setCurrentItem(0);
            }
        });
    }
}




