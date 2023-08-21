package az.azercell.customer.model;

import az.azercell.customer.model.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
@Getter
@Setter
@Entity
@Table(name = "otps")
@NoArgsConstructor
@AllArgsConstructor
public class Otp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gsm_number", nullable = false)
    private String gsmNumber;

    @Column(name = "otp_code", nullable = false)
    private Integer otpCode;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "attack", nullable = false)
    private Integer attack = 0;

    private boolean isBlocked = false;

    @Column(name = "created_at", nullable = false,updatable = false)
    @CreatedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @Column(name = "updated_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
}
