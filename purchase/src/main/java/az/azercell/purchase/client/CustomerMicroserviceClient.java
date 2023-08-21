package az.azercell.purchase.client;

import az.azercell.purchase.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "customer-microservice",url = "${client.url}")
public interface CustomerMicroserviceClient {

    @GetMapping(value = "/{gsmNumber}")
    CustomerDTO getCustomerByGsmNumber(@RequestParam(value="gsmNumber") String gsmNumber);



    @PutMapping("/{id}")
    void updateCustomer(@RequestBody CustomerDTO customerDTO, @PathVariable("id") Long id);
}
