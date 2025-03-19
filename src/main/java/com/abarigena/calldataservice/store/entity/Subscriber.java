package com.abarigena.calldataservice.store.entity;

import jakarta.persistence.*;

/**
 * Класс сущности для хранения информации об абонентах.
 * Содержит идентификатор и номер телефона абонента (msisdn).
 */
@Entity
@Table(name = "subscribers")
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String msisdn; // Номер телефона

    public Subscriber() {
    }

    public Subscriber(Long id, String msisdn) {
        this.id = id;
        this.msisdn = msisdn;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
