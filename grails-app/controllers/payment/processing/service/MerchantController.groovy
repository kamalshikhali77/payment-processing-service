package payment.processing.service


import grails.rest.*
import grails.converters.*
import org.springframework.http.HttpStatus
import grails.validation.ValidationException

class MerchantController {
    static responseFormats = ['json']

    MerchantService merchantService

    def create() { //singeltion
        try {
            def body = request.JSON

            Merchant merchant = merchantService.createMerchant(
                    body.name as String,
                    body.email as String
            )

            render(
                    status: HttpStatus.CREATED.value(),
                    contentType: 'application/json',
                    text: [
                            id         : merchant.id,
                            name       : merchant.name,
                            email      : merchant.email,
                            apiKey     : merchant.apiKey,
                            active     : merchant.active,
                            dateCreated: merchant.dateCreated
                    ] as JSON
            )
        } catch (ValidationException exception) {
            String errorMessage =
                    getMerchantValidationMessage(exception)

            render(
                    status: HttpStatus.BAD_REQUEST.value(),
                    contentType: 'application/json',
                    text: [
                            errorCode: '50',
                            error    : errorMessage
                    ] as JSON
            )
        }
    }
    private String getMerchantValidationMessage(
            ValidationException exception
    ) {
        def fieldError = exception.errors?.fieldErrors?.first()

        if (!fieldError) {
            return 'Invalid merchant data'
        }

        switch (fieldError.field) {
            case 'name':
                return 'Merchant name is required'

            case 'email':
                if (fieldError.code?.contains('unique')) {
                    return 'Email must be unique'
                }

                if (fieldError.rejectedValue) {
                    return 'Email format is invalid'
                }

                return 'Email is required'

            default:
                return 'Invalid merchant data'
        }
    }
}
