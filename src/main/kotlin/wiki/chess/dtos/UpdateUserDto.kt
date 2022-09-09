package wiki.chess.dtos

import wiki.chess.enums.Country
import wiki.chess.enums.Federation
import wiki.chess.enums.Sex

data class UpdateUserDto(
    val name: String = "",
    val bio: String = "",
    val references: List<String> = listOf(),
    val country: Country? = null,
    val email: String = "",
    val federation: Federation? = null,
    val sex: Sex? = null,
    val birthday: Long = 0
)
