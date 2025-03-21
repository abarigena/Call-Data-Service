package com.abarigena.calldataservice.serviceTest;

import com.abarigena.calldataservice.dto.UdrReport;
import com.abarigena.calldataservice.service.UdrService;
import com.abarigena.calldataservice.store.entity.CdrRecord;
import com.abarigena.calldataservice.store.entity.Subscriber;
import com.abarigena.calldataservice.store.repository.CdrRecordRepository;
import com.abarigena.calldataservice.store.repository.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UdrServiceTest {
    @Mock
    private CdrRecordRepository cdrRecordRepository;

    @Mock
    private SubscriberRepository subscriberRepository;

    @InjectMocks
    private UdrService udrService;

    private final String msisdn = "79991234567";
    private List<CdrRecord> incomingCalls;
    private List<CdrRecord> outgoingCalls;
    private List<Subscriber> subscribers;

    @BeforeEach
    void setUp() {
        incomingCalls = new ArrayList<>();
        outgoingCalls = new ArrayList<>();

        // Создаем два входящих звонка для первого абонента (79991234567)
        // Первый входящий звонок - 5 минут
        CdrRecord incomingCall1 = new CdrRecord();
        incomingCall1.setCallType("02");
        incomingCall1.setCallerNumber("79992222222");   // Кто звонит
        incomingCall1.setReceiverNumber(msisdn);        // Кому звонят (наш первый абонент)
        incomingCall1.setStartTime(LocalDateTime.now().minusHours(2));
        incomingCall1.setEndTime(LocalDateTime.now().minusHours(2).plusMinutes(5));
        incomingCalls.add(incomingCall1);

        // Второй входящий звонок - 10 минут
        CdrRecord incomingCall2 = new CdrRecord();
        incomingCall2.setCallType("02");
        incomingCall2.setCallerNumber("79993333333");   // Кто звонит
        incomingCall2.setReceiverNumber(msisdn);        // Кому звонят (наш первый абонент)
        incomingCall2.setStartTime(LocalDateTime.now().minusHours(1));
        incomingCall2.setEndTime(LocalDateTime.now().minusHours(1).plusMinutes(10));
        incomingCalls.add(incomingCall2);

        // Создаем один исходящий звонок для первого абонента (79991234567) - 15 минут
        CdrRecord outgoingCall = new CdrRecord();
        outgoingCall.setCallType("01");
        outgoingCall.setCallerNumber(msisdn);           // Кто звонит (наш первый абонент)
        outgoingCall.setReceiverNumber("79994444444");  // Кому звонят
        outgoingCall.setStartTime(LocalDateTime.now().minusMinutes(30));
        outgoingCall.setEndTime(LocalDateTime.now().minusMinutes(30).plusMinutes(15));
        outgoingCalls.add(outgoingCall);

        // Создаем исходящий звонок для второго абонента (79992222222) - 5 минут
        CdrRecord outgoingCallSubscriber2 = new CdrRecord();
        outgoingCallSubscriber2.setCallType("01");
        outgoingCallSubscriber2.setCallerNumber("79992222222");  // Кто звонит (наш второй абонент)
        outgoingCallSubscriber2.setReceiverNumber("79995555555"); // Кому звонят
        outgoingCallSubscriber2.setStartTime(LocalDateTime.now().minusMinutes(20));
        outgoingCallSubscriber2.setEndTime(LocalDateTime.now().minusMinutes(20).plusMinutes(5));
        outgoingCalls.add(outgoingCallSubscriber2);

        subscribers = new ArrayList<>();
        Subscriber subscriber1 = new Subscriber();
        subscriber1.setMsisdn(msisdn);
        subscribers.add(subscriber1);

        Subscriber subscriber2 = new Subscriber();
        subscriber2.setMsisdn("79992222222");
        subscribers.add(subscriber2);
    }

    @Test
    void getUdrForSubscriber_returnCorrectDurations() {
        // моки - запрос UDR для одного абонента
        when(cdrRecordRepository.findIncomingCallsByMsisdnAndPeriod(eq(msisdn), any(), any()))
                .thenReturn(incomingCalls);
        when(cdrRecordRepository.findOutgoingCallsByMsisdnAndPeriod(eq(msisdn), any(), any()))
                .thenReturn(outgoingCalls.stream()
                        .filter(call -> call.getCallerNumber().equals(msisdn))
                        .toList()); // вернуть звонки первого абонента

        UdrReport report = udrService.getUdrForSubscriber(msisdn, null, null);

        assertNotNull(report);
        assertEquals(msisdn, report.getMsisdn());
        assertEquals("00:15:00", report.getIncomingCall().getTotalTime()); // 5 + 10 минут входящих
        assertEquals("00:15:00", report.getOutcomingCall().getTotalTime()); // 15 минут исходящих
    }

    @Test
    void getAllUdrsByMonth_returnReportsForAllSubscribers() {
        int year = 2024;
        int month = 3;
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // моки - запрос UDR для всех абонентов
        when(subscriberRepository.findAll()).thenReturn(subscribers);
        when(cdrRecordRepository.findAllIncomingCallsByPeriod(eq(startDate), eq(endDate)))
                .thenReturn(incomingCalls);
        when(cdrRecordRepository.findAllOutgoingCallsByPeriod(eq(startDate), eq(endDate)))
                .thenReturn(outgoingCalls);

        List<UdrReport> reports = udrService.getAllUdrsByMonth(year, month);

        assertNotNull(reports);
        assertEquals(subscribers.size(), reports.size());

        // Проверяем отчет для первого абонента (79991234567)
        UdrReport report1 = reports.stream()
                .filter(r -> r.getMsisdn().equals(msisdn))
                .findFirst()
                .orElse(null);
        assertNotNull(report1);
        assertEquals("00:15:00", report1.getIncomingCall().getTotalTime()); // 5 + 10 минут входящих
        assertEquals("00:15:00", report1.getOutcomingCall().getTotalTime()); // 15 минут исходящих

        // Проверяем отчет для второго абонента (79992222222)
        UdrReport report2 = reports.stream()
                .filter(r -> r.getMsisdn().equals("79992222222"))
                .findFirst()
                .orElse(null);
        assertNotNull(report2);
        assertEquals("00:00:00", report2.getIncomingCall().getTotalTime()); // Нет входящих звонков
        assertEquals("00:05:00", report2.getOutcomingCall().getTotalTime()); // 5 минут исходящих
    }
}
