package az.azercell.purchase.generic;

import az.azercell.purchase.repository.BaseJpaSpecificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Transactional
public abstract class GenericServiceImpl<T, E> implements GenericService<T, E>
{

    private final BaseJpaSpecificationRepository<E, Long> repository;
    private final Function<E, T> entityToDtoMapper;
    protected BiFunction<T, Optional<E>, E> dtoToEntityMapper;

    public GenericServiceImpl(BaseJpaSpecificationRepository<E, Long> repository,
                              Function<E, T> entityToDtoMapper,
                              BiFunction<T, Optional<E>, E> dtoToEntityMapper)
    {
        this.repository = repository;
        this.entityToDtoMapper = entityToDtoMapper;
        this.dtoToEntityMapper = dtoToEntityMapper;
    }

    @Override
    public E save(T dto)
    {
        E entity = dtoToEntityMapper.apply(dto, Optional.empty());

        return repository.save(entity);
    }

    @Override
    public Optional<T> update(Long id, T dto)
    {
        Optional<E> optionalEntity = repository.findById(id);

        if (optionalEntity.isPresent())
        {
            E updatedEntity = dtoToEntityMapper.apply(dto, optionalEntity);
            E savedEntity   = repository.save(updatedEntity);
            return Optional.of(entityToDtoMapper.apply(savedEntity));
        }

        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public T findById(Long id)
    {
        return repository.findById(id).map(entityToDtoMapper).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable)
    {
        return repository.findAll(pageable).map(entityToDtoMapper);
    }


    @Override
    public void delete(Long id)
    {
        E entity = repository.findById(id).orElseThrow(EntityNotFoundException::new);
        repository.delete(entity);
    }

}
