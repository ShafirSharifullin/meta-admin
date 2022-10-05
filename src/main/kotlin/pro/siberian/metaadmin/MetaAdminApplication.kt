package pro.siberian.metaadmin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MetaAdminApplication

fun main(args: Array<String>) {
    runApplication<MetaAdminApplication>(*args)
}
