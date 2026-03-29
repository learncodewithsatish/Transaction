package com.transaction.service;

import com.transaction.entity.CourseEnrollment;
import com.transaction.repository.CourseEnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository courseEnrollmentRepository;

    public CourseEnrollmentService(CourseEnrollmentRepository courseEnrollmentRepository) {
        this.courseEnrollmentRepository = courseEnrollmentRepository;
    }

    public void saveCourseEnrollmentRecord(Long studentId, String courseName, double fee, String status) {
        String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        System.out.println("Transaction Name in saveCourseEnrollmentRecord: " + transactionName);
        try{
            CourseEnrollment enrollment = new CourseEnrollment();
            enrollment.setStudentId(studentId);
            enrollment.setCourseName(courseName);
            enrollment.setCourseFee(fee);
            enrollment.setStatus(status);
            int i = 10/0;
            courseEnrollmentRepository.save(enrollment);
        } catch (Exception e) {
            throw new RuntimeException("Exception while saving course enrollment record: ");
        }
    }

}
