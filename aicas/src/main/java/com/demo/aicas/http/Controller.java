package com.demo.aicas.http;

import com.demo.aicas.common.*;
import com.demo.aicas.db.DataBaseManager;
import com.demo.aicas.face.FaceChecker;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

@RestController

public class Controller {

    private DataBaseManager manager;
    private FaceChecker face;

    public Controller() {
        manager = DataBaseManager.getInstance();
        face = FaceChecker.getInstance();
    }

    @RequestMapping("/")
    public String hello() {
        return "hello";
    }

    @RequestMapping(value = "/check_teacher", method = RequestMethod.POST)
    public ArrayList<TeacherClass> checkTeacher(String id) {
        return manager.checkTeacher(id);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public boolean login(LoginInfo loginInfo) {
        return manager.isLegal(loginInfo);
    }

    @RequestMapping(value = "/upload_picture", method = RequestMethod.POST)
    @ResponseBody
    public ArrayList<Student> getClassPicture(HttpServletRequest request) {
        MultipartHttpServletRequest param = ((MultipartHttpServletRequest) request);
        MultipartFile file = param.getFile("picture");
        String courseId = param.getParameter("courseId");
        String courseTime = param.getParameter("courseTime");
        String classPicture = "img/" + courseId + ' ' + courseTime + ".png";

        BufferedOutputStream stream;

        if (file != null) {
            try {
                byte[] bytes = file.getBytes();
                stream = new BufferedOutputStream(new FileOutputStream(new File("img/" + courseId + ' ' + courseTime + ".png")));
                stream.write(bytes);
                stream.close();
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
        ArrayList<Student> studentInfo = manager.getStudentPicture(courseId);
        ArrayList<Student> attend1 = face.faceSearch(classPicture, studentInfo);
        setAttend(courseId, courseTime, studentInfo, attend1);
        return attend1;
    }


    @RequestMapping(value = "/get_student_info", method = RequestMethod.POST)
    public ArrayList<Student> getStudentInfo(String courseId) {
        return manager.getStudentPicture(courseId);
    }

    private void setAttend(String courseId, String courseTime, ArrayList<Student> studentInfo, ArrayList<Student> attend1) {
        ArrayList<Attend> attendList = new ArrayList<>();
        boolean flag;
        for (Student value : studentInfo) {
            flag = false;
            if (attend1 != null) {
                for (Student student : attend1) {
                    if (value.getStudentId().equals(student.getStudentId())) {
                        Attend temp = new Attend(value.getStudentId(), true);
                        attendList.add(temp);
                        flag = true;
                        break;
                    }
                }
            }
            if (!flag) {
                Attend temp = new Attend(value.getStudentId(), false);
                attendList.add(temp);
            }
        }
        manager.inputAttend(courseId, courseTime, attendList);
    }

    @RequestMapping(value = "/select_student", method = RequestMethod.POST)
    public SelectStudent selectStudent(String id) {
        return manager.findStudent(id);
    }

    @RequestMapping(value = "/select_teacher", method = RequestMethod.POST)
    public SelectTeacher selectTeacher(String id) {
        return manager.findTeacher(id);
    }

    @RequestMapping(value = "/edit_student", method = RequestMethod.POST)
    public boolean editStudent(String id, String mail, String phone) {
        mail = String.format("'%s'", mail);
        return manager.editInfo(id, mail, phone, true);
    }

    @RequestMapping(value = "/edit_teacher", method = RequestMethod.POST)
    public boolean editInfo(String id, String mail, String phone) {
        mail = String.format("'%s'", mail);
        return manager.editInfo(id, mail, phone, false);
    }

    @RequestMapping(value = "/edit_attend", method = RequestMethod.POST)
    public boolean editAttend(String courseId, String courseTime, String studentId) {
        courseId = String.format("'%s'", courseId);
        courseTime = String.format("'%s'", courseTime);
        return manager.editAttend(courseId, courseTime, studentId);
    }

}
