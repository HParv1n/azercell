package az.azercell.refund.exceptions;

import az.azercell.refund.model.base.BaseEntity;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.AbstractThrowableProblem;

@Getter
@ResponseStatus(code = HttpStatus.CONFLICT, value = HttpStatus.CONFLICT)
public class ForeignKeyException extends AbstractThrowableProblem {

    private final Long id;
    private final Class<? extends BaseEntity> entityClass;

    public ForeignKeyException(Long id, Class<? extends BaseEntity> entityClass) {
        this.id = id;
        this.entityClass = entityClass;
    }
}
