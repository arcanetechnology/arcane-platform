package no.arcane.platform.utils.arrow

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.core.separateEither

fun <LEFT, RIGHT> List<Either<LEFT, RIGHT>>.join(): Either<List<LEFT>, List<RIGHT>> {
    val (leftList, rightList) = this.separateEither()
    return if (leftList.isEmpty()) {
        rightList.right()
    } else {
        leftList.left()
    }
}