package validation;

import com.checkout.payment.gateway.model.ProcessPaymentRequest;
import com.checkout.payment.gateway.validation.ExpiryYearMonthValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExpiryYearMonthValidatorTest {

        private ExpiryYearMonthValidator validator;
        private ConstraintValidatorContext context;

        @BeforeEach
        void setUp() {
            validator = new ExpiryYearMonthValidator();
            context = mock(ConstraintValidatorContext.class);

            ConstraintViolationBuilder constraintViolationBuilder =
                    mock(ConstraintViolationBuilder.class);
            NodeBuilderCustomizableContext nodeBuilderCustomizableContext =
                    mock(NodeBuilderCustomizableContext.class);


            when(context.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
            when(constraintViolationBuilder.addPropertyNode(any())).thenReturn(nodeBuilderCustomizableContext);
            when(nodeBuilderCustomizableContext.addConstraintViolation()).thenReturn(context);
        }

        @Test
        void shouldReturnTrue_whenExpiryIsInFuture() {
            ProcessPaymentRequest request = buildRequest(YearMonth.now().plusMonths(1));
            assertTrue(validator.isValid(request, context));
        }

        @Test
        void shouldReturnFalse_whenExpiryIsInPast() {
            ProcessPaymentRequest request = buildRequest(YearMonth.now().minusMonths(1));
            assertFalse(validator.isValid(request, context));
        }

        @Test
        void shouldReturnFalse_whenExpiryIsCurrentMonth() {
            ProcessPaymentRequest request = buildRequest(YearMonth.now());
            assertFalse(validator.isValid(request, context));
        }

        private ProcessPaymentRequest buildRequest(YearMonth expiry) {
            ProcessPaymentRequest request = new ProcessPaymentRequest();

            request.setExpiryMonth(expiry.getMonthValue());
            request.setExpiryYear(expiry.getYear());

            return request;
        }
    }