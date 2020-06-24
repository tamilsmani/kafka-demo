package com.example.config;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;


public class CustomValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		javax.validation.Validator validator = factory.getValidator();
		
		
		Set<ConstraintViolation<EventMessage>> violations = validator.validate((EventMessage)target);
        
        for (ConstraintViolation<EventMessage> violation : violations) {
            String message = violation.getMessage();
            
            SpringValidatorAdapter springValidator = new SpringValidatorAdapter(validator);
            springValidator.validate(target, errors);
            
        }    
        SpringValidatorAdapter springValidator = new SpringValidatorAdapter(validator);
        springValidator.validate(target, errors);
        
		System.out.println(errors);
	}
}