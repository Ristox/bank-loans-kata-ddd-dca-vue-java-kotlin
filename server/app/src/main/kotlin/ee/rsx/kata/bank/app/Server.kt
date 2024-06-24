package ee.rsx.kata.bank.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["ee.rsx.kata.bank"])
open class Server

fun main(vararg args: String) {
  runApplication<Server>(*args)
}
