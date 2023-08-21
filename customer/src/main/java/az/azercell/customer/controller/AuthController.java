package az.azercell.customer.controller;

import az.azercell.customer.dto.OtpDTO;
import az.azercell.customer.security.JwtService;
import az.azercell.customer.service.OtpService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@AllArgsConstructor
public class AuthController {

    private final OtpService otpService;
    private final JwtService jwtService;

    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestParam String gsmNumber) {
        try {
            Integer otpCode = otpService.generateOtp(gsmNumber);
            return ResponseEntity.ok(String.valueOf(otpCode));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating OTP code.");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String gsmNumber, @RequestParam Integer otpCode) {
        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setOtpCode(otpCode);
        otpDTO.setGsmNumber(gsmNumber);

        if (otpService.verifyOtp(otpDTO)) {
            String token = jwtService.generateToken(gsmNumber);
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP code.");
        }
    }

}


