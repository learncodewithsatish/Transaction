package com.transaction.service.isolation;

import com.transaction.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class Serializable {

    private final StudentService studentService;

    public Serializable(StudentService studentService) {
        this.studentService = studentService;
    }

    public void serializable(Long id) throws InterruptedException {

        Thread t2 = new Thread(() -> {
            studentService.readWithLock(id);
        });

        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(2000); // ensure T2 starts first
                studentService.updateWalletBalanceSerializable(id);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t2.start();
        t1.start();

        t2.join();
        t1.join();
    }
}
