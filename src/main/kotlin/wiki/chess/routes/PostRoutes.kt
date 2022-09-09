package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.chess.*
import wiki.chess.enums.Role
import wiki.chess.services.PostService

fun Route.postRoutes() {
    get {
        val limit = call.getQuery("limit")?.toInt(call)?.isNegative(call) ?: return@get
        val before = call.getQuery("before", false)!!
        val sort = call.getQuery("sort", false)!!

        call.respond(PostService.getPosts(limit, before, sort))
    }

    get("/{id}") {
        val post = call.getPost("id") ?: return@get

        call.respond(post)
    }

    put("/create") {
        val user = call.getUser() ?: return@put
        val content = call.receiveText()

        content.hasLength(call, min = 8) ?: return@put

        val data: Map<String, Any> = mapOf(
            "content" to content,
            "author" to user.id,
            "created" to currentTime(),
            "edited" to 0,
            "votes" to 0
        )

        PostService.createPost(data)

        call.respond(HttpStatusCode.OK)
    }

    put("/{id}/incrementVotes") {
        call.getUser() ?: return@put
        val post = call.getPost("id") ?: return@put

        PostService.incrementVotes(post)

        call.respond(HttpStatusCode.OK)
    }

    put("/{id}/decrementVotes") {
        call.getUser() ?: return@put
        val post = call.getPost("id") ?: return@put

        PostService.decrementVotes(post)

        call.respond(HttpStatusCode.OK)
    }

    put("/{id}/edit") {
        val user = call.getUser() ?: return@put
        val post = call.getPost("id") ?: return@put
        val content = call.receiveText()

        content.hasLength(call, min = 8) ?: return@put

        if (user.id != post.author) {
            call.respond(HttpStatusCode.Forbidden)
            return@put
        }

        val editedPost: Map<String, Any> = mapOf(
            "content" to content,
            "edited" to currentTime()
        )

        PostService.editPost(post, editedPost)

        call.respond(HttpStatusCode.OK)
    }

    delete("/{id}/delete") {
        val user = call.getUser() ?: return@delete
        val post = call.getPost("id") ?: return@delete

        if (user.id != post.author && user.role != Role.MOD && user.role != Role.ADMIN) {
            call.respond(HttpStatusCode.Forbidden)
            return@delete
        }

        PostService.deletePost(post)

        call.respond(HttpStatusCode.OK)
    }
}
