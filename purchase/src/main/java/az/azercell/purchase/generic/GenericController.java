package az.azercell.purchase.generic;


import az.azercell.purchase.model.base.BaseDTO;
import az.azercell.purchase.model.base.BaseEntity;
import az.azercell.purchase.util.QueryProcessingUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.function.Function;

public abstract class GenericController<T extends BaseDTO, E extends BaseEntity>
{
    private final GenericService<T, E> genericService;

    public GenericController(GenericService<T, E> genericService)
    {
        this.genericService = genericService;
    }

    @GetMapping
    public ResponseEntity<Object> findAll(
            @RequestParam(value = "take", required = false, defaultValue = "100") int take,
            @RequestParam(value = "skip", required = false, defaultValue = "0") int skip,
            @RequestParam(value = "sort", required = false, defaultValue = "[]") String sort) throws IOException
    {

        JsonNode sortNode   = QueryProcessingUtils.parseSortParameter(sort);

        Sort sortObj  = QueryProcessingUtils.resolveSortCriteria(sortNode);
        Pageable pageable = QueryProcessingUtils.createPageRequest(take, skip, sortObj);

        Function<Pageable, Page<T>> findAllFunction = genericService::findAll;

        Page<T> page = QueryProcessingUtils.loadPageResults(pageable, findAllFunction);

        return QueryProcessingUtils.buildPageResponseEntity(page);
    }


    @GetMapping("/{id}")
    public ResponseEntity<T> findById(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(genericService.findById(id));
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
