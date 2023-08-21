package az.azercell.customer.generic;


import az.azercell.customer.exceptions.ForeignKeyException;
import az.azercell.customer.model.base.BaseDTO;
import az.azercell.customer.model.base.BaseEntity;
import az.azercell.customer.util.QueryProcessingUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
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


    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable Long id, @Validated @RequestBody T baseDTO)
    {
        Optional<T> optional = genericService.update(id, baseDTO);
        return optional.map((response) ->
                        ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<E> save(@Validated @RequestBody T baseDTO) throws URISyntaxException
    {

        E newData = genericService.save(baseDTO);
        return ResponseEntity
                .created(new URI(requestMappingValueOfChildClass() + "/" + newData.getId()))
                .body(newData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws ClassNotFoundException {
        try
        {
            genericService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch (DataIntegrityViolationException ex)
        {
            throw new ForeignKeyException(id, getEntityClassFromGenericControllerArgument(getClass()));
        } catch (EntityNotFoundException e)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private String requestMappingValueOfChildClass()
    {
        return getClass().getAnnotation(RequestMapping.class).value()[0];
    }

    private Class<? extends BaseEntity> getEntityClassFromGenericControllerArgument(Class<?> controllerClass) throws ClassNotFoundException
    {
        String entityName = ((ParameterizedType) controllerClass.getGenericSuperclass()).getActualTypeArguments()[1].getTypeName();
        //noinspection unchecked
        return (Class<? extends BaseEntity>) Class.forName(entityName);
    }

}
