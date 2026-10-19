package com.meetbeat.matching.config;

import org.neo4j.driver.exceptions.ClientException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                erros.put(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
    }

    // Violação das restrições de unicidade do banco (RestricoesBanco): acontece quando duas
    // requisições tentam gravar o mesmo nome de usuário ou e-mail ao mesmo tempo
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDuplicidade(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Nome de usuário ou e-mail já está em uso");
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<String> handleErroNeo4j(ClientException ex) {
        if ("Neo.ClientError.Schema.ConstraintValidationFailed".equals(ex.code())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nome de usuário ou e-mail já está em uso");
        }
        throw ex;
    }
}
