package com.creati.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    private static final String FROM_EMAIL = "nyoo0923@gmail.com"; // 발신 Gmail 주소
    private static final String APP_PASSWORD = "whkw klfc vnck tbum"; // 앱 비밀번호

    public static String sendVerificationCode(String toEmail) throws Exception {
        // 6자리 랜덤 인증번호 생성
        String code = String.format("%06d", new Random().nextInt(1000000));

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("[Creati] 인증번호 안내");
        message.setText(
            "안녕하세요! Creati입니다.\n\n" +
            "인증번호: " + code + "\n\n" +
            "5분 내에 입력해주세요.\n" +
            "본인이 요청하지 않았다면 이 메일을 무시하세요."
        );

        Transport.send(message);
        return code; 
    }
}