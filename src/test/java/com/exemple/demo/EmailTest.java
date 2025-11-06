package com.exemple.demo;

import com.example.demo.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailTest {
/*
    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);

        // Injecter une valeur dans le champ privé "fromEmail"
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
    }

    @Test
    void testSendVerificationEmail_Success() {
        // Arrange
        String toEmail = "user@test.com";
        String verificationCode = "123456";

        // Act
        emailService.sendVerificationEmail(toEmail, verificationCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("noreply@test.com", sentMessage.getFrom());
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals("Vérification de votre compte", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(verificationCode));
    }

    @Test
    void testSendVerificationEmail_WhenMailSenderThrowsException() {
        // Arrange
        String toEmail = "user@test.com";
        String verificationCode = "123456";

        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act + Assert
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(toEmail, verificationCode));

        // Vérifie qu'un appel a bien été tenté malgré l'erreur
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
*/
}
