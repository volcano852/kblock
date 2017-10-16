package com.fabien.kblock

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class KBlockApp

fun main(args: Array<String>) {
    SpringApplication.run(KBlockApp::class.java, *args)
}