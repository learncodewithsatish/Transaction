package com.transaction.service;

import com.transaction.entity.Student;
import com.transaction.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class StudentService {

    @Autowired
    private EntityManager entityManager;

    private final StudentRepository studentRepository;
    private final CourseEnrollmentService courseEnrollmentService;

    public StudentService(StudentRepository studentRepository,
                          CourseEnrollmentService courseEnrollmentService){
        this.studentRepository = studentRepository;
        this.courseEnrollmentService = courseEnrollmentService;
    }

    @Transactional
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

    //========================= READ_UNCOMMITTED ====================================
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void updateWalletBalance(Long studentId) throws InterruptedException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        System.out.println("Thread 1: Original Balance = " + student.getWalletBalance());
        student.setWalletBalance(100000L);
        studentRepository.save(student);
        // Force Hibernate to hit DB immediately
        studentRepository.flush();
        System.out.println("Thread 1: Balance Updated but NOT committed");
        Thread.sleep(10000); // keep transaction open, wait for 10 seconds so that T2 can read
        System.out.println("Thread 1: Transaction Committing...");
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void readWalletBalance(Long studentId) {
        studentRepository.findById(studentId); // force a DB hit
        Double balance = studentRepository.findById(studentId).get().getWalletBalance();
        System.out.println("Thread 2: Read wallet Balance = " + balance);
    }

    //========================= READ_COMMITTED ====================================
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateWalletBalanceRC(Long studentId) throws InterruptedException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        System.out.println("Thread 1: Original Balance = " + student.getWalletBalance());
        Thread.sleep(4000); // delay so T2 reads first
        student.setWalletBalance(100000L);
        studentRepository.save(student);
        studentRepository.flush(); // force DB write
        System.out.println("Thread 1: Balance Updated and Committing...");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Double readWalletBalanceRC(Long studentId) {
        Double balance = studentRepository.findById(studentId).get().getWalletBalance();
        return balance;
    }

    // ========================= REPEATABLE_READ ====================================
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateWalletBalanceRR(Long studentId) throws InterruptedException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        System.out.println("Thread 1: Original Balance = " + student.getWalletBalance());
        Thread.sleep(4000); // delay so T2 reads first
        student.setWalletBalance(100000L);
        studentRepository.save(student);
        studentRepository.flush(); // force DB write
        System.out.println("Thread 1: Balance Updated and Committing...");
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void readWalletBalanceRR(Long studentId) {
        Double firstRead = studentRepository.findById(studentId).get().getWalletBalance();
        System.out.println("Thread 2: First Read = " + firstRead);
        try {
            Thread.sleep(6000); // wait for T1 commit
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Double secondRead = studentRepository.findById(studentId).get().getWalletBalance();
        System.out.println("Thread 2: Second Read = " + secondRead);
    }

    // ========================= SERIALIZABLE ====================================
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void readWithLock(Long studentId) {
        Double balance = studentRepository.findById(studentId).get().getWalletBalance();
        System.out.println("Thread 2: Read Balance = " + balance);
        try {
            System.out.println("Thread 2: Holding transaction...");
            Thread.sleep(10000); // keep transaction open
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Thread 2: Transaction Committing...");
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updateWalletBalanceSerializable(Long studentId) {
        System.out.println("Thread 1: Trying to update...");
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        student.setWalletBalance(100000L);
        studentRepository.save(student);
        studentRepository.flush();
        System.out.println("Thread 1: Update Done and Committing...");
    }

}
