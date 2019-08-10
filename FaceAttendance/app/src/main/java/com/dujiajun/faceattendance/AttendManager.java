package com.dujiajun.faceattendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AttendManager {
    private static final int MSG_ATTEND = 1;
    private static final int MSG_TOTAL = 2;
    private static final int MSG_COURSE = 3;
    private static final int MSG_EDIT = 4;
    private static final int MSG_LOGIN = 5;
    private static final int MSG_NET_ERROR = 6;
    private static final int ATTEND = 0;
    private static final int TOTAL = 1;
    private static AttendManager singleton;
    private final String URL = "http://10.0.2.2:8080";
    private OkHttpClient client;
    private Context context;
    private List<Student> attendStudents = new ArrayList<>();
    private List<Student> totalStudents = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();
    private String id;
    private ResponseHandler responseHandler = new ResponseHandler(Looper.getMainLooper());

    private AttendManager(Context context) {
        client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS).build();
        this.context = context.getApplicationContext();
    }

    public static AttendManager getInstance(Context context) {
        if (singleton == null)
            singleton = new AttendManager(context);
        return singleton;
    }

    public void login(String teacherId, String password, CallBack callBack) {
        RequestBody body = new FormBody.Builder()
                .add("userName", teacherId)
                .add("passWord", password)
                .add("role", "0")
                .build();
        Request request = new Request.Builder()
                .url(URL + "/login")
                .post(body)
                .build();
        responseHandler.setLoginCallBack(callBack);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = MSG_NET_ERROR;
                responseHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                if (Boolean.valueOf(body)) {
                    id = teacherId;
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString("id", teacherId);
                    editor.apply();
                }
                Message message = new Message();
                message.what = MSG_LOGIN;
                message.obj = Boolean.valueOf(body);
                responseHandler.sendMessage(message);
            }
        });
    }

    public void getOnlineCourses(CallBack callBack) {
        //Log.e("TAG", id + "\n" + s);
        if (id == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            id = preferences.getString("id", "");
        }
        RequestBody body = new FormBody.Builder()
                .add("id", id)
                .build();
        Request request = new Request.Builder()
                .url(URL + "/check_teacher")
                .post(body)
                .build();
        responseHandler.setCourseCallBack(callBack);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = MSG_NET_ERROR;
                responseHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                //Log.e("TAG", response.headers().toString()+"\n"+body);
                setLocalCourse(body);
                //courses = getCourseFronString(body);
                parseCourseList(body);
                Message message = new Message();
                message.what = MSG_COURSE;
                responseHandler.sendMessage(message);
            }
        });
    }

    private void parseCourseList(String s) {
        courses.clear();
        try {
            JSONArray array = new JSONArray(s);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                courses.add(new Course(object.getString("courseId"), object.getString("courseName")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void parseStudentList(String s, int which) {
        if (which == ATTEND) {
            attendStudents.clear();
            try {
                JSONArray array = new JSONArray(s);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    attendStudents.add(new Student(object.getString("studentId"), object.getString("studentName")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (which == TOTAL) {
            totalStudents.clear();
            try {
                JSONArray array = new JSONArray(s);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    totalStudents.add(new Student(object.getString("studentId"), object.getString("studentName")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String getLocalCourse() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("COURSE_JSON", "");
    }

    private void setLocalCourse(String s) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("COURSE_JSON", s);
        editor.apply();
    }

    public boolean checkCoursesExist() {
        if (!courses.isEmpty()) return true;
        String s = getLocalCourse();
        if (!s.equals("")) {
            parseCourseList(s);
            return true;
        }
        return false;
    }

    public void uploadPicture(File image, String classId, String classTime, CallBack callBack) {
        //Log.e("TAG", image.getPath());
        RequestBody imagePart = RequestBody.create(MediaType.parse("image/png"), image);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("picture", image.getName(), imagePart)
                .addFormDataPart("courseId", classId)
                .addFormDataPart("courseTime", classTime)
                .build();
        Request request = new Request.Builder()
                .url(URL + "/upload_picture")
                .post(requestBody)
                .build();
        responseHandler.setAttendCallBack(callBack);
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                //responseHandler.handleMessage();
                e.printStackTrace();
                Message message = new Message();
                message.what = MSG_NET_ERROR;
                responseHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //String header = response.header("Content-Type");
                String body = response.body().string();
                parseStudentList(body, ATTEND);
                Message message = new Message();
                message.what = MSG_ATTEND;
                responseHandler.sendMessage(message);
            }
        });
    }

    public void getTotalStudent(String courseId, CallBack callBack) {
        RequestBody body = new FormBody.Builder()
                .add("courseId", courseId)
                .build();
        Request request = new Request.Builder()
                .url(URL + "/get_student_info")
                .post(body)
                .build();
        responseHandler.setTotalCallBack(callBack);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = MSG_NET_ERROR;
                responseHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                parseStudentList(body, TOTAL);
                Message message = new Message();
                message.what = MSG_TOTAL;
                responseHandler.sendMessage(message);
            }
        });
    }

    public List<Student> getAttendStudents() {
        return attendStudents;
    }

    public List<Student> getTotalStudents() {
        return totalStudents;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void editAttend(String courseId, String courseTime, String studentId, CallBack callBack) {
        RequestBody body = new FormBody.Builder()
                .add("courseId", courseId)
                .add("courseTime", courseTime)
                .add("studentId", studentId)
                .add("attend", "true")
                .build();
        Request request = new Request.Builder()
                .url(URL + "/edit_attend")
                .post(body)
                .build();
        responseHandler.setEditCallBack(callBack);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = MSG_NET_ERROR;
                responseHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Message message = new Message();
                message.what = MSG_EDIT;
                message.obj = Boolean.valueOf(body);
                responseHandler.sendMessage(message);
            }
        });
    }

    interface CallBack {
        void onSuccess();

        void onFailed();
    }

    private class ResponseHandler extends Handler {
        CallBack attendCallBack;
        CallBack totalCallBack;
        CallBack courseCallBack;
        CallBack editCallBack;
        CallBack loginCallBack;

        ResponseHandler(Looper looper) {
            super(looper);
        }

        void setAttendCallBack(CallBack attendCallBack) {
            this.attendCallBack = attendCallBack;
        }

        void setTotalCallBack(CallBack totalCallBack) {
            this.totalCallBack = totalCallBack;
        }

        void setLoginCallBack(CallBack loginCallBack) {
            this.loginCallBack = loginCallBack;
        }

        void setEditCallBack(CallBack callback) {
            this.editCallBack = callback;
        }

        void setCourseCallBack(CallBack callback) {
            this.courseCallBack = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_ATTEND) {
                attendCallBack.onSuccess();
            } else if (msg.what == MSG_TOTAL) {
                totalCallBack.onSuccess();
            } else if (msg.what == MSG_COURSE) {
                courseCallBack.onSuccess();
            } else if (msg.what == MSG_EDIT) {
                if ((Boolean) msg.obj)
                    editCallBack.onSuccess();
                else
                    editCallBack.onFailed();
            } else if (msg.what == MSG_LOGIN) {
                if ((Boolean) msg.obj)
                    loginCallBack.onSuccess();
                else
                    loginCallBack.onFailed();
            } else if (msg.what == MSG_NET_ERROR) {
                Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
