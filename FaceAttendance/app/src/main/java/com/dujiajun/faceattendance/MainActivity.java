package com.dujiajun.faceattendance;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CAMERA_OK = 1;
    private static final int EXTERNAL_OK = 2;
    private static final int TAKE_PHOTO = 2;
    private static final int CUT_PICTURE = 3;
    private ImageView imageView;
    private TextView textHint;
    private Button btnUpload;
    private Button btnCapture;
    private String courseId;
    private AttendManager manager;
    private List<Course> courseList;
    private ArrayAdapter<Course> adapter;
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnCapture = findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(this);

        imageView = findViewById(R.id.imageview);
        textHint = findViewById(R.id.text_hint);
        btnUpload = findViewById(R.id.btn_upload);
        btnUpload.setOnClickListener(this);
        manager = AttendManager.getInstance(this);

        courseList = manager.getCourses();
        Spinner spinner = findViewById(R.id.spinner);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courseList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseId = courseList.get(position).courseId;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                courseId = null;
            }
        });

        if (!manager.checkCoursesExist()) {
            manager.getOnlineCourses(new AttendManager.CallBack() {
                @Override
                public void onSuccess() {
                    //courseList = manager.getCourses();
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailed() {

                }
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        //courseList = manager.getCourses();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private String getPathFromUri(Uri uri, String selection) {
        String s = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if (cursor.moveToFirst()) {
                s = cursor.getString(column_index);
            }
            cursor.close();
        }
        return s;
    }

    private String handleImage(Intent data) {
        Uri uri = data.getData();
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            Log.e("TAG", docId);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getPathFromUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);

            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                if (docId.startsWith("raw")) {
                    imagePath = docId.replace("raw:", "");
                } else {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    imagePath = getPathFromUri(contentUri, null);
                }
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getPathFromUri(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {
                //ImageView imageView = new ImageView(MainActivity.this);
                //Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                btnCapture.setText("重新拍摄");
                //btnCapture.setVisibility(View.GONE);

            } else if (requestCode == CUT_PICTURE) {
                imageFile = new File(handleImage(data));
                //Log.e("TAG", imageFile.getPath());
            }
            textHint.setVisibility(View.GONE);
            btnUpload.setVisibility(View.VISIBLE);
            btnCapture.setText("重新拍摄");
            Glide.with(MainActivity.this).load(imageFile).into(imageView);
        }

    }

    private void capture() {
        Uri imageUri;
        File outputPath = new File(getFilesDir(), "images");
        if (!outputPath.exists())
            outputPath.mkdir();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA);
        String imageName = sdf.format(Calendar.getInstance().getTime());
        imageFile = new File(outputPath, imageName + ".png");
        try {
            if (imageFile.exists())
                imageFile.delete();
            imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(MainActivity.this,
                    "com.dujiajun.fileprovider", imageFile);

        } else {
            imageUri = Uri.fromFile(imageFile);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void getFromGallery() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CUT_PICTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_OK || requestCode == EXTERNAL_OK) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capture();
            } else {
                Toast.makeText(this, "未允许权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_capture: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("请选择操作")
                        .setPositiveButton("拍照", (dialog, which) -> {
                            if (ContextCompat.checkSelfPermission(MainActivity.this,
                                    android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.CAMERA}, CAMERA_OK);
                            } else {
                                capture();
                            }

                        })
                        .setNeutralButton("取消", null)
                        .setNegativeButton("相册", (dialog, which) -> {
                            if (ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_OK);
                            } else {
                                getFromGallery();
                            }

                        }).show();

                break;
            }

            case R.id.btn_upload: {
                if (courseId == null) {
                    Toast.makeText(this, "还未选中课程", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示")
                        .setMessage("确认上传此照片？")
                        .setPositiveButton("确认", (dialog, which) -> {
                            Intent intent1 = new Intent(MainActivity.this, ResultActivity.class);
                            intent1.putExtra("file", imageFile.getPath());
                            intent1.putExtra("courseId", courseId);
                            startActivity(intent1);
                        })
                        .setNegativeButton("取消", null).show();
            }

        }
    }
}
