package payment.processing.service
import grails.gorm.transactions.Transactional

@Transactional
class PaymentService {
    PaymentTransaction createPayment(
            String apiKey,
            String reference,
            BigDecimal amount,
            String currency,
            String description
    ) {
        Merchant merchant = Merchant.findByApiKeyAndActive(apiKey, true)

        if (!merchant) {
            throw new IllegalArgumentException('Invalid API key')
        }

        PaymentTransaction payment = new PaymentTransaction(
                reference: reference,
                amount: amount,
                currency: currency,
                description: description,
                status: PaymentStatus.PENDING,
                merchant: merchant
        )

        payment.save(failOnError: true, flush: true)
        return payment
    }
    PaymentTransaction capturePayment(String apiKey, String reference) {
        Merchant merchant = Merchant.findByApiKeyAndActive(apiKey, true)

        if (!merchant) {
            throw new IllegalArgumentException('Invalid API key')
        }

        PaymentTransaction payment =
                PaymentTransaction.findByReferenceAndMerchant(reference, merchant)

        if (!payment) {
            throw new IllegalArgumentException('Payment not found')
        }

        if (payment.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    'Only PENDING payments can be captured'
            )
        }

        payment.status = PaymentStatus.SUCCESS
        payment.save(failOnError: true, flush: true)

        return payment
    }
    PaymentTransaction refundPayment(String apiKey, String reference) {
        Merchant merchant = Merchant.findByApiKeyAndActive(apiKey, true)

        if (!merchant) {
            throw new IllegalArgumentException('Invalid API key')
        }

        PaymentTransaction payment =
                PaymentTransaction.findByReferenceAndMerchant(reference, merchant)

        if (!payment) {
            throw new IllegalArgumentException('Payment not found')
        }

        if (payment.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    'Only SUCCESS payments can be refunded'
            )
        }

        payment.status = PaymentStatus.REFUNDED
        payment.save(failOnError: true, flush: true)

        return payment
    }
    PaymentTransaction getPayment(String apiKey, String reference) {
        Merchant merchant = Merchant.findByApiKeyAndActive(apiKey, true)

        if (!merchant) {
            throw new IllegalArgumentException('Invalid API key')
        }

        PaymentTransaction payment =
                PaymentTransaction.findByReferenceAndMerchant(reference, merchant)

        if (!payment) {
            throw new IllegalArgumentException('Payment not found')
        }

        return payment
    }
    List<PaymentTransaction> listPayments(
            String apiKey,
            PaymentStatus status,
            Date fromDate,
            Date toDate,
            Integer max,
            Integer offset
    ) {
        Merchant merchant = Merchant.findByApiKeyAndActive(apiKey, true)

        if (!merchant) {
            throw new IllegalArgumentException('Invalid API key')
        }

        return PaymentTransaction.createCriteria().list(
                max: max,
                offset: offset
        ) {
            eq('merchant', merchant)

            if (status) {
                eq('status', status)
            }

            if (fromDate) {
                ge('dateCreated', fromDate)
            }

            if (toDate) {
                lt('dateCreated', toDate)
            }

            order('dateCreated', 'desc')
        } as List<PaymentTransaction>
    }
}
