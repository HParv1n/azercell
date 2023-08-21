package az.azercell.topup.controller;

import az.azercell.topup.dto.TransactionDTO;
import az.azercell.topup.exceptions.CustomerNotFoundException;
import az.azercell.topup.exceptions.InvalidRefundAmountException;
import az.azercell.topup.exceptions.TokenExpiredException;
import az.azercell.topup.generic.GenericController;
import az.azercell.topup.generic.GenericService;
import az.azercell.topup.model.Transaction;
import az.azercell.topup.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/top-ups")
public class TopUpController extends GenericController<TransactionDTO, Transaction> {


    private TransactionService transactionService;

    public TopUpController(GenericService<TransactionDTO, Transaction> genericService,TransactionService transactionService) {
        super(genericService);
        this.transactionService = transactionService;
    }

    @PostMapping("/add-funds")
    public ResponseEntity<String> addFunds(@RequestHeader("Authorization") String jwtToken,
                                           @RequestParam double amount) {
        try {
            String result = transactionService.addFunds(jwtToken, amount);
            return ResponseEntity.ok(result);
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has expired.");
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found.");
        } catch (InvalidRefundAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid amount for TopUp.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

}