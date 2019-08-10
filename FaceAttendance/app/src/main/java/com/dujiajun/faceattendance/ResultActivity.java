package com.dujiajun.faceattendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private List<Fragment> tabFragments = new ArrayList<>();
    private List<String> tabIndicator = new ArrayList<>();
    private ContentPagerAdapter adapter;
    private AttendManager manager;
    private ResultFragment attendFragment;
    private ResultFragment absenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        setSupportActionBar(toolbar);

        tabIndicator.add("未到");
        tabIndicator.add("已到");

        attendFragment = new ResultFragment();
        absenceFragment = new ResultFragment();

        tabFragments.add(absenceFragment);
        tabFragments.add(attendFragment);

        TabLayout tabLayout = findViewById(R.id.tablayout);
        ViewPager viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(viewPager);
        adapter = new ContentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        ProgressBar progressBar = findViewById(R.id.progressbar);

        Intent i = getIntent();
        File file = new File(i.getStringExtra("file"));
        String courseId = i.getStringExtra("courseId");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String courseTime = sdf.format(Calendar.getInstance().getTime());
        manager = AttendManager.getInstance(this);

        manager.uploadPicture(file, courseId, courseTime, new AttendManager.CallBack() {
            @Override
            public void onSuccess() {
                attendFragment.setStudents(manager.getAttendStudents());
                manager.getTotalStudent(courseId, new AttendManager.CallBack() {
                    @Override
                    public void onSuccess() {
                        List<Student> attendStudents = manager.getAttendStudents();
                        List<Student> totalStudents = manager.getTotalStudents();
                        List<Student> absentStudents = new ArrayList<>();
                        for (Student student : totalStudents) {
                            if (attendStudents == null || !attendStudents.contains(student)) {
                                absentStudents.add(student);
                            }
                        }
                        absenceFragment.setStudents(absentStudents);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailed() {
                        finish();
                    }
                });
            }

            @Override
            public void onFailed() {

            }
        });

        absenceFragment.setCourseIdAndTime(courseId, courseTime);
        attendFragment.setCourseIdAndTime(courseId, courseTime);
        attendFragment.setAttendMode(true);
    }

    public void syncStudentList() {
        List<Student> attend = manager.getAttendStudents();
        List<Student> total = manager.getTotalStudents();
        attendFragment.setStudents(attend);
        List<Student> absentStudents = new ArrayList<>();
        for (Student student : total) {
            if (attend == null || !attend.contains(student)) {
                absentStudents.add(student);
            }
        }
        absenceFragment.setStudents(absentStudents);
    }

    class ContentPagerAdapter extends FragmentPagerAdapter {

        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return tabFragments.get(position);
        }

        @Override
        public int getCount() {
            return tabFragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabIndicator.get(position);
        }
    }
}
