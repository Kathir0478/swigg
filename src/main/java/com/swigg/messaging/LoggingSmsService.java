package com.swigg.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "logging", matchIfMissing = true)
public class LoggingSmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingSmsService.class);

    @Override
    public void sendSms(String phoneNumber, String message) {
        logger.info("SMS to {}: {}", phoneNumber, message);
    }
}
