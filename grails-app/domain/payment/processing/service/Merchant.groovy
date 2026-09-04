package payment.processing.service

class Merchant {
    String name
    String email
    String apiKey
    Boolean active = true

    Date dateCreated
    Date lastUpdated

    static hasMany = [
            paymentTransactions: PaymentTransaction
    ]

    static constraints = {
        name blank: false
        email blank: false, email: true, unique: true
        apiKey blank: false, unique: true
        active nullable: false
    }

    static mapping = {
        apiKey index: 'merchant_api_key_idx'
    }

}
