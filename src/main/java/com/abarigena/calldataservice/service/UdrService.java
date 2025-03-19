package com.abarigena.calldataservice.service;

import com.abarigena.calldataservice.dto.UdrReport;
import com.abarigena.calldataservice.store.entity.CdrRecord;
import com.abarigena.calldataservice.store.entity.Subscriber;
import com.abarigena.calldataservice.store.repository.CdrRecordRepository;
import com.abarigena.calldataservice.store.repository.SubscriberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с UDR-отчетами.
 * Предоставляет методы для получения и формирования отчетов об использовании услуг связи.
 */
@Service
public class UdrService {
    private static final Logger logger = LoggerFactory.getLogger(UdrService.class);

    private final CdrRecordRepository cdrRecordRepository;
    private final SubscriberRepository subscriberRepository;

    @Autowired
    public UdrService(CdrRecordRepository cdrRecordRepository, SubscriberRepository subscriberRepository) {
        this.cdrRecordRepository = cdrRecordRepository;
        this.subscriberRepository = subscriberRepository;
    }

    /**
     * Получение UDR-отчета для одного абонента за указанный период или за все время
     *
     * @return заполненный объект UdrReport с данными о входящих и исходящих звонках
     */
    public UdrReport getUdrForSubscriber(String msisdn, Integer year, Integer month) {
        logger.info("Запрос UDR-отчета для абонента {}, год: {}, месяц: {}", msisdn, year, month);

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (year != null && month != null) {
            YearMonth yearMonth = YearMonth.of(year, month);
            startDate = yearMonth.atDay(1).atStartOfDay();
            endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            logger.debug("Установлен период для отчета: с {} по {}", startDate, endDate);
        } else {
            logger.debug("Период не указан, будет сформирован отчет за все время");
        }

        List<CdrRecord> incomingCalls = cdrRecordRepository.findIncomingCallsByMsisdnAndPeriod(msisdn, startDate, endDate);
        List<CdrRecord> outgoingCalls = cdrRecordRepository.findOutgoingCallsByMsisdnAndPeriod(msisdn, startDate, endDate);

        logger.debug("Найдено {} входящих и {} исходящих звонков для абонента {}",
                incomingCalls.size(), outgoingCalls.size(), msisdn);

        // Расчет продолжительности звонков
        Duration incomingDuration = calculateTotalDuration(incomingCalls);
        Duration outgoingDuration = calculateTotalDuration(outgoingCalls);

        UdrReport report = new UdrReport();
        report.setMsisdn(msisdn);
        report.setIncomingCall(new UdrReport.CallInfo(formatDuration(incomingDuration)));
        report.setOutcomingCall(new UdrReport.CallInfo(formatDuration(outgoingDuration)));

        logger.info("Сформирован UDR-отчет для абонента {}: входящие={}, исходящие={}",
                msisdn, formatDuration(incomingDuration), formatDuration(outgoingDuration));

        return report;
    }

    /**
     * Получение UDR-отчетов для всех абонентов за указанный месяц
     *
     * @return список отчетов UdrReport для всех абонентов
     */
    public List<UdrReport> getAllUdrsByMonth(int year, int month) {
        logger.info("Запрос UDR-отчетов для всех абонентов за {}-{}", year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        logger.debug("Установлен период для отчетов: с {} по {}", startDate, endDate);

        List<Subscriber> subscribers = subscriberRepository.findAll();
        List<UdrReport> reports = new ArrayList<>();

        logger.debug("Получен список из {} абонентов для формирования отчетов", subscribers.size());

        List<CdrRecord> allIncomingCalls = cdrRecordRepository.findAllIncomingCallsByPeriod(startDate, endDate);
        List<CdrRecord> allOutgoingCalls = cdrRecordRepository.findAllOutgoingCallsByPeriod(startDate, endDate);

        logger.debug("Загружено {} входящих и {} исходящих звонков за период",
                allIncomingCalls.size(), allOutgoingCalls.size());

        // Группировка звонков по номерам абонентов
        Map<String, List<CdrRecord>> incomingCallsByMsisdn = groupCallsByMsisdn(allIncomingCalls, false);
        Map<String, List<CdrRecord>> outgoingCallsByMsisdn = groupCallsByMsisdn(allOutgoingCalls, true);

        for (Subscriber subscriber : subscribers) {
            String msisdn = subscriber.getMsisdn();

            // Получение звонков для текущего абонента
            List<CdrRecord> subscriberIncomingCalls = incomingCallsByMsisdn
                    .getOrDefault(msisdn, new ArrayList<>());
            List<CdrRecord> subscriberOutgoingCalls = outgoingCallsByMsisdn
                    .getOrDefault(msisdn, new ArrayList<>());

            Duration incomingDuration = calculateTotalDuration(subscriberIncomingCalls);
            Duration outgoingDuration = calculateTotalDuration(subscriberOutgoingCalls);

            UdrReport report = new UdrReport();
            report.setMsisdn(msisdn);
            report.setIncomingCall(new UdrReport.CallInfo(formatDuration(incomingDuration)));
            report.setOutcomingCall(new UdrReport.CallInfo(formatDuration(outgoingDuration)));

            reports.add(report);

            logger.trace("Сформирован UDR-отчет для абонента {}: входящие={}, исходящие={}",
                    msisdn, formatDuration(incomingDuration), formatDuration(outgoingDuration));
        }

        logger.info("Сформировано {} UDR-отчетов для всех абонентов за {}-{}", reports.size(), year, month);

        return reports;
    }

    /**
     * Группировка звонков по номеру абонента
     *
     * @param calls список звонков для группировки
     * @param outgoing флаг типа звонков (true - исходящие, false - входящие)
     * @return Map, где ключ - номер абонента, значение - список звонков
     */
    private Map<String, List<CdrRecord>> groupCallsByMsisdn(List<CdrRecord> calls, boolean outgoing) {
        Map<String, List<CdrRecord>> callsByMsisdn = new HashMap<>();

        for (CdrRecord call : calls) {
            String msisdn = outgoing ? call.getCallerNumber() : call.getReceiverNumber();

            if (!callsByMsisdn.containsKey(msisdn)) {
                callsByMsisdn.put(msisdn, new ArrayList<>());
            }
            callsByMsisdn.get(msisdn).add(call);
        }

        return callsByMsisdn;
    }

    /**
     * Расчет общей продолжительности звонков
     *
     * @param calls список звонков для расчета
     * @return общая продолжительность в формате Duration
     */
    private Duration calculateTotalDuration(List<CdrRecord> calls) {
        Duration totalDuration = Duration.ZERO;

        for (CdrRecord call : calls) {
            Duration callDuration = Duration.between(call.getStartTime(), call.getEndTime());
            totalDuration = totalDuration.plus(callDuration);
        }

        return totalDuration;
    }

    /**
     * Форматирование длительности в строку "HH:MM:SS"
     *
     * @param duration объект Duration для форматирования
     * @return строка в формате "HH:MM:SS"
     */
    private String formatDuration(Duration duration) {
        long totalSeconds = duration.getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
