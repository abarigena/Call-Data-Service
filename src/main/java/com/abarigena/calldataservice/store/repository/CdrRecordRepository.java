package com.abarigena.calldataservice.store.repository;

import com.abarigena.calldataservice.store.entity.CdrRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с CDR-записями.
 * Предоставляет методы для поиска звонков.
 */
@Repository
public interface CdrRecordRepository extends CrudRepository<CdrRecord, Long> {

    // Находит все входящие звонки для абонента за период
    @Query("SELECT c FROM CdrRecord c WHERE c.callType = '02' AND c.receiverNumber = :msisdn AND " +
            "(:startDate IS NULL OR c.startTime >= :startDate) AND (:endDate IS NULL OR c.startTime <= :endDate)")
    List<CdrRecord> findIncomingCallsByMsisdnAndPeriod(
            @Param("msisdn") String msisdn,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Находит все исходящие звонки для абонента за период
    @Query("SELECT c FROM CdrRecord c WHERE c.callType = '01' AND c.callerNumber = :msisdn AND " +
            "(:startDate IS NULL OR c.startTime >= :startDate) AND (:endDate IS NULL OR c.startTime <= :endDate)")
    List<CdrRecord> findOutgoingCallsByMsisdnAndPeriod(
            @Param("msisdn") String msisdn,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Находит все входящие звонки для всех абонентов за период
    @Query("SELECT c FROM CdrRecord c WHERE c.callType = '02' AND" +
            " c.startTime BETWEEN :startDate AND :endDate")
    List<CdrRecord> findAllIncomingCallsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Находит все исходящие звонки для всех абонентов за период
    @Query("SELECT c FROM CdrRecord c WHERE c.callType = '01' AND" +
            " c.startTime BETWEEN :startDate AND :endDate")
    List<CdrRecord> findAllOutgoingCallsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
