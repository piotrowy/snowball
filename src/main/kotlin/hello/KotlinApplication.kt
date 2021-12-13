package hello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@SpringBootApplication
class KotlinApplication {

    @Bean
    fun routes() = router {
        GET {
            ServerResponse.ok().body(Mono.just("Let the battle begin!"))
        }

        POST("/**", accept(APPLICATION_JSON)) { request ->
            request.bodyToMono(ArenaUpdate::class.java).flatMap { arenaUpdate ->
                val me = arenaUpdate._links.self.href
                val x = arenaUpdate.arena.state[me]!!.x
                val y = arenaUpdate.arena.state[me]!!.x
                val players = arenaUpdate.arena.state.values
                if (
                    (when (arenaUpdate.arena.state[me]!!.direction) {
                        "N" -> players.filter { it.x == x && it.y - y <= 3 && it.y - y > 0 }
                        "E" -> players.filter { it.y == y && it.x - x <= 3 && it.x - x > 0 }
                        "S" -> players.filter { it.x == x && y - it.y <= 3 && y - it.y > 0 }
                        "W" -> players.filter { it.y == y && x - it.x <= 3 && x - it.x > 0 }
                        else -> emptyList()
                    }).isNotEmpty()
                ) {
                    ServerResponse.ok().body(Mono.just("T"))
                } else {
                    ServerResponse.ok().body(Mono.just(listOf("R", "L", "F").random()))
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args)
}

data class ArenaUpdate(val _links: Links, val arena: Arena)
data class PlayerState(val x: Int, val y: Int, val direction: String, val score: Int, val wasHit: Boolean)
data class Links(val self: Self)
data class Self(val href: String)
data class Arena(val dims: List<Int>, val state: Map<String, PlayerState>)
