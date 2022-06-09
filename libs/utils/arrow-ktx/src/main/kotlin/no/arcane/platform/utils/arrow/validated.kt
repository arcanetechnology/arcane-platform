package no.arcane.platform.utils.arrow

import arrow.core.NonEmptyList
import arrow.core.Validated
import arrow.core.ValidatedNel
import arrow.core.invalid
import arrow.core.nonEmptyListOf
import arrow.core.valid

interface ValidatedEffect<INVALID, VALID> {
    suspend fun toValidatedNel(): ValidatedNel<INVALID, VALID>
}

interface ValidatedEffectScope<INVALID, VALID> {

    suspend fun shift(invalid: INVALID)

    suspend fun Validated<INVALID, VALID>.bind(): Validated<INVALID, VALID> {
        when (this) {
            is Validated.Valid -> value
            is Validated.Invalid -> shift(value)
        }
        return this
    }
}

@Suppress("ClassName")
object validated {
    suspend inline operator fun <INVALID, VALID> invoke(
        crossinline f: suspend ValidatedEffectScope<INVALID, VALID>.() -> VALID
    ): ValidatedNel<INVALID, VALID> = validatedEffect(f).toValidatedNel()
}

inline fun <INVALID, VALID> validatedEffect(
    crossinline f: suspend ValidatedEffectScope<INVALID, VALID>.() -> VALID
): ValidatedEffect<INVALID, VALID> = object : ValidatedEffect<INVALID, VALID> {

    override suspend fun toValidatedNel(): ValidatedNel<INVALID, VALID> {
        var invalidList: NonEmptyList<INVALID>? = null
        val effectScope = object : ValidatedEffectScope<INVALID, VALID> {
            override suspend fun shift(invalid: INVALID) {
                invalidList = invalidList?.let { it + invalid } ?: nonEmptyListOf(invalid)
            }
        }
        val valid = f(effectScope)
        return invalidList?.invalid() ?: valid.valid()
    }
}
