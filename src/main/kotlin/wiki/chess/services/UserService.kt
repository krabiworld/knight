package wiki.chess.services

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import wiki.chess.bearerAuthorization
import wiki.chess.currentTime
import wiki.chess.db
import wiki.chess.discordApi
import wiki.chess.dtos.UpdateUserDto
import wiki.chess.models.AccessToken
import wiki.chess.models.DiscordError
import wiki.chess.models.DiscordUser
import wiki.chess.models.User

object UserService {
    private const val collectionName = "users"

    /**
     * `getDiscordUserByToken` is a function that takes a string and returns a `DiscordUser` object
     *
     * @param token The token of the user you want to get the information of.
     * @return A DiscordUser object
     */
    suspend fun getDiscordUserByToken(token: String): DiscordUser {
        val res = discordApi.get("users/@me") {
            bearerAuthorization(token)
        }

        if (!res.status.isSuccess()) {
            return DiscordUser(error = res.body<DiscordError>().apply { status = res.status })
        }

        return res.body()
    }

    /**
     * > This function gets a user from the database by their id, and if safety is true, it sets the id and notifications
     * to null
     *
     * @param id The id of the user you want to get
     * @param safety If true, the user's id and notifications will be set to the id and notifications of the user in the
     * database.
     * @return A user object
     */
    suspend fun getUserById(id: String, safety: Boolean = true): User? {
        val user = withContext(Dispatchers.IO) {
            db.collection(collectionName).document(id).get().get()
        }.toObject(User::class.java) ?: return null

        if (safety) {
            user.notifications = listOf()
        }

        return user
    }

    /**
     * > This function gets a user by their token
     *
     * @param token The token of the user you want to get.
     * @return A User object
     */
    suspend fun getUserByToken(token: String): User? {
        val discordUser = getDiscordUserByToken(token)

        return getUserById(discordUser.id, false)
    }

    /**
     * > This function returns a list of users, and it does so by calling the `GeneralService.get` function, which takes a
     * collection name, a limit, a before, a sort, and a function that converts a document to a user
     *
     * @param limit The maximum number of users to return.
     * @param before The id of the last user in the previous page.
     * @param sort The field to sort by.
     * @return A list of users.
     */
    suspend fun getUsers(limit: Int, before: String, sort: String): List<User> {
        return GeneralService.get(collectionName, limit, before, sort) { user ->
            user.toObject(User::class.java).apply {
                email = ""
                notifications = listOf()
            }
        }
    }

    /**
     * > Update a user in the database
     *
     * @param id The id of the user to update
     * @param data The data to be updated.
     */
    fun updateUser(id: String, data: UpdateUserDto) {
        db.collection(collectionName).document(id).update(Json.encodeToJsonElement(data).jsonObject.toMap())
    }

    /**
     * It deletes a user from the database
     *
     * @param id The id of the user you want to delete.
     */
    fun deleteUser(id: String) {
        db.collection(collectionName).document(id).delete()
    }

    /**
     * It gets the user's information from Discord, checks if the user exists in the database, and if not, creates a new
     * user
     *
     * @param accessToken The access token that was received from the OAuth2 flow.
     */
    suspend fun initializeUser(accessToken: AccessToken) {
        val discordUser = getDiscordUserByToken(accessToken.access_token)
        val document = db.collection(collectionName).document(discordUser.id)
        val user = withContext(Dispatchers.IO) { document.get().get() }

        if (!user.exists()) {
            /*val data: Map<String, Any?> = mapOf(
                "id" to discordUser.id,
                "name" to discordUser.username,
                "avatar" to "https://cdn.discordapp.com/avatars/${discordUser.id}/${discordUser.avatar}.png",
                "bio" to "Look at me, I'm new!",
                "references" to listOf<String>(),
                "country" to null,
                "email" to "",
                "federation" to null,
                "rating" to 1000,
                "role" to Role.USER.name,
                "sex" to null,
                "title" to null,
                "notifications" to mapOf<String, Map<String, String>>(),
                "birthday" to 0,
                "registered_at" to currentTime(),
                "badges" to listOf<String>(),
                "fide_id" to 0
            )*/
            val data = User(
                id = discordUser.id,
                name = discordUser.username,
                avatar = "https://cdn.discordapp.com/avatars/${discordUser.id}/${discordUser.avatar}.png",
                bio = "Look at me, I'm new!",
                registered_at = currentTime()
            )
            document.set(data)
        }
    }
}
