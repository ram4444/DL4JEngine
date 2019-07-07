package main.kotlin.config

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonSerializer
import java.io.Serializable

@EnableKafka
@Configuration
class KakfaConfig {

    companion object {
        const val PRODUCER_SrcNodeOpen = "src-node-open"
        const val PRODUCER_SrcNodeClose = "src-node-close"

        const val schemaRegistryUrl = "http://localhost:8081"
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, GenericRecord>  {
        val config: Map<String, Serializable> = mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:29092",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
                "schema.registry.url" to schemaRegistryUrl
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun producerFactory_string(): ProducerFactory<String, String>  {
        val config: Map<String, Serializable> = mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "127.0.0.1:9092",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun producerFactory_json(): ProducerFactory<String, String>  {
        val config: Map<String, Serializable> = mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "127.0.0.1:9092",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java

        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, GenericRecord> {
        val config: Map<String, Serializable>  = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "127.0.0.1:29092",
                ConsumerConfig.GROUP_ID_CONFIG to "foo",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
                "schema.registry.url" to schemaRegistryUrl
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun consumerFactory_string(): ConsumerFactory<String, String> {
        val config: Map<String, Serializable>  = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "127.0.0.1:9092",
                ConsumerConfig.GROUP_ID_CONFIG to "foo",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
                //ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun KafkaTemplate(): KafkaTemplate<String, GenericRecord> {
        return KafkaTemplate<String, GenericRecord>(producerFactory())
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, GenericRecord> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, GenericRecord> = ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory());
        return factory
    }

    @Bean
    fun kafkaListenerContainerFactory_string(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, String> = ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory_string());
        return factory
    }

    // https://www.baeldung.com/spring-kafka
    //https://github.com/TechPrimers/spring-boot-kafka-consumer-example/blob/master/src/main/java/com/techprimers/kafka/springbootkafkaconsumerexample/config/KafkaConfiguration.java



}