package com.swigg.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "twilio")
public class TwilioSmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @Value("${sms.default-country-code:+91}")
    private String defaultCountryCode;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendSms(String phoneNumber, String message) {
        String normalizedPhone = normalizePhone(phoneNumber);
        logger.info("Sending SMS via Twilio to {}", normalizedPhone);

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("To", normalizedPhone);
        body.add("From", fromNumber);
        body.add("Body", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(accountSid, authToken);

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        logger.info("SMS sent successfully to {}", normalizedPhone);
    }

    private String normalizePhone(String phoneNumber) {
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }
        return defaultCountryCode + phoneNumber;
    }
}
