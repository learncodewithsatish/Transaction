package com.transaction.service.isolation;

import com.transaction.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class ReadCommitted {

    private final StudentService studentService;

    public ReadCommitted(StudentService studentService) {
        this.studentService = studentService;
    }

    public void readCommitted(Long id) throws InterruptedException {

        Thread t1 = new Thread(() -> {
            try {
                studentService.updateWalletBalanceRC(id);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread t2 = new Thread(() -> {
            try {
//                Thread.sleep(2000); // ensure first read happens before update commit
                Double firstRead = studentService.readWalletBalanceRC(id);
                System.out.println("Thread 2: First Read = " + firstRead);
                Thread.sleep(5000); // wait for T1 to commit
                Double secondRead = studentService.readWalletBalanceRC(id);
                System.out.println("Thread 2: Second Read = " + secondRead);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
