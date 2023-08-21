package az.azercell.customer.service;

import az.azercell.customer.dto.CustomerDTO;
import az.azercell.customer.dto.OtpDTO;
import az.azercell.customer.exceptions.OtpVerificationException;
import az.azercell.customer.generic.GenericServiceImpl;
import az.azercell.customer.model.Customer;
import az.azercell.customer.model.Otp;
import az.azercell.customer.repository.CustomerRepository;
import az.azercell.customer.repository.OtpRepository;
import liquibase.pro.packaged.O;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OtpService extends GenericServiceImpl<OtpDTO, Otp> {

    private final OtpRepository repository;
    private static final int MAX_ATTEMPTS = 3;

    public OtpService(OtpRepository repository) {
        super(repository, OtpDTO::new, OtpService::dtoToEntity);
        this.repository = repository;
    }


    public boolean verifyOtp(OtpDTO otpDTO) {
        List<Otp> otpList = retrieveOtpFromDatabase(otpDTO.getGsmNumber());
        Otp otp = otpList.isEmpty() ? null : otpList.get(otpList.size() - 1);

        if (isOtpBlockedOrInvalid(otp)) {
            return false;
        }

        if (isOtpExpired(otp)) {
            return false;
        }

        if (isOtpCodeValid(otp, otpDTO.getOtpCode())) {
            return true;
        } else {
            incrementOtpAttempts(otpDTO.getGsmNumber());
            return false;
        }
    }

    protected boolean isOtpBlockedOrInvalid(Otp otp) {
        if (otp == null || otp.isBlocked()) {
            handleOtpVerificationError("OTP verification limit exceeded.");
            return true;
        }
        return false;
    }

    protected boolean isOtpExpired(Otp otp) {
        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            handleOtpVerificationError("OTP verification timed out.");
            return true;
        }
        return false;
    }

    protected boolean isOtpCodeValid(Otp otp, Integer otpCode) {
        if (otpCode.equals(otp.getOtpCode())) {
            return true;
        }
        incrementOtpAttempts(otp.getGsmNumber());
        return false;
    }

    private void handleOtpVerificationError(String errorMessage) {
        throw new OtpVerificationException(errorMessage);
    }


    protected void incrementOtpAttempts(String gsmNumber) {
        Optional<Otp> lastOtp = retrieveLastOtpFromDatabase(gsmNumber);

        lastOtp.ifPresent(otp -> {
            int newAttack = otp.getAttack() + 1;
            otp.setAttack(newAttack);

            if (newAttack >= MAX_ATTEMPTS) {
                otp.setBlocked(true);
            }
            repository.save(otp);
        });
    }

    private Optional<Otp> retrieveLastOtpFromDatabase(String gsmNumber) {
        List<Otp> otpList = retrieveOtpFromDatabase(gsmNumber);
        return otpList.isEmpty() ? Optional.empty() : Optional.of(otpList.get(otpList.size() - 1));
    }


    public Integer generateOtp(String gsmNumber) {
        Integer otpCode = generateRandomCode();

        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setGsmNumber(gsmNumber);
        otpDTO.setOtpCode(otpCode);
        otpDTO.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        save(otpDTO);

        return otpCode;
    }

    protected Integer generateRandomCode() {
        int min = 1000;
        int max = 9999;
        int randomCode = min + (int) (Math.random() * ((max - min) + 1));

        return randomCode;
    }

    @Override
    public Otp save(OtpDTO dto) {
        Otp entity = dtoToEntity(dto, Optional.empty());
        entity.setCreatedAt(Date.valueOf(LocalDate.now()));
        Otp saved = repository.save(entity);
        return saved;
    }

    protected List<Otp> retrieveOtpFromDatabase(String phoneNumber) {
        return repository.findByGsmNumber(phoneNumber);
    }

    private static Otp dtoToEntity(OtpDTO dto, Optional<Otp> existingEntity) {
        Otp entity = existingEntity.orElseGet(Otp::new);
        entity.setId(dto.getId());
        entity.setAttack(dto.getAttack());
        entity.setOtpCode(dto.getOtpCode());
        entity.setGsmNumber(dto.getGsmNumber());
        entity.setExpiredAt(dto.getExpiredAt());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }
}

