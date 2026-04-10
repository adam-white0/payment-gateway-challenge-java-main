package com.checkout.payment.gateway.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurrencyValidatorTest {

        private CurrencyValidator validator;
        private ConstraintValidatorContext context;

        @BeforeEach
        void setUp() {
            validator = new CurrencyValidator();
            context = mock(ConstraintValidatorContext.class);

            ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder =
                    mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContext =
                    mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);


            when(context.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
            when(constraintViolationBuilder.addPropertyNode(any())).thenReturn(nodeBuilderCustomizableContext);
            when(nodeBuilderCustomizableContext.addConstraintViolation()).thenReturn(context);
        }

        @Test
        void shouldReturnTrue_whenSupportedCurrencyProvided() {
            assertTrue(validator.isValid("GBP", context));
        }

        @Test
        void shouldReturnFalse_whenUnsupportedCurrencyProvided() {
            assertFalse(validator.isValid("KRW", context));
        }
    }