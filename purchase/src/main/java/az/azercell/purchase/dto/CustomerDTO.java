package az.azercell.purchase.dto;

import az.azercell.purchase.model.base.BaseDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO extends BaseDTO {

    private Long id;

    private String name;

    private String surname;

    private String birthdate;

    private String gsmNumber;

    private BigDecimal balance;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
