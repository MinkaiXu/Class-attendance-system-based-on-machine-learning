package com.demo.aicas.db;


import com.demo.aicas.common.*;

import java.sql.*;
import java.util.ArrayList;

public class DataBaseManager {

    private static DataBaseManager singleton;
    private Connection conn;

    private DataBaseManager() {
        try {
            /**
             * 数据库链接地址
             */
            String URL = "jdbc:mysql://localhost:3306/aicas_database?serverTimezone=UTC";
            /**
             * 数据库用户名
             */
            String USERNAME = "";
            /**
             * 数据库用户密码
             */
            String PASSWORD = "";
            /**
             * 驱动类
             */
            String DRIVER = "com.mysql.cj.jdbc.Driver";
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public static DataBaseManager getInstance() {
        if (singleton == null)
            singleton = new DataBaseManager();
        return singleton;
    }

    /*以下为最终版本*/

    /**
     * 根据学号获取学生的student ID，名字，图片路径
     *
     * @param classId： class ID
     * @return Student ArrayList
     */
    public ArrayList<Student> getStudentPicture(String classId) {
        ArrayList<Student> student = new ArrayList<>();
        try {
            String sql;
            sql = "select A.student_id, A.student_name, A.student_photo_path from student A, student_class B " +
                    "where A.student_id = B.student_id and B.class_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                student.add(new Student(rs.getString(1), rs.getString(2),
                        rs.getString(3)));
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return student;
    }

    /**
     * 向出勤表中插入出勤信息
     *
     * @param classId:    Class ID
     * @param classTime:  class time
     * @param attendList: attendance info list
     * @return boolean：True：success，False：Failed
     */
    public boolean inputAttend(String classId, String classTime, ArrayList<Attend> attendList) {
        try {
            for (int i = 0; i < attendList.size(); i++) {
                //删除已存在数据
                String checkSQL = "delete from attendance_record where class_id = ? and class_time = ? and " +
                        "student_id = ?";
                PreparedStatement checkPS = conn.prepareStatement(checkSQL);
                checkPS.setString(1, classId);
                checkPS.setString(2, classTime);
                checkPS.setString(3, attendList.get(i).getStudentId());
                checkPS.execute();
                checkPS.close();

                String editSQL = "insert into  attendance_record (class_id, class_time, student_id, status) " +
                        "values (?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(editSQL);

                ps.setString(1, classId);
                ps.setString(2, classTime);
                ps.setString(3, attendList.get(i).getStudentId());
                ps.setBoolean(4, attendList.get(i).isAttend());
                ps.executeUpdate();
                ps.close();

            }
        } catch (SQLIntegrityConstraintViolationException f) {
            return true;
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * 获取学生的所有信息，包括学生所上课程的老师的信息
     *
     * @param id：Student ID
     * @return SelectStudent
     */
    public SelectStudent findStudent(String id) {
        try {
            //获取学生出勤信息
            ArrayList<StudentAttend> studentAttends = new ArrayList<>();
            String getStudentAttendanceSQL = String.format("select C.class_name, A.classTime, A.status from attendance_record A, class C " +
                    "where C.class_id = A.class_id and A.student_id = %s", id);
            PreparedStatement studentAttendancePS = conn.prepareStatement(getStudentAttendanceSQL);
            //studentAttendancePS.setString(1, id);
            ResultSet attendanceRs = studentAttendancePS.executeQuery();
            while (attendanceRs.next()) {
                studentAttends.add(new StudentAttend(attendanceRs.getString(1),
                        attendanceRs.getString(2), attendanceRs.getBoolean(3)));
            }
            studentAttendancePS.close();
            attendanceRs.close();

            //获取老师的信息
            ArrayList<TeacherInfo> teacherInfos = new ArrayList<>();
            ArrayList<String> classNames = new ArrayList<>();
            for (int i = 0; i < studentAttends.size(); i++) {
                boolean flag = false;
                String className = studentAttends.get(i).getCourseId();
                for (int j = 0; j < classNames.size(); j++) {
                    if (className.equals(classNames.get(j))) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                } else {
                    classNames.add(className);
                }

                String getTeacherInfosSQL = "select C.class_name, T.teacher_name, T.teacher_mail_address, T.phone " +
                        "from teacher T, class C where T.teacher_id = C.teacher_id and C.class_name = ?";
                PreparedStatement teacherInfoPS = conn.prepareStatement(getTeacherInfosSQL);
                teacherInfoPS.setString(1, className);
                ResultSet teacherInfosRS = teacherInfoPS.executeQuery();
                while (teacherInfosRS.next()) {
                    TeacherInfo t = new TeacherInfo(teacherInfosRS.getString(1),
                            teacherInfosRS.getString(2)
                            , teacherInfosRS.getString(3), teacherInfosRS.getString(4));
                    teacherInfos.add(t);
                }
                teacherInfoPS.close();
                teacherInfosRS.close();
            }

            //获取个人信息
            String infoSQL = "select student_name, student_id, classNum, student_mail_address, phone" +
                    " from student where student_id = ?";
            PreparedStatement infoPs = conn.prepareStatement(infoSQL);
            infoPs.setString(1, id);
            ResultSet infoRs = infoPs.executeQuery();
            while (infoRs.next()) {
                return new SelectStudent(studentAttends, teacherInfos, infoRs.getString(1),
                        infoRs.getString(2), infoRs.getString(3), infoRs.getString(4),
                        infoRs.getString(5));
            }
            System.out.print(1);
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取老师的所有信息，包括其学生的所有信息
     *
     * @param id: the teacher ID
     * @return SelectTeacher
     */
    public SelectTeacher findTeacher(String id) {
        try {
            //查询老师所在班级的所有出勤信息
            ArrayList<TeacherAttend> teacherAttends = new ArrayList<>();
            String getTeacherAttendSQL = "select A.class_id, A.class_name, C.classTime, B.student_id, B.student_name, " +
                    "C.status from class A, student B, attendance_record C where A.class_id = C.class_id " +
                    "and B.student_id = C.student_id and A.teacher_id = ?";
            PreparedStatement getTeacherAttendPS = conn.prepareStatement(getTeacherAttendSQL);

            getTeacherAttendPS.setString(1, id);
            ResultSet getTeacherAttendRS = getTeacherAttendPS.executeQuery();

            while (getTeacherAttendRS.next()) {
                teacherAttends.add(new TeacherAttend(getTeacherAttendRS.getString(1),
                        getTeacherAttendRS.getString(2),
                        getTeacherAttendRS.getString(3), getTeacherAttendRS.getString(4),
                        getTeacherAttendRS.getString(5), getTeacherAttendRS.getBoolean(6)));
            }

            getTeacherAttendPS.close();
            getTeacherAttendRS.close();

            //查询学生的信息
            ArrayList<StudentInfo> studentInfos = new ArrayList<>();
            ArrayList<String> studentIds = new ArrayList<>();
            for (int i = 0; i < teacherAttends.size(); i++) {
                //判断是否已经被选取
                String studentId = teacherAttends.get(i).getStudentId();
                boolean flag = false;
                for (int j = 0; j < studentIds.size(); j++) {
                    if (studentId.equals(studentIds.get(j))) {
                        flag = true;
                    }
                }
                if (flag) {
                    continue;
                } else {
                    studentIds.add(studentId);
                }
                //未被选取过，进行选取
                String getStudentInfoSQL = "select C.class_id, C.class_name, S.student_id, S.student_name, " +
                        "S.student_mail_address, S.phone from class C, student S, student_class SC " +
                        "where C.class_id = SC.class_id and SC.student_id = S.student_id and S.student_id = ? and C.teacher_id = ? " +
                        "group by S.student_id, C.class_id";
                PreparedStatement studentInfosPS = conn.prepareStatement(getStudentInfoSQL);
                studentInfosPS.setString(1, teacherAttends.get(i).getStudentId());
                studentInfosPS.setString(2, id);
                ResultSet studentInfosRS = studentInfosPS.executeQuery();
                while (studentInfosRS.next()) {
                    StudentInfo temp = new StudentInfo(studentInfosRS.getString(1), studentInfosRS.getString(2),
                            studentInfosRS.getString(3), studentInfosRS.getString(4),
                            studentInfosRS.getString(5), studentInfosRS.getString(6));
                    studentInfos.add(temp);

                }
                studentInfosPS.close();
                studentInfosRS.close();
            }
            //查询老师信息
            String infoSQL = "select teacher_name, teacher_id, teacher_mail_address, phone " +
                    "from teacher where teacher_id = ?";
            PreparedStatement infoPs = conn.prepareStatement(infoSQL);
            infoPs.setString(1, id);
            ResultSet infoRs = infoPs.executeQuery();
            while (infoRs.next()) {
                SelectTeacher selectTeacher = new SelectTeacher(teacherAttends, studentInfos, infoRs.getString(1),
                        infoRs.getString(2), infoRs.getString(3), infoRs.getString(4));
                return selectTeacher;
            }
            infoPs.close();
            infoRs.close();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据输入的信息修改老师和学生的信息
     *
     * @param id:    student id or teacher id
     * @param mail:  students' or teachers' Email-address, format: xxxx@xx.xx.xx
     * @param phone: students' or teachers' phone number
     * @param role:  0: teacher; 1: student
     * @return: 修改成功则返回true， 修改失败则返回false
     */
    public boolean editInfo(String id, String mail, String phone, boolean role) {
        try {
            String editSQL;
            if (role) {
                editSQL = String.format("update student set student_mail_address = %s, phone = %s where student_id = %s",
                        mail, phone, id);
            } else {
                editSQL = String.format("update teacher set teacher_mail_address = %s, phone = %s where teacher_id = %s",
                        mail, phone, id);
            }

            PreparedStatement editPs = conn.prepareStatement(editSQL);
            //editPs.setString(1, mail);
            //editPs.setString(2, phone);
            //editPs.setString(3, id);

            editPs.executeUpdate();
            editPs.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据输入信息修改出勤表的出勤情况
     *
     * @param classId:   class id
     * @param classTime: class time, format: 0000-00-00
     * @param studentId: student id
     * @return 修改成功则返回true， 失败则返回false
     */
    public boolean editAttend(String classId, String classTime, String studentId) {
        try {
            String editSQL = String.format("update attendance_record set status = 1 " +
                    "where class_id = %s and classTime = %s and student_id = %s", classId, classTime, studentId);

            PreparedStatement editPs = conn.prepareStatement(editSQL);
            //editPs.setBoolean(1, attend);
            //editPs.setString(2, classId);
            //editPs.setString(3, classTime);
            //editPs.setString(4, studentId);

            editPs.executeUpdate();
            editPs.close();

            editPs.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 输入老师工号，获取老师所有课程的课程id和课程名
     *
     * @param id：Teacher ID
     * @return ArrayList TeacherCLass
     */
    public ArrayList<TeacherClass> checkTeacher(String id) {
        try {
            ArrayList<TeacherClass> teacherClasses = new ArrayList<>();
            String checkSQL = "select class_id, class_name from class where teacher_id = ?";
            PreparedStatement checkPS = conn.prepareStatement(checkSQL);
            checkPS.setString(1, id);
            ResultSet checkRS = checkPS.executeQuery();
            while (checkRS.next()) {
                teacherClasses.add(new TeacherClass(checkRS.getString(1), checkRS.getString(2)));
            }
            checkPS.close();
            checkRS.close();
            return teacherClasses;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * check if the account is legal
     *
     * @param loginInfo: the account info that need to be checked
     * @return if the account is legal, return true, else return false
     */
    public boolean isLegal(LoginInfo loginInfo) {
        try {
            boolean flag = false;
            String loginCheckSQL = "select password from login where username = ? and role = ?";
            PreparedStatement loginCheckPS = conn.prepareStatement(loginCheckSQL);
            loginCheckPS.setString(1, loginInfo.getUserName());
            loginCheckPS.setBoolean(2, loginInfo.isRole());
            ResultSet loginCheckRS = loginCheckPS.executeQuery();
            while (loginCheckRS.next()) {
                String password = loginCheckRS.getString(1);
                if (password.equals(loginInfo.getPassWord())) {
                    flag = true;
                }
            }
            loginCheckPS.close();
            loginCheckRS.close();
            if (flag)
                return true;
            else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
