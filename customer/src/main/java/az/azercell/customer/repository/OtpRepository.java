package az.azercell.customer.repository;

import az.azercell.customer.dto.OtpDTO;
import az.azercell.customer.model.Otp;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface OtpRepository extends BaseJpaSpecificationRepository<Otp, Long> {
    List<Otp> findByGsmNumber(String gsmNumber);
}
