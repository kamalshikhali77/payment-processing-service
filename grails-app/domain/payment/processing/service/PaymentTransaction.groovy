package payment.processing.service

class PaymentTransaction {
    String reference
    BigDecimal amount
    String currency
    String description
    PaymentStatus status = PaymentStatus.PENDING

    Merchant merchant // belongs *

    Date dateCreated
    Date lastUpdated
    static constraints = {
        reference blank: false, unique: true
        amount nullable: false, min: 0.01G
        currency blank: false
        description nullable: true, blank: true
        status nullable: false
        merchant nullable: false
    }

    static mapping = {
        amount scale: 2
        reference index: 'payment_reference_idx'
        merchant index: 'payment_merchant_idx'
    }
}
