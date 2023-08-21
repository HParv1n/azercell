package az.azercell.purchase.controller;

import az.azercell.purchase.dto.TransactionDTO;
import az.azercell.purchase.exceptions.CustomerNotFoundException;
import az.azercell.purchase.exceptions.InvalidRefundAmountException;
import az.azercell.purchase.exceptions.TokenExpiredException;
import az.azercell.purchase.generic.GenericController;
import az.azercell.purchase.generic.GenericService;
import az.azercell.purchase.model.Transaction;
import az.azercell.purchase.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchases")
public class PurchaseController extends GenericController<TransactionDTO, Transaction> {

    private  TransactionService  transactionService;

    public PurchaseController(GenericService<TransactionDTO, Transaction> genericService,TransactionService  transactionService) {
        super(genericService);
        this.transactionService = transactionService;
    }


    @PostMapping("/make-purchase")
    public ResponseEntity<String> makePurchase(@RequestHeader("Authorization") String jwtToken, @RequestParam double amount) {
        try {
            String result = transactionService.makePurchase(jwtToken, amount);
            return ResponseEntity.ok(result);
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has expired.");
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found.");
        } catch (InvalidRefundAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid amount for Purchase.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

}
