package com.swigg.messaging;

public interface SmsService {

    void sendSms(String phoneNumber, String message);
}
