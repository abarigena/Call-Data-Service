package com.abarigena.calldataservice.serviceTest;

import com.abarigena.calldataservice.dto.CdrReportResponse;
import com.abarigena.calldataservice.dto.CdrReportsRequest;
import com.abarigena.calldataservice.service.CdrReportService;
import com.abarigena.calldataservice.store.entity.CdrRecord;
import com.abarigena.calldataservice.store.repository.CdrRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CdrReportServiceTest {

    @Mock
    private CdrRecordRepository cdrRecordRepository;

    @InjectMocks
    private CdrReportService cdrReportService;

    private CdrReportsRequest validRequest;
    private final String testMsisdn = "79991234567";
    private final LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
    private final LocalDateTime endDate = LocalDateTime.of(2023, 1, 31, 23, 59);

    @BeforeEach
    public void setUp() {
        validRequest = new CdrReportsRequest();
        validRequest.setMsisdn(testMsisdn);
        validRequest.setStartDate(startDate);
        validRequest.setEndDate(endDate);
    }

    @Test
    void generateReport_withValidRequest_returnSuccessResponse() {
        CdrReportResponse response = cdrReportService.generateReport(validRequest);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getRequestId());
        assertTrue(response.getFileName().contains(testMsisdn));
    }

    @Test
    void generateReport_withEmptyMsisdn_returnErrorResponse(){

        validRequest.setMsisdn("");

        CdrReportResponse response = cdrReportService.generateReport(validRequest);

        assertEquals("ERROR", response.getStatus());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    void generateReport_retrieveCallsFromRepository() throws InterruptedException {
        List<CdrRecord> incomingCalls = createTestCdrRecords(3, "02");
        List<CdrRecord> outgoingCalls = createTestCdrRecords(2, "01");

        when(cdrRecordRepository.findIncomingCallsByMsisdnAndPeriod(eq(testMsisdn), eq(startDate), eq(endDate)))
                .thenReturn(incomingCalls);
        when(cdrRecordRepository.findOutgoingCallsByMsisdnAndPeriod(eq(testMsisdn), eq(startDate), eq(endDate)))
                .thenReturn(outgoingCalls);

        cdrReportService.generateReport(validRequest);

        TimeUnit.MILLISECONDS.sleep(500);

        verify(cdrRecordRepository).findIncomingCallsByMsisdnAndPeriod(testMsisdn, startDate, endDate);
        verify(cdrRecordRepository).findOutgoingCallsByMsisdnAndPeriod(testMsisdn, startDate, endDate);
    }

    private List<CdrRecord> createTestCdrRecords(int count, String callType) {
        List<CdrRecord> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CdrRecord record = new CdrRecord();
            record.setCallType(callType);
            record.setCallerNumber("caller" + i);
            record.setReceiverNumber("receiver" + i);
            record.setStartTime(LocalDateTime.now().minusHours(i));
            record.setEndTime(LocalDateTime.now().minusHours(i).plusMinutes(5));
            records.add(record);
        }
        return records;
    }
}
