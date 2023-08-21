package az.azercell.customer.generic;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import java.util.Collections;
import java.util.Optional;

public interface GenericService<T, E>
{
    E save(T dto);

    Page<T> findAll(Pageable pageable);

    T findById(Long id);

    Optional<T> update(Long id, T dto);

    void delete(Long id);

}
