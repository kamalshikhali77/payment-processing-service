package payment.processing.service

import grails.gorm.transactions.Transactional

@Transactional
class MerchantService {
    Merchant createMerchant(String name, String email) {
        Merchant merchant = new Merchant(
                name: name?.trim(),

                email: email?.trim()?.toLowerCase(),
                apiKey: UUID.randomUUID().toString()
        )

        merchant.save(failOnError: true)
        return merchant
    }

}
