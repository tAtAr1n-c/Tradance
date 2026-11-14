package tradance.repository;

import tradance.models.PaymentMethod;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodsRepository {
    List<PaymentMethod> findAll();
    Optional<PaymentMethod> findById(Long id);
}
