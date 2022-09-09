package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.chess.*
import wiki.chess.dtos.UpdateUserDto
import wiki.chess.models.User
import wiki.chess.services.UserService

fun Route.userRoutes() {
    get {
        val limit = call.getQuery("limit")?.toInt(call)?.isNegative(call) ?: return@get
        val before = call.getQuery("before", false)!!
        val sort = call.getQuery("sort", false)!!

        call.respond(UserService.getUsers(limit, before, sort))
    }

    get("/{id}") {
        val user = call.getUser("id") ?: return@get

        user.email = ""

        call.respond(user)
    }

    get("/@me") {
        val user = call.getUser() ?: return@get

        call.respond(user)
    }

    put("/@me/update") {
        val user = call.getUser() ?: return@put

        val content: UpdateUserDto = try {
            call.receive()
        } catch (e: ContentTransformationException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            return@put
        }

        content.name.hasLength(call, 2, 32) ?: return@put

        val data: Map<String, Any?> = mapOf(
            "name" to content.name,
            "bio" to content.bio,
            "references" to content.references,
            "country" to content.country,
            "email" to content.email,
            "federation" to content.federation,
            "sex" to content.sex,
            "birthday" to content.birthday
        )

        UserService.updateUser(user.id, content)

        call.respond(HttpStatusCode.OK, "Account updated")
    }

    delete("/@me/delete") {
        val user = call.getUser() ?: return@delete

        UserService.deleteUser(user.id)

        call.respond(HttpStatusCode.OK, "Account deleted")
    }
}
