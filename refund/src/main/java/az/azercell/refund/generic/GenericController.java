package az.azercell.refund.generic;


import az.azercell.refund.model.base.BaseDTO;
import az.azercell.refund.model.base.BaseEntity;
import az.azercell.refund.util.QueryProcessingUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

public abstract class GenericController<T extends BaseDTO, E extends BaseEntity> {
    private final GenericService<T, E> genericService;

    public GenericController(GenericService<T, E> genericService) {
        this.genericService = genericService;
    }

    @GetMapping
    public ResponseEntity<Object> findAll(
            @RequestParam(value = "take", required = false, defaultValue = "100") int take,
            @RequestParam(value = "skip", required = false, defaultValue = "0") int skip,
            @RequestParam(value = "sort", required = false, defaultValue = "[]") String sort) throws IOException {

        JsonNode sortNode = QueryProcessingUtils.parseSortParameter(sort);

        Sort sortObj = QueryProcessingUtils.resolveSortCriteria(sortNode);
        Pageable pageable = QueryProcessingUtils.createPageRequest(take, skip, sortObj);

        Function<Pageable, Page<T>> findAllFunction = genericService::findAll;

        Page<T> page = QueryProcessingUtils.loadPageResults(pageable, findAllFunction);

        return QueryProcessingUtils.buildPageResponseEntity(page);
    }


    @GetMapping("/{id}")
    public ResponseEntity<T> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(genericService.findById(id));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<E> save(@Validated @RequestBody T baseDTO) throws URISyntaxException {

        E newData = genericService.save(baseDTO);
        return ResponseEntity
                .created(new URI(requestMappingValueOfChildClass() + "/" + newData.getId()))
                .body(newData);
    }

    private String requestMappingValueOfChildClass() {
        return getClass().getAnnotation(RequestMapping.class).value()[0];
    }

}
