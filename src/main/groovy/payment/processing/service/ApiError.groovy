package payment.processing.service

enum ApiError {

    INVALID_API_KEY('01', 'Invalid API key', 401),

    PAYMENT_NOT_FOUND('20', 'Payment not found', 404),

    CAPTURE_NOT_ALLOWED(
            '11',
            'Only PENDING payments can be captured',
            409
    ),

    REFUND_NOT_ALLOWED(
            '10',
            'Only SUCCESS payments can be refunded',
            409
    ),

    INVALID_PAYMENT_DATA('40', 'Invalid payment data', 400),

    INVALID_PAYMENT_STATUS('30', 'Invalid payment status', 400),

    INVALID_DATE_FORMAT(
            '31',
            'Date must use yyyy-MM-dd format',
            400
    )

    final String code
    final String message
    final int httpStatus

    ApiError(String code, String message, int httpStatus) {
        this.code = code
        this.message = message
        this.httpStatus = httpStatus
    }
}
