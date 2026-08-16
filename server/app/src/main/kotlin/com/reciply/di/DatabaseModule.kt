package com.reciply.di

import com.reciply.db.ReceiptRepository
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val databaseModule =
    module {

        single<HikariDataSource> {
            val config = ConfigFactory.load()

            HikariConfig()
                .apply {
                    jdbcUrl = config.getString("database.url")
                    driverClassName = config.getString("database.driver")
                    username = config.getString("database.user")
                    password = config.getString("database.password")
                    maximumPoolSize = config.getInt("database.maxPoolSize")
                }.let(::HikariDataSource)
        }

        single<Database> {
            Database.connect(get<HikariDataSource>())
        }

        single<ReceiptRepository> {
            ReceiptRepository(get())
        }
    }
