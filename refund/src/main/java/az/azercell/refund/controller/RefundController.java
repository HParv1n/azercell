package az.azercell.refund.controller;

import az.azercell.refund.dto.TransactionDTO;
import az.azercell.refund.exceptions.CustomerNotFoundException;
import az.azercell.refund.exceptions.InvalidRefundAmountException;
import az.azercell.refund.exceptions.TokenExpiredException;
import az.azercell.refund.generic.GenericController;
import az.azercell.refund.generic.GenericService;
import az.azercell.refund.model.Transaction;
import az.azercell.refund.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/refunds")
public class RefundController extends GenericController<TransactionDTO, Transaction> {

    private  TransactionService  transactionService;

    public RefundController(GenericService<TransactionDTO, Transaction> genericService, TransactionService  transactionService) {
        super(genericService);
        this.transactionService = transactionService;
    }

    @PostMapping("/make-refund")
    public ResponseEntity<String> makeRefund(@RequestHeader("Authorization") String jwtToken, @RequestParam double amount) {
        try {
            String result = transactionService.makeRefund(jwtToken, amount);
            return ResponseEntity.ok(result);
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has expired.");
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found.");
        } catch (InvalidRefundAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid amount for refund.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

}
