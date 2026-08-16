package com.reciply.di

import com.reciply.db.ReceiptRepository
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val databaseModule = module {
    single {
        val config = ConfigFactory.load()
        HikariConfig().apply {
            jdbcUrl = config.getString("database.url")
            driverClassName = config.getString("database.driver")
            username = config.getString("database.user")
            password = config.getString("database.password")
            maximumPoolSize = config.getInt("database.maxPoolSize")
        }.let { HikariDataSource(it) }
    }

    single {
        Database.connect(get<HikariDataSource>())
        get<HikariDataSource>()
    }

    single { ReceiptRepository() }
}
