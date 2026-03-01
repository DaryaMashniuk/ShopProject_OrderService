package com.innowise.orderservice.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {
  private Class<? extends Enum<?>> enumClass;
  private boolean ignoreCase;

  @Override
  public void initialize(ValidEnum annotation) {
    this.enumClass = annotation.enumClass();
    this.ignoreCase = annotation.ignoreCase();
  }

  @Override
  public boolean isValid(String orderStatus, ConstraintValidatorContext constraintValidatorContext) {
    if (orderStatus == null) {
      return true;
    }
    return Arrays.stream(enumClass.getEnumConstants())
            .anyMatch(e -> ignoreCase
            ? e.name().equalsIgnoreCase(orderStatus)
            : e.name().equals(orderStatus));
  }
}
