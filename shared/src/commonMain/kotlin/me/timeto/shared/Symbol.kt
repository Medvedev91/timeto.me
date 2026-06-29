package me.timeto.shared

sealed class Symbol(
    val raw: String,
) {

    data class Letter(
        val letter: String,
    ) : Symbol("letter--$letter")

    data class Icon(
        val iconEnum: IconEnum,
    ) : Symbol("icon--${iconEnum.code}") {

        companion object {

            val map: Map<String, Icon> =
                IconEnum.entries.associate { it.code to it.toIcon() }
        }

        enum class IconEnum(val code: String) {

            book("book"),
            case("case"),
            timer("timer"),
            exercise("exercise"),
            piano("piano"),
            music_note("music_note"),
            rocket("rocket"),
            bus("bus"),
            bulb("bulb"),
            bolt("bolt"),
            option("option"),
            graduationcap("graduationcap"),
            megaphone("megaphone"),
            instruments("instruments"),
            meditation("meditation"),
            flask("flask"),
            compass("compass"),
            gamecontroller("gamecontroller"),
            soccerball("soccerball"),
            hiking("hiking"),
            inbox("inbox"),
            sun("sun"),
            moon("moon"),
            moon_stars("moon_stars"),
            film("film"),
            coffee("coffee"),
            tennis("tennis"),
            surfing("surfing"),
            skiing("skiing"),
            fork_knife("fork_knife"),
            hockey("hockey"),
            question("question");

            fun toIcon(): Icon =
                Icon(this)
        }
    }

    data class Emoji(
        val emoji: String,
    ) : Symbol("emoji--$emoji")

    ///

    companion object {

        fun fromRawOrNull(raw: String): Symbol? {
            val parts: List<String> =
                raw.split("--", limit = 2)
            if (parts.size != 2)
                return null
            return when (parts[0]) {
                "letter" -> Letter(parts[1])
                "icon" -> Icon.map[parts[1]]
                "emoji" -> Emoji(parts[1])
                else -> null
            }
        }
    }
}
