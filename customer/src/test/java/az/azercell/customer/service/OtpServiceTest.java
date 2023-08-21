package az.azercell.customer.service;

import az.azercell.customer.dto.OtpDTO;
import az.azercell.customer.exceptions.OtpVerificationException;
import az.azercell.customer.model.Otp;
import az.azercell.customer.repository.OtpRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OtpServiceTest {

    @InjectMocks
    private OtpService otpService;

    @Mock
    private OtpRepository otpRepository;

    @Test
    public void testVerifyOtp_ValidOtp() {
        // Arrange
        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setGsmNumber("1234567890");
        otpDTO.setOtpCode(1234);

        Otp otp = new Otp();
        otp.setOtpCode(1234);
        otp.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        when(otpRepository.findByGsmNumber(anyString())).thenReturn(Collections.singletonList(otp));

        // Act
        boolean result = otpService.verifyOtp(otpDTO);

        // Assert
        assertTrue(result);
        verify(otpRepository, times(1)).findByGsmNumber("1234567890");
    }


    @Test
    public void testVerifyOtp_ExpiredOtp() {
        // Arrange
        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setGsmNumber("1234567890");
        otpDTO.setOtpCode(1234);
        Otp otp = new Otp();
        otp.setOtpCode(1234);
        otp.setExpiredAt(LocalDateTime.now().minusMinutes(1)); // Expired OTP
        when(otpRepository.findByGsmNumber(anyString())).thenReturn(Collections.singletonList(otp));

        // Act and Assert
        try {
            boolean result = otpService.verifyOtp(otpDTO);
            assertFalse(result);
        } catch (OtpVerificationException e) {
            assertEquals("OTP verification timed out.", e.getMessage());
        }

        verify(otpRepository, times(1)).findByGsmNumber("1234567890");
    }

    @Test
    void testIsOtpBlockedOrInvalid_BlockedOtp() {
        // Arrange
        Otp blockedOtp = new Otp();
        blockedOtp.setBlocked(true);

        // Act

        try {
            boolean result = otpService.isOtpBlockedOrInvalid(blockedOtp);
            assertFalse(result);
        } catch (OtpVerificationException e) {
            assertEquals("OTP verification limit exceeded.", e.getMessage());
        }

    }

    @Test
    void testIsOtpBlockedOrInvalid_ValidOtp() {
        // Arrange
        Otp validOtp = new Otp();
        validOtp.setBlocked(false);

        // Act
        boolean result = otpService.isOtpBlockedOrInvalid(validOtp);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsOtpExpired_ExpiredOtp() {
        // Arrange
        Otp expiredOtp = new Otp();
        expiredOtp.setExpiredAt(LocalDateTime.now().minusMinutes(1));

        // Act
        try {
            boolean result = otpService.isOtpExpired(expiredOtp);
            assertFalse(result);
        } catch (OtpVerificationException e) {
            assertEquals("OTP verification timed out.", e.getMessage());
        }

    }

    @Test
    void testIsOtpExpired_ValidOtp() {
        // Arrange
        Otp validOtp = new Otp();
        validOtp.setExpiredAt(LocalDateTime.now().plusMinutes(5));

        // Act
        boolean result = otpService.isOtpExpired(validOtp);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsOtpCodeValid_ValidOtpCode() {
        // Arrange
        Otp validOtp = new Otp();
        validOtp.setOtpCode(1234);

        // Act
        boolean result = otpService.isOtpCodeValid(validOtp, 1234);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsOtpCodeValid_InvalidOtpCode() {
        // Arrange
        Otp validOtp = new Otp();
        validOtp.setOtpCode(1234);

        // Act
        boolean result = otpService.isOtpCodeValid(validOtp, 5678);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIncrementOtpAttempts_NotBlocked() {
        // Arrange
        Otp validOtp = new Otp();
        validOtp.setGsmNumber("1234567890");
        validOtp.setAttack(2); // Attack count: 2 (below MAX_ATTEMPTS)

        when(otpRepository.findByGsmNumber(anyString())).thenReturn(Collections.singletonList(validOtp));

        // Act
        otpService.incrementOtpAttempts("1234567890");

        // Assert
        verify(otpRepository, times(1)).findByGsmNumber("1234567890");
        verify(otpRepository, times(1)).save(any(Otp.class));
        assertEquals(3, validOtp.getAttack());
        assertTrue(validOtp.isBlocked());
    }

    @Test
    void testIncrementOtpAttempts_Blocked() {
        // Arrange
        Otp blockedOtp = new Otp();
        blockedOtp.setGsmNumber("9876543210");
        blockedOtp.setAttack(3 - 1); // Attack count: MAX_ATTEMPTS - 1 (below MAX_ATTEMPTS)

        when(otpRepository.findByGsmNumber(anyString())).thenReturn(Collections.singletonList(blockedOtp));

        // Act
        otpService.incrementOtpAttempts("9876543210");

        // Assert
        verify(otpRepository, times(1)).findByGsmNumber("9876543210");
        verify(otpRepository, times(1)).save(any(Otp.class));
        assertEquals(3, blockedOtp.getAttack());
        assertTrue(blockedOtp.isBlocked());
    }

    @Test
    void testGenerateRandomCode() {
        // Act
        Integer otpCode = otpService.generateRandomCode();

        // Assert
        assertTrue(otpCode >= 1000 && otpCode <= 9999);
    }

    @Test
    void testGenerateOtp() {
        // Arrange
        String gsmNumber = "1234567890";
        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setGsmNumber(gsmNumber);
        when(otpRepository.save(any(Otp.class))).thenReturn(new Otp());

        // Act
        Integer generatedOtpCode = otpService.generateOtp(gsmNumber);

        // Assert
        verify(otpRepository, times(1)).save(any(Otp.class));
        assertTrue(generatedOtpCode >= 1000 && generatedOtpCode <= 9999);
    }

    @Test
    void testRetrieveOtpFromDatabase() {
        // Arrange
        String phoneNumber = "1234567890";
        Otp otp1 = new Otp();
        otp1.setGsmNumber(phoneNumber);
        Otp otp2 = new Otp();
        otp2.setGsmNumber(phoneNumber);
        when(otpRepository.findByGsmNumber(phoneNumber)).thenReturn(Arrays.asList(otp1, otp2));

        // Act
        List<Otp> otpList = otpService.retrieveOtpFromDatabase(phoneNumber);

        // Assert
        verify(otpRepository, times(1)).findByGsmNumber(phoneNumber);
        assertEquals(2, otpList.size());
    }

}