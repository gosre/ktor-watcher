package me.claytonw.util

import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat

typealias DateTimeAsLong = @Serializable(DateTimeSerializer::class) DateTime
typealias LocalDateAsText = @Serializable(LocalDateSerializer::class) LocalDate
typealias HttpStatusCodeAsInt = @Serializable(HttpStatusCodeSerializer::class) HttpStatusCode

object DateTimeSerializer : KSerializer<DateTime> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("date_time", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) =
        DateTime(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: DateTime) =
        encoder.encodeLong(value.millis)

}

object LocalDateSerializer : KSerializer<LocalDate> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("local_date", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) =
        LocalDate.parse(decoder.decodeString(), ISODateTimeFormat.date())

    override fun serialize(encoder: Encoder, value: LocalDate) =
        encoder.encodeString(value.toString(ISODateTimeFormat.date()))

}

object HttpStatusCodeSerializer : KSerializer<HttpStatusCode> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("status", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) =
        HttpStatusCode.fromValue(decoder.decodeInt())

    override fun serialize(encoder: Encoder, value: HttpStatusCode) =
        encoder.encodeInt(value.value)

}