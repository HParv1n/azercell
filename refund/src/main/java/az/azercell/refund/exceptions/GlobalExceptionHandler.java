package az.azercell.refund.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(CustomerNotFoundException ex) {
        ExceptionResponse response = new ExceptionResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(TokenExpiredException ex) {
        ExceptionResponse response = new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidRefundAmountException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(InvalidRefundAmountException ex) {
        ExceptionResponse response = new ExceptionResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

}
