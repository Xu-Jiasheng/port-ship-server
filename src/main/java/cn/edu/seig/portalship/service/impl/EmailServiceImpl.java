package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public boolean sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            javaMailSender.send(message);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

    @Override
    public String sendVerificationCodeEmail(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String subject = "智慧港口船舶信息管理系统 - 验证码";
        String content = "<h3>您的验证码是：<b>" + code + "</b></h3><p>验证码5分钟内有效，请勿泄露给他人。</p>";
        boolean sent = sendEmail(email, subject, content);
        return sent ? code : null;
    }
}
