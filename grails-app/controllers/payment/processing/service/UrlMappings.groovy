package payment.processing.service

class UrlMappings {

    static mappings = {
        post "/api/merchants"(controller: "merchant", action: "create")
        post "/api/payments"(controller: "payment", action: "create")
        post "/api/payments/$reference/capture"(
                controller: "payment",
                action: "capture"
        )
        post "/api/payments/$reference/refund"(
                controller: "payment",
                action: "refund"
        )

        get "/api/payments"(
                controller: "payment",
                action: "listAll"
        )
        get "/api/payments/$reference"(
                controller: "payment",
                action: "details"
        )
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
