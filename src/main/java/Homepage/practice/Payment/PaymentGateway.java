package Homepage.practice.Payment;

import Homepage.practice.Payment.DTO.PaymentConfirmRequest;
import Homepage.practice.Payment.DTO.PaymentConfirmResponse;


public interface PaymentGateway {
    PaymentConfirmResponse confirm(PaymentConfirmRequest command);
}
