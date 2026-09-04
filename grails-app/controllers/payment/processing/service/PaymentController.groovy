package payment.processing.service

import grails.converters.JSON
import org.springframework.http.HttpStatus
import java.text.SimpleDateFormat
import grails.validation.ValidationException
class PaymentController {
    static responseFormats = ['json']

    PaymentService paymentService

    def create() {
        try {
            String apiKey = request.getHeader('X-API-KEY')
            def body = request.JSON

            PaymentTransaction payment =
                    paymentService.createPayment(
                            apiKey,
                            body.reference as String,
                            body.amount as BigDecimal,
                            body.currency as String,
                            body.description as String
                    )

            render(
                    status: HttpStatus.CREATED.value(),
                    contentType: 'application/json',
                    text: [
                            id         : payment.id,
                            reference  : payment.reference,
                            amount     : payment.amount,
                            currency   : payment.currency,
                            description: payment.description,
                            status     : payment.status.name(),
                            merchantId : payment.merchant.id,
                            dateCreated: payment.dateCreated
                    ] as JSON
            )
        } catch (ValidationException exception) {
            String errorMessage =
                    getPaymentValidationMessage(exception)

            render(
                    status: HttpStatus.BAD_REQUEST.value(),
                    contentType: 'application/json',
                    text: [
                            errorCode: '40',
                            error    : errorMessage
                    ] as JSON
            )
        } catch (IllegalArgumentException exception) {
            boolean invalidApiKey =
                    exception.message == 'Invalid API key'

            render(
                    status: invalidApiKey
                            ? HttpStatus.UNAUTHORIZED.value()
                            : HttpStatus.BAD_REQUEST.value(),
                    contentType: 'application/json',
                    text: [
                            errorCode: invalidApiKey ? '01' : '40',
                            error    : invalidApiKey
                                    ? exception.message
                                    : 'Invalid payment data'
                    ] as JSON
            )
        }
    }
    def capture() {
        try {
            String apiKey = request.getHeader('X-API-KEY')
            String reference = params.reference

            PaymentTransaction payment =
                    paymentService.capturePayment(apiKey, reference)

            render(
                    status: HttpStatus.OK.value(),
                    contentType: 'application/json',
                    text: [
                            id       : payment.id,
                            reference: payment.reference,
                            status   : payment.status.name()
                    ] as JSON
            )
        } catch (IllegalStateException exception) {
            render(
                    status: HttpStatus.CONFLICT.value(),
                    contentType: 'application/json',
                    text: [
                            errorCode: '11',
                            error    : exception.message
                    ] as JSON
            )
        } catch (IllegalArgumentException exception) {
            boolean invalidApiKey =
                    exception.message == 'Invalid API key'

            render(
                    status: invalidApiKey
                            ? HttpStatus.UNAUTHORIZED.value()
                            : HttpStatus.NOT_FOUND.value(),
                    contentType: 'application/json',
                    text: [
                            errorCode: invalidApiKey ? '01' : '20',
                            error    : exception.message
                    ] as JSON
            )
        }
    }
    def refund() {
        try {
            String apiKey = request.getHeader('X-API-KEY')
            String reference = params.reference

            PaymentTransaction payment =
                    paymentService.refundPayment(apiKey, reference)

            render(
                    status: HttpStatus.OK.value(),
                    contentType: 'application/json',
                    text: [
                            id       : payment.id,
                            reference: payment.reference,
                            status   : payment.status.name()
                    ] as JSON
            )
        } catch (IllegalStateException exception) {
            render(
                    status: HttpStatus.CONFLICT.value(),
                    contentType: 'application/json',
                    text: [
                            errorCode: '10',
                            error    : exception.message
                    ] as JSON
            )
        }
     catch (IllegalArgumentException exception) {
        boolean invalidApiKey =
                exception.message == 'Invalid API key'

        render(
                status: invalidApiKey
                        ? HttpStatus.UNAUTHORIZED.value()
                        : HttpStatus.NOT_FOUND.value(),
                contentType: 'application/json',
                text: [
                        errorCode: invalidApiKey ? '01' : '20',
                        error    : exception.message
                ] as JSON
        )
    }
    }
    def details() {
        try {
            String apiKey = request.getHeader('X-API-KEY')
            String reference = params.reference

            PaymentTransaction payment =
                    paymentService.getPayment(apiKey, reference)

            render(
                    status: HttpStatus.OK.value(),
                    contentType: 'application/json',
                    text: [
                            id         : payment.id,
                            reference  : payment.reference,
                            amount     : payment.amount,
                            currency   : payment.currency,
                            description: payment.description,
                            status     : payment.status.name(),
                            merchantId : payment.merchant.id,
                            dateCreated: payment.dateCreated,
                            lastUpdated: payment.lastUpdated
                    ] as JSON
            )
        } catch (IllegalArgumentException exception) {
            boolean invalidApiKey =
                    exception.message == 'Invalid API key'

            render(
                    status: invalidApiKey
                            ? HttpStatus.UNAUTHORIZED.value()
                            : HttpStatus.NOT_FOUND.value(),
                    contentType: 'application/json',
                    text: [
                            errorCode: invalidApiKey ? '01' : '20',
                            error    : exception.message
                    ] as JSON
            )
        }
    }
    def listAll() {
        try {
            String apiKey = request.getHeader('X-API-KEY')

            PaymentStatus status = params.status
                    ? PaymentStatus.valueOf(params.status.toUpperCase())
                    : null

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat('yyyy-MM-dd')

            dateFormat.setLenient(false)

            Date fromDate = params.fromDate
                    ? dateFormat.parse(params.fromDate)
                    : null

            Date toDate = null

            if (params.toDate) {
                Calendar calendar = Calendar.getInstance()
                calendar.time = dateFormat.parse(params.toDate)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                toDate = calendar.time
            }

            Integer max = params.int('max') ?: 10
            Integer offset = params.int('offset') ?: 0

            max = Math.max(1, Math.min(max, 100))
            offset = Math.max(0, offset)

            List<PaymentTransaction> payments =
                    paymentService.listPayments(
                            apiKey,
                            status,
                            fromDate,
                            toDate,
                            max,
                            offset
                    )

            def response = payments.collect {
                PaymentTransaction payment ->
                    [
                            id         : payment.id,
                            reference  : payment.reference,
                            amount     : payment.amount,
                            currency   : payment.currency,
                            description: payment.description,
                            status     : payment.status.name(),
                            merchantId : payment.merchant.id,
                            dateCreated: payment.dateCreated,
                            lastUpdated: payment.lastUpdated
                    ]
            }

            render(
                    status: HttpStatus.OK.value(),
                    contentType: 'application/json',
                    text: response as JSON
            )
        } catch (java.text.ParseException exception) {
            render(
                    status: HttpStatus.BAD_REQUEST.value(),
                    contentType: 'application/json',
                    text: [
                            errorCode: '31',
                            error    : 'Date must use yyyy-MM-dd format'
                    ] as JSON
            )
        } catch (IllegalArgumentException exception) {
            boolean invalidApiKey =
                    exception.message == 'Invalid API key'

            render(
                    status: invalidApiKey
                            ? HttpStatus.UNAUTHORIZED.value()
                            : HttpStatus.BAD_REQUEST.value(),
                    contentType: 'application/json',
                    text: [
                            errorCode: invalidApiKey ? '01' : '30',
                            error    : invalidApiKey
                                    ? exception.message
                                    : 'Invalid payment status'
                    ] as JSON
            )
        }
    }
    private String getPaymentValidationMessage(
            ValidationException exception
    ) {
        def fieldError = exception.errors?.fieldErrors?.first()

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
}
