package com.demo.aicas.common;

public class Attend {
    private final String studentId;
    private final boolean attend;

    public Attend(String student_id, boolean attend) {
        this.studentId = student_id;
        this.attend = attend;
    }

    public String getStudentId() {
        return studentId;
    }

    public boolean isAttend() {
        return attend;
    }
}
