package com.transaction.service;

import com.transaction.entity.CourseEnrollment;
import com.transaction.entity.Student;
import com.transaction.repository.CourseEnrollmentRepository;
import com.transaction.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository courseEnrollmentRepository;

    public CourseEnrollmentService(CourseEnrollmentRepository courseEnrollmentRepository) {
        this.courseEnrollmentRepository = courseEnrollmentRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveCourseEnrollmentRecord(Long studentId, String courseName, double fee, String status) {
        String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        System.out.println("Transaction Name in saveCourseEnrollmentRecord: " + transactionName);
        try{
            CourseEnrollment enrollment = new CourseEnrollment();
            enrollment.setStudentId(studentId);
            enrollment.setCourseName(courseName);
            enrollment.setCourseFee(fee);
            enrollment.setStatus(status);
            courseEnrollmentRepository.save(enrollment);
        } catch (Exception e) {
            throw new RuntimeException("Exception while saving course enrollment record: ");
        }
    }
}
