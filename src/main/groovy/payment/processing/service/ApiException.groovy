package payment.processing.service

class ApiException extends RuntimeException {

    final ApiError error

    ApiException(ApiError error) {
        super(error.message)
        this.error = error
    }
    ApiException(ApiError error, String message) {
        super(message)
        this.error = error
    }
}
