package com.angeldevtech.gol.domain.models

data class ScheduleItem(
    val id: Int,
    val hour: String,
    val name: String,
    val date: String,
    val category: String,
    val embeds: List<Embed>,
    val leagueName: String,
    val leagueImageUrl: String
)

data class Embed(
    val name: String,
    val language: String,
    val url: String
)

data class ScheduleCategories(
    val name: String,
    val items: List<ScheduleItem>
)