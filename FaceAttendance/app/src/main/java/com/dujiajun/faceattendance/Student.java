package com.dujiajun.faceattendance;

import androidx.annotation.Nullable;

public class Student {
    public String studentName;
    public String studentId;

    public Student(String studentId, String studentName) {
        this.studentName = studentName;
        this.studentId = studentId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Student) {
            Student stu = (Student) obj;
            return stu.studentId.equals(this.studentId) && stu.studentName.equals(this.studentName);
        }
        return super.equals(obj);
    }
}
