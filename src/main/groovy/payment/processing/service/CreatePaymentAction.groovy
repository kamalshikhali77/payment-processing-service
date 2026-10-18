
package payment.processing.service

class CreatePaymentAction {

    private final PaymentService paymentService

    CreatePaymentAction(PaymentService paymentService) {
        this.paymentService = paymentService
    }

    PaymentTransaction execute(
            String apiKey,
            CreatePaymentCommand command
    ) {
        return paymentService.createPayment(
                apiKey,
                command.reference,
                command.amount,
                command.currency,
                command.description
        )
    }
}

