package com.abarigena.calldataservice.dto;

/**
 * Класс DTO для представления UDR-отчета.
 * Содержит информацию о номере абонента и суммарной длительности входящих и исходящих звонков.
 */
public class UdrReport {
    private String msisdn;
    private CallInfo incomingCall;
    private CallInfo outcomingCall;

    public static class CallInfo {
        private String totalTime;

        public CallInfo() {
        }

        public CallInfo(String totalTime) {
            this.totalTime = totalTime;
        }

        public String getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(String totalTime) {
            this.totalTime = totalTime;
        }
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public CallInfo getIncomingCall() {
        return incomingCall;
    }

    public void setIncomingCall(CallInfo incomingCall) {
        this.incomingCall = incomingCall;
    }

    public CallInfo getOutcomingCall() {
        return outcomingCall;
    }

    public void setOutcomingCall(CallInfo outcomingCall) {
        this.outcomingCall = outcomingCall;
    }
}
