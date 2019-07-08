package main.kotlin.kafka

import main.kotlin.config.KakfaConfig
import mu.KotlinLogging
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.io.File
import java.lang.Math.abs
import java.util.*

@Service
class Listener {
    var currentValue = 0F

    private val logger = KotlinLogging.logger {}

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, GenericRecord>


    val schema_open = Schema.Parser().parse(File("src/main/resources/avsc/node-src-open.avsc"))
    val schema_close = Schema.Parser().parse(File("src/main/resources/avsc/node-src-close.avsc"))

    //@KafkaListener(topics = ["src-node-open"], groupId = "src")
    fun listen(message: GenericRecord) {
        currentValue=message.get("value") as Float
        logger.debug { currentValue }

        //TODO: Append the JSON to a file
    }

}
