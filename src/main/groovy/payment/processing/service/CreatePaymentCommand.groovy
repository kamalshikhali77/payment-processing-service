package payment.processing.service
import grails.validation.Validateable

class CreatePaymentCommand implements Validateable {

    String reference
    BigDecimal amount
    String currency
    String description

    static constraints = {
        reference blank: false, nullable: false
        amount nullable: false, min: 0.01G
        currency blank: false, nullable: false
        description nullable: true, blank: true
    }

    String getValidationMessage() {
        def fieldError = errors?.fieldErrors?.find()

        if (!fieldError) {
            return 'Invalid payment data'
        }

        switch (fieldError.field) {
            case 'amount':
                return 'Amount must be greater than 0'

            case 'reference':
                return 'Reference is required'

            case 'currency':
                return 'Currency is required'

            default:
                return 'Invalid payment data'
        }
    }

}
