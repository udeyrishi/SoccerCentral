package com.udeyrishi.soccercentral.api

import org.joda.money.Money
import java.net.URL
import java.util.*

/**
 * Created by Udey Rishi (udeyrishi) on 2017-09-09.
 * Copyright © 2017 Udey Rishi. All rights reserved.
 */
class Year(year: String) {
    private val year: String = (Regex("^\\d{4}$").matchEntire(year) ?: throw IllegalArgumentException("Illegal year string")).groupValues[0]

    override fun toString() = year

    override fun equals(other: Any?) = other is Year && this.year == other.year

    override fun hashCode() = year.hashCode() * 7
}

data class Season(val id: Int,
                  val caption: String,
                  val league: String,
                  val year: Year,
                  val currentMatchday: Int,
                  val numberOfMatchdays: Int,
                  val numberOfTeams: Int,
                  val lastUpdated: Date)

data class Team(val id: Int,
                val name: String,
                val shortName: String?,
                val squadMarketValue: Money?,
                val crestUrl: URL?)

class TeamList(size: Int): ArrayList<Team>(size)