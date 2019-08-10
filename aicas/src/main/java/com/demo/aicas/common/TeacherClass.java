package com.demo.aicas.common;

public class TeacherClass {
    private String courseId;
    private String courseName;

    public TeacherClass(String courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }
}
