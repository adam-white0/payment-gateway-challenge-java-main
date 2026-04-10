package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.model.ProcessedPaymentResponse;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentsRepository {

  private final HashMap<UUID, ProcessedPaymentResponse> payments = new HashMap<>();

  public void add(ProcessedPaymentResponse payment) {
    payments.put(payment.getId(), payment);
  }

  public Optional<ProcessedPaymentResponse> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

}
