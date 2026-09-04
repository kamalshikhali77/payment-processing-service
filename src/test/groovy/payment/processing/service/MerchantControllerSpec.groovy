package payment.processing.service

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class MerchantControllerSpec extends Specification
        implements ControllerUnitTest<MerchantController> {

    void "create returns 201 and merchant data"() {
        given:
        Merchant merchant = new Merchant(
                name: 'Test Store',
                email: 'store@test.com',
                apiKey: 'test-api-key',
                active: true
        )
        merchant.id = 1L

        MerchantService merchantService = Mock(MerchantService)
        controller.merchantService = merchantService

        merchantService.createMerchant(
                'Test Store',
                'store@test.com'
        ) >> merchant

        request.json = [
                name : 'Test Store',
                email: 'store@test.com'
        ]

        when:
        controller.create()

        then:
        response.status == 201
        response.json.id == 1
        response.json.name == 'Test Store'
        response.json.email == 'store@test.com'
        response.json.apiKey == 'test-api-key'
        response.json.active == true
    }
}