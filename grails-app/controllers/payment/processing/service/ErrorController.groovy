
package payment.processing.service

import grails.converters.JSON
import grails.validation.ValidationException

class ErrorController {

    def handleError() {
        Throwable exception = request.getAttribute(
                'javax.servlet.error.exception'
        )

        while (exception?.cause &&
                !(exception instanceof ApiException) &&
                !(exception instanceof ValidationException) &&
                !(exception instanceof IllegalArgumentException)) {
            exception = exception.cause
        }

        if (exception instanceof ApiException) {
            renderApiError(exception.error, exception.message)
            return
        }

        if (exception instanceof ValidationException) {
            String message = getValidationMessage(exception)
            renderApiError(ApiError.INVALID_PAYMENT_DATA, message)
            return
        }

        if (exception instanceof IllegalArgumentException) {
            renderApiError(ApiError.INVALID_PAYMENT_DATA)
            return
        }

        render(
                status: 500,
                contentType: 'application/json',
                text: [
                        message: 'Internal server error',
                        error  : 500
                ] as JSON
        )
    }

    private String getValidationMessage(ValidationException exception) {
        def fieldError = exception.errors?.fieldErrors?.find()

        if (!fieldError) {
            return 'Invalid payment data'
        }

        switch (fieldError.field) {
            case 'amount':
                return 'Amount must be greater than 0'

            case 'reference':
                return fieldError.code?.contains('unique')
                        ? 'Reference must be unique'
                        : 'Reference is required'

            case 'currency':
                return 'Currency is required'

            case 'merchant':
                return 'Merchant is required'

            default:
                return 'Invalid payment data'
        }
    }

    private void renderApiError(ApiError apiError, String message = null) {
        render(
                status: apiError.httpStatus,
                contentType: 'application/json',
                text: [
                        errorCode: apiError.code,
                        error    : message ?: apiError.message
                ] as JSON
        )
    }
}
