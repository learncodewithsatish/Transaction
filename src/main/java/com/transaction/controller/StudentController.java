package com.transaction.controller;

import com.transaction.service.isolation.ReadCommitted;
import com.transaction.service.isolation.ReadUncommitted;
import com.transaction.service.isolation.RepeatableRead;
import com.transaction.service.isolation.Serializable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final ReadUncommitted readUncommitted;
    private final ReadCommitted readCommitted;
    private final RepeatableRead repeatableRead;
    private final Serializable serializable;

    public StudentController(ReadUncommitted readUncommitted,
                             ReadCommitted readCommitted,
                             RepeatableRead repeatableRead,
                             Serializable serializable) {
        this.readUncommitted = readUncommitted;
        this.readCommitted = readCommitted;
        this.repeatableRead = repeatableRead;
        this.serializable = serializable;
    }

    @GetMapping("/read-uncommitted/{id}")
    public void readUncommittedDemo(@PathVariable Long id) throws InterruptedException {
        readUncommitted.readUncommitted(id);
    }

    @GetMapping("/read-committed/{id}")
    public void readCommittedDemo(@PathVariable Long id) throws InterruptedException {
        readCommitted.readCommitted(id);
    }

    @GetMapping("/repeatable-read/{id}")
    public void repeatableReadDemo(@PathVariable Long id) throws InterruptedException {
        repeatableRead.repeatableRead(id);
    }

    @GetMapping("/serializable/{id}")
    public void serializableDemo(@PathVariable Long id) throws InterruptedException {
        serializable.serializable(id);
    }


}
