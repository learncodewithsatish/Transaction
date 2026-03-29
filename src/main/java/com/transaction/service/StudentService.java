package com.transaction.service;

import com.transaction.entity.Student;
import com.transaction.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseEnrollmentService courseEnrollmentService;

    public StudentService(StudentRepository studentRepository,
                          CourseEnrollmentService courseEnrollmentService){
        this.studentRepository = studentRepository;
        this.courseEnrollmentService = courseEnrollmentService;
    }

    @Transactional(rollbackFor = {Exception.class})
    public String enrollCourse(Long studentId, String courseName, double fee) {

        String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        System.out.println("Transaction Name in enrollCourse: " + transactionName);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        double studentWalletBalance = student.getWalletBalance();
        if (studentWalletBalance < fee) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        student.setWalletBalance(studentWalletBalance - fee);
        studentRepository.save(student);
        courseEnrollmentService.saveCourseEnrollmentRecord(studentId, courseName, fee, "ENROLLED");
        return "Student " + student.getName() + " enrolled in " + courseName;
    }

}
