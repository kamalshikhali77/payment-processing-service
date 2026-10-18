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
        Merchant merchant = requireMerchant(apiKey)

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
        Merchant merchant = requireMerchant(apiKey)

        PaymentTransaction payment = requirePayment(reference, merchant)

        if (payment.status != PaymentStatus.PENDING) {
            throw new ApiException(ApiError.CAPTURE_NOT_ALLOWED)
        }

        payment.status = PaymentStatus.SUCCESS
        payment.save(failOnError: true, flush: true)

        return payment
    }
    PaymentTransaction refundPayment(String apiKey, String reference) {
        Merchant merchant = requireMerchant(apiKey)

        PaymentTransaction payment = requirePayment(reference, merchant)

        if (payment.status != PaymentStatus.SUCCESS) {
            throw new ApiException(ApiError.REFUND_NOT_ALLOWED)
        }

        payment.status = PaymentStatus.REFUNDED
        payment.save(failOnError: true, flush: true)

        return payment
    }
    PaymentTransaction getPayment(String apiKey, String reference) {

        Merchant merchant = requireMerchant(apiKey)

        PaymentTransaction payment = requirePayment(reference, merchant)

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
        Merchant merchant = requireMerchant(apiKey)

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
    private Merchant requireMerchant(String apiKey) {

        Merchant merchant = Merchant.findByApiKeyAndActive(apiKey, true)

        if (!merchant) {
            throw new ApiException(ApiError.INVALID_API_KEY)
        }

        return merchant
    }
    private PaymentTransaction requirePayment(
            String reference,
            Merchant merchant
    ) {
        PaymentTransaction payment =
                PaymentTransaction.findByReferenceAndMerchant(
                        reference,
                        merchant
                )

        if (!payment) {
            throw new ApiException(ApiError.PAYMENT_NOT_FOUND)
        }

        return payment
    }
}
