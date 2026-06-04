package Homepage.practice.Payment;

import Homepage.practice.Event.DTO.OrderPaidEvent;
import Homepage.practice.Event.OrderEventPublisher;
import Homepage.practice.Exception.AmountNotMatch;
import Homepage.practice.Exception.OrderNotFound;
import Homepage.practice.Order.Order;
import Homepage.practice.Order.OrderRepository;
import Homepage.practice.Payment.DTO.PaymentConfirmRequest;
import Homepage.practice.Payment.DTO.PaymentConfirmResponse;
import Homepage.practice.Payment.DTO.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final OrderEventPublisher orderEventPublisher;
    // 외부에서 실행될 API까지 DB 트랜잭션 안에서 실행될 가능성이 있으므로, 트랜잭션 분리가 필요
    private final TransactionTemplate transactionTemplate;

    public PaymentResponse confirmPayment(Long userId, PaymentConfirmRequest request) {
        // 승인 전 준비 과정
        PaymentConfirmRequest command = transactionTemplate.execute(status -> preparePayment(userId, request));

        // 결제 객체 생성 후  외부  Payment API 호출하여 승인 진행
        PaymentConfirmResponse result = paymentGateway.confirm(command);

        // 승인 후 완료 처리
        return transactionTemplate.execute(status -> completePayment(userId, request.getOrderId(), result));
    }

    /** 결제 승인을 받기 전 준비 */
    private PaymentConfirmRequest preparePayment(Long userId, PaymentConfirmRequest request) {
        // 주문 조회
        Order order = orderRepository.findOrderForPayment(request.getOrderId(), userId)
                .orElseThrow(() -> new OrderNotFound("아이디에 해당하는 주문이 없습니다."));

        // 주문 금액과 결제 요청 금액이 일치하지 않으면 예외 발생
        if (order.getTotalPrice().compareTo(request.getAmount()) != 0) {
            throw new AmountNotMatch("결제 요청 금액이 주문 금액과 일치하지 않습니다.");
        }

        // Payment 조회 또는 생성
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> paymentRepository.save(Payment.create(order, order.getTotalPrice())));

        // 이미 승인된 결제라면 이미 승인된 결제를 반환
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            return new PaymentConfirmRequest(order.getId(), order.getTotalPrice(), "ALREADY_APPROVED");
        }

        return new PaymentConfirmRequest(order.getId(), order.getTotalPrice(), request.getMockResult());
    }

    /** 결제 승인 이후 완료 처리 */
    private PaymentResponse completePayment(Long userId, Long orderId, PaymentConfirmResponse result) {
        // 트랜잭션이 나누어져 있기 때문에, 외부 API 호출 후 주문 상태가 바뀌었을 수 있으므로 재조회
        Order order = orderRepository.findOrderForPayment(orderId, userId)
                .orElseThrow(() -> new OrderNotFound("아이디에 해당하는 주문이 없습니다."));

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> paymentRepository.save(Payment.create(order, order.getTotalPrice())));

        //이미 승인되어있다면 바로 리턴
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            return PaymentResponse.from(payment);
        }

        // 승인이 되었는지 되지 않았는지에 따라 분기
        if (result.isSuccess()) {
            payment.approve(result.getPaymentKey());
            order.markPaid();

            // 승인 성공 시 OrderPaidEvent 발행
            OrderPaidEvent event = new OrderPaidEvent(
                    UUID.randomUUID().toString(),               // Event ID
                    order.getId(),
                    order.getUser().getId(),
                    order.getUser().getUsername(),
                    payment.getId(),
                    payment.getPaymentKey(),
                    payment.getAmount(),
                    payment.getApprovedAt()
            );
            orderEventPublisher.publishOrderPaidAfterCommit(event);
        } else {
            payment.fail(result.getMessage());
            order.failPayment();
        }

        return PaymentResponse.from(payment);
    }
}
