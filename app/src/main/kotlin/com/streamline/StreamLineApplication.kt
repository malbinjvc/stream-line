package com.streamline

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class StreamLineApplication

fun main(args: Array<String>) {
    runApplication<StreamLineApplication>(*args)
}
