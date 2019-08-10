package com.demo.aicas.common;

public class TeacherAttend {
    private String courseId;
    private String courseName;
    private String courseTime;
    private String studentId;
    private String studentName;
    private boolean attend;

    public TeacherAttend(String courseId, String courseName, String courseTime, String studentId, String studentName, boolean attend) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseTime = courseTime;
        this.studentId = studentId;
        this.studentName = studentName;
        this.attend = attend;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseTime() {
        return courseTime;
    }

    public String getStudentName() {
        return studentName;
    }

    public boolean isAttend() {
        return attend;
    }

    public String getStudentId() {
        return studentId;
    }
}
