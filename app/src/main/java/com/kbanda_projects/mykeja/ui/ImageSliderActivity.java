package com.kbanda_projects.mykeja.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.adapters.ImageSliderViewPagerAdapter;

import java.util.List;

public class ImageSliderActivity extends AppCompatActivity {
    private List<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_slider);

        imageUrls = (List<String>) getIntent().getSerializableExtra("hostelImages");

        ViewPager viewPager = findViewById(R.id.imageSliderViewPager);
        ImageSliderViewPagerAdapter sliderViewPagerAdapter = new ImageSliderViewPagerAdapter(this, imageUrls);
        viewPager.setAdapter(sliderViewPagerAdapter);


    }
}