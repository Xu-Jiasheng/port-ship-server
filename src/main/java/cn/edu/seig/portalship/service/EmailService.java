package cn.edu.seig.portalship.service;

public interface EmailService {

    boolean sendEmail(String to, String subject, String content);

    String sendVerificationCodeEmail(String email);
}
