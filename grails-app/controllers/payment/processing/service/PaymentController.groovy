package payment.processing.service

import grails.converters.JSON
import org.springframework.http.HttpStatus
import java.text.SimpleDateFormat

class PaymentController {
    static responseFormats = ['json']

    PaymentService paymentService

    def create(CreatePaymentCommand cmd) {
        String apiKey = request.getHeader('X-API-KEY')


        if (!cmd.validate()) {
            throw new ApiException(
                    ApiError.INVALID_PAYMENT_DATA,
                    cmd.getValidationMessage()
            )
        }


        CreatePaymentAction action = new CreatePaymentAction(paymentService)

        PaymentTransaction payment = action.execute(apiKey, cmd)



        PaymentDTO dto = PaymentDTO.fromPayment(payment)

        render(
                status: HttpStatus.CREATED.value(),
                contentType: 'application/json',
                text: dto.toCreateResponse() as JSON
        )
    }
    def capture() {
        String apiKey = request.getHeader('X-API-KEY')
        String reference = params.reference

        PaymentTransaction payment =
                paymentService.capturePayment(apiKey, reference)

        PaymentDTO dto = PaymentDTO.fromPayment(payment)

        render(
                status: HttpStatus.OK.value(),
                contentType: 'application/json',
                text: dto.toStatusResponse() as JSON
        )
    }

    def refund() {
        String apiKey = request.getHeader('X-API-KEY')
        String reference = params.reference

        PaymentTransaction payment =
                paymentService.refundPayment(apiKey, reference)

        PaymentDTO dto = PaymentDTO.fromPayment(payment)

        render(
                status: HttpStatus.OK.value(),
                contentType: 'application/json',
                text: dto.toStatusResponse() as JSON
        )
    }

    def details() {
        String apiKey = request.getHeader('X-API-KEY')
        String reference = params.reference

        PaymentTransaction payment =
                paymentService.getPayment(apiKey, reference)

        PaymentDTO dto = PaymentDTO.fromPayment(payment)

        render(
                status: HttpStatus.OK.value(),
                contentType: 'application/json',
                text: dto as JSON
        )
    }

    def listAll() {
        String apiKey = request.getHeader('X-API-KEY')

        PaymentStatus status = null

        if (params.status) {
            try {
                status = PaymentStatus.valueOf(
                        params.status.toUpperCase()
                )
            } catch (IllegalArgumentException exception) {
                throw new ApiException(ApiError.INVALID_PAYMENT_STATUS)
            }
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat('yyyy-MM-dd')

        dateFormat.setLenient(false)

        Date fromDate = null
        Date toDate = null

        try {
            if (params.fromDate) {
                String from = params.fromDate.toString()

                if (!(from ==~ /\d{4}-\d{2}-\d{2}/)) {
                    throw new java.text.ParseException('Invalid date format', 0)
                }

                fromDate = dateFormat.parse(from)
            }

            if (params.toDate) {
                String to = params.toDate.toString()

                if (!(to ==~ /\d{4}-\d{2}-\d{2}/)) {
                    throw new java.text.ParseException('Invalid date format', 0)
                }

                Calendar calendar = Calendar.getInstance()
                calendar.time = dateFormat.parse(to)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                toDate = calendar.time
            }
        } catch (java.text.ParseException exception) {
            throw new ApiException(ApiError.INVALID_DATE_FORMAT)
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

        def response = payments.collect { PaymentTransaction payment ->
            PaymentDTO.fromPayment(payment)
        }


        render(
                status: HttpStatus.OK.value(),
                contentType: 'application/json',
                text: response as JSON
        )
    }
}

