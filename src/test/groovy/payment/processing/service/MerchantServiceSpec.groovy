package payment.processing.service

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class MerchantServiceSpec extends Specification
        implements ServiceUnitTest<MerchantService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Merchant] as Class<?>[]
    }

    void "createMerchant creates and saves a merchant"() {
        when:
        Merchant merchant = service.createMerchant(
                ' Test Store ',
                'STORE@TEST.COM'
        )

        then:
        merchant.id
        merchant.name == 'Test Store'
        merchant.email == 'store@test.com'
        merchant.apiKey
        merchant.active
        Merchant.count() == 1
    }
}
