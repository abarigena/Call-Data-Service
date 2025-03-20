package com.abarigena.calldataservice.service;

import com.abarigena.calldataservice.store.entity.CdrRecord;
import com.abarigena.calldataservice.store.entity.Subscriber;
import com.abarigena.calldataservice.store.repository.CdrRecordRepository;
import com.abarigena.calldataservice.store.repository.SubscriberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Сервис для генерации CDR-записей.
 * Предоставляет функциональность для создания и сохранения CDR-записей за указанный период.
 */
@Service
public class CdrGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(CdrGeneratorService.class);

    private final CdrRecordRepository cdrRecordRepository;
    private final SubscriberRepository subscriberRepository;
    private final Random random = new Random();

    @Autowired
    public CdrGeneratorService(CdrRecordRepository cdrRecordRepository, SubscriberRepository subscriberRepository) {
        this.cdrRecordRepository = cdrRecordRepository;
        this.subscriberRepository = subscriberRepository;
    }

    /**
     * Очистка существующих CDR-записей из базы данных
     */
    @Transactional
    public void clearCdrRecords() {
        logger.info("Начата очистка CDR-записей из базы данных");
        cdrRecordRepository.deleteAll();
        logger.info("Завершена очистка CDR-записей из базы данных");
    }

    /**
     * Генерация CDR-записей за год
     */
    @Transactional
    public void generateYearCdrRecords() {
        // Очистка существующих CDR-записей
        logger.info("Начата генерация CDR-записей за год");
        clearCdrRecords();

        List<Subscriber> subscribers = subscriberRepository.findAll();
        if (subscribers.isEmpty()) {
            logger.error("Ошибка генерации CDR-записей: пользователи не найдены");
            throw new IllegalStateException("Пользователи не найдены");
        }
        logger.info("Получен список из {} абонентов для генерации записей", subscribers.size());

        LocalDateTime startDate = LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = LocalDateTime.now();
        logger.info("Период генерации CDR-записей: с {} по {}", startDate, endDate);

        List<CdrRecord> recordsToSave = new ArrayList<>();
        int totalRecordsGenerated = 0;

        // Генерируем записи для каждого абонента в день
        while (startDate.isBefore(endDate)) {
            for (Subscriber subscriber : subscribers) {
                int callsPerDay = random.nextInt(5) + 1; // 1-5 звонков в день

                for (int i = 0; i < callsPerDay; i++) {
                    CdrRecord record = generateRandomCdrRecord(subscriber, startDate);
                    recordsToSave.add(record);
                    totalRecordsGenerated++;

                    // Сохраняем пакетами для оптимизации
                    if (recordsToSave.size() >= 100) {
                        cdrRecordRepository.saveAll(recordsToSave);
                        logger.debug("Сохранена пакетная партия из {} CDR-записей", recordsToSave.size());
                        recordsToSave.clear();
                    }
                }
            }
            startDate = startDate.plusDays(1);
        }

        if (!recordsToSave.isEmpty()) {
            cdrRecordRepository.saveAll(recordsToSave);
            logger.debug("Сохранена финальная партия из {} CDR-записей", recordsToSave.size());
        }

        logger.info("Завершена генерация CDR-записей за год. Всего сгенерировано {} записей", totalRecordsGenerated);
    }

    /**
     * Генерирует случайную CDR-запись для указанного абонента и даты.
     *
     * @param subscriber абонент, для которого генерируется запись
     * @param date дата, в которую должен быть совершен звонок
     * @return созданная CDR-запись с заполненными полями
     */
    private CdrRecord generateRandomCdrRecord(Subscriber subscriber, LocalDateTime date) {
        CdrRecord cdrRecord = new CdrRecord();

        String callType = random.nextBoolean() ? "01" : "02";
        cdrRecord.setCallType(callType);

        List<Subscriber> allSubscribers = subscriberRepository.findAll();
        Subscriber otherSubscriber;
        do {
            otherSubscriber = allSubscribers.get(random.nextInt(allSubscribers.size()));
        } while (otherSubscriber.getMsisdn().equals(subscriber.getMsisdn()));

        if ("01".equals(callType)) {
            cdrRecord.setCallerNumber(subscriber.getMsisdn());
            cdrRecord.setReceiverNumber(otherSubscriber.getMsisdn());
        } else {
            cdrRecord.setCallerNumber(otherSubscriber.getMsisdn());
            cdrRecord.setReceiverNumber(subscriber.getMsisdn());
        }

        int hourOfDay = random.nextInt(24);
        int minuteOfHour = random.nextInt(60);
        int secondOfMinute = random.nextInt(60);

        LocalDateTime callStartTime = date
                .withHour(hourOfDay)
                .withMinute(minuteOfHour)
                .withSecond(secondOfMinute);

        // Генерация случайной продолжительности звонка (от 10 сек до 10 минут)
        int callDurationInSeconds = random.nextInt(600) + 10;
        LocalDateTime callEndTime = callStartTime.plusSeconds(callDurationInSeconds);

        cdrRecord.setStartTime(callStartTime);
        cdrRecord.setEndTime(callEndTime);

        return cdrRecord;
    }
}
