package com.example.gamehub.data.model

import kotlinx.serialization.Serializable

/**
 * Jedna odigrana runda Una. `scores[i]` je broj bodova koje je igrač na indeksu `i`
 * (isti redoslijed kao UnoViewModel.players) dobio u toj rundi.
 */
@Serializable
data class UnoRound(
    val scores: List<Int>
)