package com.abarigena.calldataservice.store.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Класс сущности для хранения информации о CDR-записях.
 * Содержит данные о звонках абонентов: тип звонка, номера абонентов, время начала и окончания звонка.
 */
@Entity
@Table(name = "cdr_records")
public class CdrRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false,length = 2)
    private String callType; // 01 , 02

    @Column(nullable = false)
    private String callerNumber;

    @Column(nullable = false)
    private String receiverNumber;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    public CdrRecord() {
    }

    public CdrRecord(String callerNumber, Long id, String callType, String receiverNumber, LocalDateTime startTime, LocalDateTime endTime) {
        this.callerNumber = callerNumber;
        this.id = id;
        this.callType = callType;
        this.receiverNumber = receiverNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallerNumber() {
        return callerNumber;
    }

    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
    }

    public String getReceiverNumber() {
        return receiverNumber;
    }

    public void setReceiverNumber(String receiverNumber) {
        this.receiverNumber = receiverNumber;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
