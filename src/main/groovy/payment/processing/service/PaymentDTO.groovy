
package payment.processing.service

class PaymentDTO {

    Long id
    String reference
    BigDecimal amount
    String currency
    String description
    String status
    Long merchantId
    Date dateCreated
    Date lastUpdated

    static PaymentDTO fromPayment(PaymentTransaction payment) {
        return new PaymentDTO(
                id: payment.id,
                reference: payment.reference,
                amount: payment.amount,
                currency: payment.currency,
                description: payment.description,
                status: payment.status.name(),
                merchantId: payment.merchant.id,
                dateCreated: payment.dateCreated,
                lastUpdated: payment.lastUpdated
        )
    }

    Map toCreateResponse() {
        return [
                id         : id,
                reference  : reference,
                amount     : amount,
                currency   : currency,
                description: description,
                status     : status,
                merchantId : merchantId,
                dateCreated: dateCreated
        ]
    }

    Map toStatusResponse() {
        return [
                id       : id,
                reference: reference,
                status   : status
        ]
    }


}

