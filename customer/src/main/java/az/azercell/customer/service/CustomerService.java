package az.azercell.customer.service;

import az.azercell.customer.dto.CustomerDTO;
import az.azercell.customer.exceptions.CustomerNotFoundException;
import az.azercell.customer.generic.GenericServiceImpl;
import az.azercell.customer.model.Customer;
import az.azercell.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService extends GenericServiceImpl<CustomerDTO, Customer> {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        super(repository, CustomerDTO::new, CustomerService::dtoToEntity);
        this.repository = repository;
    }


    @Override
    public Customer save(CustomerDTO dto) {
        Customer entity = dtoToEntity(dto, Optional.empty());
        entity.setCreatedAt(LocalDateTime.now());
        Customer saved = repository.save(entity);
        return saved;
    }


    @Override
    public Optional<CustomerDTO> update(Long id, CustomerDTO dto) {
        return Optional
                .of(repository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(
                        entity ->
                        {
                            dto.setId(entity.getId());
                            entity = dtoToEntity(dto, Optional.of(entity));
                            return repository.save(entity);
                        }
                )
                .map(CustomerDTO::new);
    }

    @Override
    public void delete(Long id) {
        repository.findById(id).ifPresent(entity ->
        {
            repository.deleteById(id);
        });
    }

    public Customer getCustomerByGsmNumber(String gsmNumber) {

        try {
            List<Customer> customers = repository.findByGsmNumber(gsmNumber);
            Customer customer = customers.stream()
                    .reduce((first, last) -> last)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found with GSM number: " + gsmNumber));
            return customer;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while retrieving customer information.", e);
        }
    }

    private static Customer dtoToEntity(CustomerDTO dto, Optional<Customer> existingEntity) {
        Customer entity = existingEntity.orElseGet(Customer::new);
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setSurname(dto.getSurname());
        entity.setBirthdate(dto.getBirthdate());
        entity.setBalance(dto.getBalance());
        entity.setGsmNumber(dto.getGsmNumber());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }

}