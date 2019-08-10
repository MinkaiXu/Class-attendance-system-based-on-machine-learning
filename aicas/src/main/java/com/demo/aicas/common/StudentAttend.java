package com.demo.aicas.common;

public class StudentAttend {
    private String courseId;
    private String courseTime;
    private boolean attend;

    public StudentAttend(String courseId, String courseTime, boolean attend) {
        this.courseId = courseId;
        this.courseTime = courseTime;
        this.attend = attend;
    }

    public String getCourseTime() {
        return courseTime;
    }

    public boolean isAttend() {
        return attend;
    }

    public String getCourseId() {
        return courseId;
    }
}
