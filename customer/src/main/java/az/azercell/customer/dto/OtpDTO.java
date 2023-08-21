package az.azercell.customer.dto;

import az.azercell.customer.model.Otp;
import az.azercell.customer.model.base.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class OtpDTO extends BaseDTO {

    private Long id;
    @NonNull
    @NotBlank
    private String gsmNumber;
    private Integer otpCode;
    private int attack;
    private boolean isBlocked;

    private LocalDateTime expiredAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    public OtpDTO(Otp entity) {
        this.id = entity.getId();
        this.gsmNumber = entity.getGsmNumber();
        this.otpCode = entity.getOtpCode();
        this.attack = entity.getAttack();
        this.isBlocked = entity.isBlocked();
        this.expiredAt = entity.getExpiredAt();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

}
