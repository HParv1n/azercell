package az.azercell.customer.controller;

import az.azercell.customer.dto.CustomerDTO;
import az.azercell.customer.generic.GenericController;
import az.azercell.customer.model.Customer;
import az.azercell.customer.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController extends GenericController<CustomerDTO, Customer> {

    private CustomerService service;

    public CustomerController(CustomerService service) {
        super(service);
        this.service = service;

    }

    @GetMapping("/{gsmNumber}")
    public ResponseEntity<Customer> getCustomerByGsmNumber(@RequestParam(value="gsmNumber") String gsmNumber){
        return ResponseEntity.ok(service.getCustomerByGsmNumber(gsmNumber));
    }


}