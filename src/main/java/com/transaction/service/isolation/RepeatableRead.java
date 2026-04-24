package com.transaction.service.isolation;

import com.transaction.entity.Student;
import com.transaction.repository.StudentRepository;
import com.transaction.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepeatableRead {

    private final StudentRepository studentRepository;
    private final StudentService studentService;

    public RepeatableRead(StudentRepository studentRepository,
                          StudentService studentService) {
        this.studentRepository = studentRepository;
        this.studentService = studentService;
    }

    public void repeatableRead(Long id) throws InterruptedException {

        Thread t1 = new Thread(() -> {
            try {
                studentService.updateWalletBalanceRR(id);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(2000); // ensure first read happens first
                studentService.readWalletBalanceRR(id);
                Thread.sleep(6000); // wait for T1 to commit
                studentService.readWalletBalanceRR(id);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

//    //========================= REPEATABLE_READ ====================================
//    @Transactional(isolation = Isolation.REPEATABLE_READ)
//    public void updateWalletBalanceRR(Long studentId) throws InterruptedException {
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new RuntimeException("Student not found"));
//        System.out.println("Thread 1: Original Balance = " + student.getWalletBalance());
//        Thread.sleep(4000); // delay so T2 reads first
//        student.setWalletBalance(100000L);
//        studentRepository.save(student);
//        studentRepository.flush(); // force DB write
//        System.out.println("Thread 1: Balance Updated and Committing...");
//    }
//
//    @Transactional(isolation = Isolation.REPEATABLE_READ)
//    public void readWalletBalanceRR(Long studentId) {
//        Double firstRead = studentRepository.findById(studentId).get().getWalletBalance();
//        System.out.println("Thread 2: First Read = " + firstRead);
//        try {
//            Thread.sleep(6000); // wait for T1 commit
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        Double secondRead = studentRepository.findById(studentId).get().getWalletBalance();
//        System.out.println("Thread 2: Second Read = " + secondRead);
//    }

}
