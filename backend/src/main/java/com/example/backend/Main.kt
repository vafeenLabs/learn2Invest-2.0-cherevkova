package com.example.backend

import com.example.backenddto.api.BackendDtoAPI
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(CORS) { anyHost() }

        routing {
            // Эндпоинт: /assets?limit=2000
            get("/assets") {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 2000
                call.respond(BackendDtoAPI.assets(limit))
            }

            // Эндпоинт: /assets/{id}
            get("/assets/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(BackendDtoAPI.assetsId(id))
            }
            get("/assets/{id}/history") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val interval = call.request.queryParameters["interval"] ?: "d1"
                val start = call.request.queryParameters["start"]?.toLongOrNull()
                    ?: (System.currentTimeMillis() - 86400000 * 7) // 7 дней назад по умолчанию
                val end = call.request.queryParameters["end"]?.toLongOrNull()
                    ?: System.currentTimeMillis()

                if (interval != "d1") {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Only daily interval (d1) is supported")
                    )
                    return@get
                }

                call.respond(BackendDtoAPI.assetsIdHistory(id, start, end))
            }
        }
    }.start(wait = true)
}
