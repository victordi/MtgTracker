package com.mtg.tracker.config

import com.impossibl.postgres.jdbc.PGDataSource
import liquibase.integration.spring.SpringLiquibase
import org.jetbrains.exposed.sql.Database
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DatabaseConfig {

    @Bean
    fun dataSource(): DataSource = PGDataSource().apply {
        url = "jdbc:pgsql://localhost:5432/postgres"
        user = "postgres"
        password = "password"
    }

    @Bean
    @ConditionalOnExpression("\${spring.liquibase.enabled:true}")
    fun liquibase(dataSource: DataSource): SpringLiquibase =
        SpringLiquibase().apply {
            changeLog = "classpath:db.changelog-master.yml"
            this.dataSource = dataSource
        }

    @Bean
    fun onStart(dataSource: DataSource) = CommandLineRunner { Database.connect(dataSource) }
}