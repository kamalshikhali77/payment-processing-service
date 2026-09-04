package payment.processing.service

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class PaymentServiceSpec extends Specification
        implements ServiceUnitTest<PaymentService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Merchant, PaymentTransaction] as Class<?>[]
    }

    void "createPayment creates a pending payment"() {
        given:
        Merchant merchant = new Merchant(
                name: 'Test Merchant',
                email: 'merchant@test.com',
                apiKey: 'valid-api-key',
                active: true
        ).save(failOnError: true)

        when:
        PaymentTransaction payment = service.createPayment(
                'valid-api-key',
                'TEST-001',
                100.00G,
                'USD',
                'Test payment'
        )

        then:
        payment.id
        payment.reference == 'TEST-001'
        payment.amount == 100.00G
        payment.currency == 'USD'
        payment.description == 'Test payment'
        payment.status == PaymentStatus.PENDING
        payment.merchant.id == merchant.id
        PaymentTransaction.count() == 1
    }
    void "capturePayment changes status from PENDING to SUCCESS"() {
        given:
        Merchant merchant = new Merchant(
                name: 'Test Merchant',
                email: 'merchant@test.com',
                apiKey: 'valid-api-key',
                active: true
        ).save(failOnError: true)

        PaymentTransaction payment = new PaymentTransaction(
                reference: 'CAPTURE-001',
                amount: 100.00G,
                currency: 'USD',
                description: 'Capture test',
                status: PaymentStatus.PENDING,
                merchant: merchant
        ).save(failOnError: true)

        when:
        PaymentTransaction capturedPayment =
                service.capturePayment(
                        'valid-api-key',
                        'CAPTURE-001'
                )

        then:
        capturedPayment.id == payment.id
        capturedPayment.status == PaymentStatus.SUCCESS
    }
    void "refundPayment changes status from SUCCESS to REFUNDED"() {
        given:
        Merchant merchant = new Merchant(
                name: 'Test Merchant',
                email: 'merchant@test.com',
                apiKey: 'valid-api-key',
                active: true
        ).save(failOnError: true)

        PaymentTransaction payment = new PaymentTransaction(
                reference: 'REFUND-001',
                amount: 100.00G,
                currency: 'USD',
                description: 'Refund test',
                status: PaymentStatus.SUCCESS,
                merchant: merchant
        ).save(failOnError: true)

        when:
        PaymentTransaction refundedPayment =
                service.refundPayment(
                        'valid-api-key',
                        'REFUND-001'
                )

        then:
        refundedPayment.id == payment.id
        refundedPayment.status == PaymentStatus.REFUNDED
    }
    void "capturePayment rejects a payment that is not PENDING"() {
        given:
        Merchant merchant = new Merchant(
                name: 'Test Merchant',
                email: 'merchant@test.com',
                apiKey: 'valid-api-key',
                active: true
        ).save(failOnError: true)

        new PaymentTransaction(
                reference: 'CAPTURE-ERROR-001',
                amount: 100.00G,
                currency: 'USD',
                status: PaymentStatus.SUCCESS,
                merchant: merchant
        ).save(failOnError: true)

        when:
        service.capturePayment(
                'valid-api-key',
                'CAPTURE-ERROR-001'
        )

        then:
        IllegalStateException exception =
                thrown(IllegalStateException)

        exception.message ==
                'Only PENDING payments can be captured'
    }
}
