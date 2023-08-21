package az.azercell.customer.dto;

import az.azercell.customer.model.Customer;
import az.azercell.customer.model.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO extends BaseDTO {

    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String surname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private String birthdate;

    @NotNull
    @NotBlank
    @Pattern(regexp = "994(?:10|51|50)\\d{7}", message = "GSM number format is invalid")
    private String gsmNumber;

    @NotNull
    @NotBlank
    private BigDecimal balance;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CustomerDTO(Customer entity)
    {
        this.id = entity.getId();
        this.name = entity.getName();
        this.surname = entity.getSurname();
        this.birthdate = entity.getBirthdate();
        this.balance = entity.getBalance();
        this.gsmNumber = entity.getGsmNumber();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }
}
