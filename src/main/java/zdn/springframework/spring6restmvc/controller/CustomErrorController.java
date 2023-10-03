package zdn.springframework.spring6restmvc.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomErrorController {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        ResponseEntity<List<Map<String, String>>> handleBindErrors(MethodArgumentNotValidException exception){
            List<Map<String, String>> errorList = exception.getFieldErrors().stream()
                    .map(fieldError -> {
                        Map<String, String> errorMap = new HashMap<>();
                        errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
                        return errorMap;
                    }).toList();

            return ResponseEntity.badRequest().body(errorList);
        }

        @ExceptionHandler
        ResponseEntity<List<Map<String, String>>> handleJPAViolations(TransactionSystemException exception){
            ResponseEntity.BodyBuilder responseEntity = ResponseEntity.badRequest();

            if (exception.getCause().getCause() instanceof ConstraintViolationException violationException){

                List<Map<String, String>> errors = violationException.getConstraintViolations().stream()
                        .map(constraintViolation -> {
                            Map<String,String> errMap = new HashMap<>();
                            errMap.put(constraintViolation.getPropertyPath().toString(),
                                    constraintViolation.getMessage());
                            return errMap;
                        }).collect(Collectors.toList());
                return responseEntity.body(errors);
            }
            return responseEntity.build();
        }
}
