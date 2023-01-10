package com.mtg.tracker.config

import com.impossibl.postgres.jdbc.PGDataSource
import liquibase.integration.spring.SpringLiquibase
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DatabaseConfig {

    @Value("\${mtg.db.url}")
    lateinit var dbUrl: String

    @Value("\${mtg.db.user}")
    lateinit var dbUser: String

    @Value("\${mtg.db.password}")
    lateinit var dbPassword: String

    @Value("\${mtg.db.changeLog}")
    lateinit var dbChangeLog: String

    @Bean
    fun dataSource(): DataSource = PGDataSource().apply {
        url = dbUrl
        user = dbUser
        password = dbPassword
    }

    @Bean
    @ConditionalOnExpression("\${spring.liquibase.enabled:true}")
    fun liquibase(dataSource: DataSource): SpringLiquibase = SpringLiquibase().apply {
        changeLog = dbChangeLog
        this.dataSource = dataSource
    }

    @Bean
    fun onStart(dataSource: DataSource) = CommandLineRunner { Database.connect(dataSource) }
}