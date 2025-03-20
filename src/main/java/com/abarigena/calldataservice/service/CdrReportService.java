package com.abarigena.calldataservice.service;

import com.abarigena.calldataservice.dto.CdrReportResponse;
import com.abarigena.calldataservice.dto.CdrReportsRequest;
import com.abarigena.calldataservice.store.entity.CdrRecord;
import com.abarigena.calldataservice.store.repository.CdrRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class CdrReportService {

    private static final Logger logger = LoggerFactory.getLogger(CdrReportService.class);
    private static final String REPORTS_DIRECTORY = "reports";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final CdrRecordRepository cdrRecordRepository;

    @Autowired
    public CdrReportService(CdrRecordRepository cdrRecordRepository) {
        this.cdrRecordRepository = cdrRecordRepository;
        createReportsDirectory();
    }

    /**
     * Создает директорию для отчетов, если она не существует.
     */
    private void createReportsDirectory() {
        Path reportsPath = Paths.get(REPORTS_DIRECTORY);
        if(!Files.exists(reportsPath)) {
            try {
                Files.createDirectory(reportsPath);
                logger.info("Создана директория для отчетов: {}", reportsPath.toAbsolutePath());
            } catch (IOException e) {
                logger.error("Ошибка при создании директории для отчетов: {}", e.getMessage());
            }
        }
    }

    /**
     * Асинхронно генерирует CDR-отчет на основе запроса.
     *
     * @param request Запрос на генерацию отчета
     * @return Ответ с UUID запроса и статусом выполнения
     */
    public CdrReportResponse generateReport(CdrReportsRequest request) {
        UUID requestId = UUID.randomUUID();
        String msisdn = request.getMsisdn();

        if(msisdn == null|| msisdn.isEmpty()) {
            logger.error("Не указан номер абонента для генерации отчета");
            return CdrReportResponse.error(requestId, "Не указан номер абонента");
        }

        String fileName = msisdn + "_" + requestId + ".csv";
        String filePath = REPORTS_DIRECTORY + File.separator + fileName;

        // Асинхронно генерируем отчет
        CompletableFuture.runAsync(() -> {
            try {
                generateCdrReport(request, filePath);
                logger.info("Отчет успешно сгенерирован: {}", filePath);
            } catch (Exception e) {
                logger.error("Ошибка при генерации отчета: {}", e.getMessage());
            }
        });

        return CdrReportResponse.success(requestId, fileName);
    }

    /**
     * Генерирует CDR-отчет и сохраняет его в файл.
     *
     * @param request  Запрос на генерацию отчета
     * @param filePath Путь к файлу для сохранения отчета
     * @throws IOException если произошла ошибка при записи в файл
     */
    private void generateCdrReport(CdrReportsRequest request, String filePath) throws IOException {
        logger.info("Начало генерации отчета для абонента {} за период с {} по {}",
                request.getMsisdn(), request.getStartDate(), request.getEndDate());

        List<CdrRecord> cdrRecords = new ArrayList<>();

        // Получаем все входящие звонки абонента за указанный период
        List<CdrRecord> incomingCalls = cdrRecordRepository.findIncomingCallsByMsisdnAndPeriod(request.getMsisdn(),
                request.getStartDate(), request.getEndDate());
        cdrRecords.addAll(incomingCalls);

        // Получаем все исходящие звонки абонента за указанный период
        List<CdrRecord> outgoingCalls = cdrRecordRepository.findOutgoingCallsByMsisdnAndPeriod(
                request.getMsisdn(), request.getStartDate(), request.getEndDate()
        );
        cdrRecords.addAll(outgoingCalls);

        // Сортируем все звонки по времени начала
        cdrRecords.sort((a,b) -> a.getStartTime().compareTo(b.getStartTime()));

        try(FileWriter writer = new FileWriter(filePath)) {
            for (CdrRecord cdrRecord : cdrRecords) {
                String line = String.format("%s,%s,%s,%s,%s%n",
                        cdrRecord.getCallType(),
                        cdrRecord.getCallerNumber(),
                        cdrRecord.getReceiverNumber(),
                        cdrRecord.getStartTime().format(ISO_FORMATTER),
                        cdrRecord.getEndTime().format(ISO_FORMATTER));
                writer.write(line);
            }
        }

        logger.info("Отчет успешно сгенерирован, записано {} записей", cdrRecords.size());
    }
}
