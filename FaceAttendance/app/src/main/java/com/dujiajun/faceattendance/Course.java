package com.dujiajun.faceattendance;

public class Course {
    public String courseId;
    public String courseName;

    public Course(String courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
    }

    @Override
    public String toString() {
        return courseName;
    }
}
