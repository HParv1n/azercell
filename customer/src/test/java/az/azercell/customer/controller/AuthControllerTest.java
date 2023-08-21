package az.azercell.customer.controller;

import az.azercell.customer.dto.OtpDTO;
import az.azercell.customer.security.JwtService;
import az.azercell.customer.service.OtpService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private OtpService otpService;

    @Mock
    private JwtService jwtService;

    @Test
    public void testRequestOtp_Success() {
        String gsmNumber = "1234567890";
        int generatedOtp = 123456;

        when(otpService.generateOtp(gsmNumber)).thenReturn(generatedOtp);

        ResponseEntity<String> response = authController.requestOtp(gsmNumber);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(String.valueOf(generatedOtp), response.getBody());
    }

    @Test
    public void testRequestOtp_Error() {
        String gsmNumber = "1234567890";

        when(otpService.generateOtp(gsmNumber)).thenThrow(new RuntimeException("Error generating OTP code."));

        ResponseEntity<String> response = authController.requestOtp(gsmNumber);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error generating OTP code.", response.getBody());
    }

    @Test
    public void testVerifyOtp_ValidOtp() {
        String gsmNumber = "1234567890";
        int otpCode = 123456;
        String token = "generatedJwtToken";

        when(otpService.verifyOtp(any(OtpDTO.class))).thenReturn(true);
        when(jwtService.generateToken(gsmNumber)).thenReturn(token);

        ResponseEntity<String> response = authController.verifyOtp(gsmNumber, otpCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(token, response.getBody());
    }

    @Test
    public void testVerifyOtp_InvalidOtp() {
        String gsmNumber = "1234567890";
        int otpCode = 123456;

        when(otpService.verifyOtp(any(OtpDTO.class))).thenReturn(false);

        ResponseEntity<String> response = authController.verifyOtp(gsmNumber, otpCode);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid OTP code.", response.getBody());
    }
}

