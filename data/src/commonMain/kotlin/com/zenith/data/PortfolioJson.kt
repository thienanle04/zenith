@file:OptIn(ExperimentalSerializationApi::class)

package com.zenith.data

import com.zenith.domain.money.Exact
import com.zenith.domain.money.toJson
import com.zenith.domain.portfolio.AccountKind
import com.zenith.domain.portfolio.AccountRecord
import com.zenith.domain.portfolio.AdjustmentRecord
import com.zenith.domain.portfolio.HoldingRecord
import com.zenith.domain.portfolio.InstrumentRecord
import com.zenith.domain.portfolio.MovementRecord
import com.zenith.domain.portfolio.Portfolio
import com.zenith.domain.portfolio.SeparatorPair
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

private const val FIELD_3 = 3
private const val FIELD_4 = 4
private const val FIELD_5 = 5
private const val FIELD_6 = 6
private const val FIELD_7 = 7
private const val FIELD_8 = 8
private const val FIELD_9 = 9

object PortfolioJson {
    private val json =
        Json {
            encodeDefaults = true
            explicitNulls = true
        }

    fun encode(portfolio: Portfolio): String = json.encodeToString(PortfolioSerializer, portfolio)

    fun decode(raw: String): Portfolio = json.decodeFromString(PortfolioSerializer, raw)
}

private object ExactSerializer : KSerializer<Exact> {
    override val descriptor = PrimitiveSerialDescriptor("Exact", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Exact,
    ) = encoder.encodeString(value.toJson())

    override fun deserialize(decoder: Decoder): Exact = Exact.fromJson(decoder.decodeString())
}

private object AccountKindSerializer : KSerializer<AccountKind> {
    override val descriptor = PrimitiveSerialDescriptor("AccountKind", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: AccountKind,
    ) = encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): AccountKind = AccountKind.valueOf(decoder.decodeString())
}

private object SeparatorPairSerializer : KSerializer<SeparatorPair> {
    override val descriptor = PrimitiveSerialDescriptor("SeparatorPair", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: SeparatorPair,
    ) = encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): SeparatorPair = SeparatorPair.valueOf(decoder.decodeString())
}

private object AccountRecordSerializer : KSerializer<AccountRecord> {
    override val descriptor =
        buildClassSerialDescriptor("AccountRecord") {
            element("id", String.serializer().descriptor)
            element("name", String.serializer().descriptor)
            element("kind", AccountKindSerializer.descriptor)
            element("currency", String.serializer().descriptor)
            element("createdOrder", Int.serializer().descriptor)
            element("setAside", Boolean.serializer().descriptor)
        }

    override fun serialize(
        encoder: Encoder,
        value: AccountRecord,
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeStringElement(descriptor, 0, value.id)
        composite.encodeStringElement(descriptor, 1, value.name)
        composite.encodeSerializableElement(descriptor, 2, AccountKindSerializer, value.kind)
        composite.encodeStringElement(descriptor, FIELD_3, value.currency)
        composite.encodeIntElement(descriptor, FIELD_4, value.createdOrder)
        composite.encodeBooleanElement(descriptor, FIELD_5, value.setAside)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): AccountRecord =
        decoder.read(descriptor, { index, input, fields ->
            when (index) {
                0 -> fields.string(input, descriptor, 0, "id")
                1 -> fields.string(input, descriptor, 1, "name")
                2 -> fields.serial(input, descriptor, 2, "kind", AccountKindSerializer)
                FIELD_3 -> fields.string(input, descriptor, FIELD_3, "currency")
                FIELD_4 -> fields.int(input, descriptor, FIELD_4, "createdOrder")
                FIELD_5 -> fields.boolean(input, descriptor, FIELD_5, "setAside")
                else -> unexpected(index)
            }
        }) { fields ->
            AccountRecord(
                id = fields.require("id"),
                name = fields.require("name"),
                kind = fields.require("kind"),
                currency = fields.require("currency"),
                createdOrder = fields.require("createdOrder"),
                setAside = fields.require("setAside"),
            )
        }
}

private object MovementRecordSerializer : KSerializer<MovementRecord> {
    override val descriptor =
        buildClassSerialDescriptor("MovementRecord") {
            element("id", String.serializer().descriptor)
            element("accountId", String.serializer().descriptor)
            element("amountMinor", Long.serializer().descriptor)
            element("vndPerUnit", ExactSerializer.descriptor)
        }

    override fun serialize(
        encoder: Encoder,
        value: MovementRecord,
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeStringElement(descriptor, 0, value.id)
        composite.encodeStringElement(descriptor, 1, value.accountId)
        composite.encodeLongElement(descriptor, 2, value.amountMinor)
        composite.encodeSerializableElement(descriptor, FIELD_3, ExactSerializer, value.vndPerUnit)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): MovementRecord =
        decoder.read(descriptor, { index, input, fields ->
            when (index) {
                0 -> fields.string(input, descriptor, 0, "id")
                1 -> fields.string(input, descriptor, 1, "accountId")
                2 -> fields.long(input, descriptor, 2, "amountMinor")
                FIELD_3 -> fields.serial(input, descriptor, FIELD_3, "vndPerUnit", ExactSerializer)
                else -> unexpected(index)
            }
        }) { fields ->
            MovementRecord(
                id = fields.require("id"),
                accountId = fields.require("accountId"),
                amountMinor = fields.require("amountMinor"),
                vndPerUnit = fields.require("vndPerUnit"),
            )
        }
}

private object InstrumentRecordSerializer : KSerializer<InstrumentRecord> {
    override val descriptor =
        buildClassSerialDescriptor("InstrumentRecord") {
            element("id", String.serializer().descriptor)
            element("name", String.serializer().descriptor)
            element("unit", String.serializer().descriptor)
            element("priceCurrency", String.serializer().descriptor)
            element("price", ExactSerializer.descriptor)
        }

    override fun serialize(
        encoder: Encoder,
        value: InstrumentRecord,
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeStringElement(descriptor, 0, value.id)
        composite.encodeStringElement(descriptor, 1, value.name)
        composite.encodeStringElement(descriptor, 2, value.unit)
        composite.encodeStringElement(descriptor, FIELD_3, value.priceCurrency)
        composite.encodeSerializableElement(descriptor, FIELD_4, ExactSerializer, value.price)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): InstrumentRecord =
        decoder.read(descriptor, { index, input, fields ->
            when (index) {
                0 -> fields.string(input, descriptor, 0, "id")
                1 -> fields.string(input, descriptor, 1, "name")
                2 -> fields.string(input, descriptor, 2, "unit")
                FIELD_3 -> fields.string(input, descriptor, FIELD_3, "priceCurrency")
                FIELD_4 -> fields.serial(input, descriptor, FIELD_4, "price", ExactSerializer)
                else -> unexpected(index)
            }
        }) { fields ->
            InstrumentRecord(
                id = fields.require("id"),
                name = fields.require("name"),
                unit = fields.require("unit"),
                priceCurrency = fields.require("priceCurrency"),
                price = fields.require("price"),
            )
        }
}

private object HoldingRecordSerializer : KSerializer<HoldingRecord> {
    override val descriptor =
        buildClassSerialDescriptor("HoldingRecord") {
            element("id", String.serializer().descriptor)
            element("instrumentId", String.serializer().descriptor)
            element("stockAccountId", String.serializer().nullable.descriptor)
            element("createdOrder", Int.serializer().descriptor)
        }

    override fun serialize(
        encoder: Encoder,
        value: HoldingRecord,
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeStringElement(descriptor, 0, value.id)
        composite.encodeStringElement(descriptor, 1, value.instrumentId)
        composite.encodeNullableSerializableElement(descriptor, 2, String.serializer().nullable, value.stockAccountId)
        composite.encodeIntElement(descriptor, FIELD_3, value.createdOrder)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): HoldingRecord =
        decoder.read(descriptor, { index, input, fields ->
            when (index) {
                0 -> fields.string(input, descriptor, 0, "id")
                1 -> fields.string(input, descriptor, 1, "instrumentId")
                2 -> fields.nullableString(input, descriptor, 2, "stockAccountId")
                FIELD_3 -> fields.int(input, descriptor, FIELD_3, "createdOrder")
                else -> unexpected(index)
            }
        }) { fields ->
            HoldingRecord(
                id = fields.require("id"),
                instrumentId = fields.require("instrumentId"),
                createdOrder = fields.require("createdOrder"),
                stockAccountId = fields.require("stockAccountId"),
            )
        }
}

private object AdjustmentRecordSerializer : KSerializer<AdjustmentRecord> {
    override val descriptor =
        buildClassSerialDescriptor("AdjustmentRecord") {
            element("id", String.serializer().descriptor)
            element("holdingId", String.serializer().descriptor)
            element("quantity", ExactSerializer.descriptor)
        }

    override fun serialize(
        encoder: Encoder,
        value: AdjustmentRecord,
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeStringElement(descriptor, 0, value.id)
        composite.encodeStringElement(descriptor, 1, value.holdingId)
        composite.encodeSerializableElement(descriptor, 2, ExactSerializer, value.quantity)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): AdjustmentRecord =
        decoder.read(descriptor, { index, input, fields ->
            when (index) {
                0 -> fields.string(input, descriptor, 0, "id")
                1 -> fields.string(input, descriptor, 1, "holdingId")
                2 -> fields.serial(input, descriptor, 2, "quantity", ExactSerializer)
                else -> unexpected(index)
            }
        }) { fields ->
            AdjustmentRecord(
                id = fields.require("id"),
                holdingId = fields.require("holdingId"),
                quantity = fields.require("quantity"),
            )
        }
}

private object PortfolioSerializer : KSerializer<Portfolio> {
    private val accounts = ListSerializer(AccountRecordSerializer)
    private val movements = ListSerializer(MovementRecordSerializer)
    private val instruments = ListSerializer(InstrumentRecordSerializer)
    private val holdings = ListSerializer(HoldingRecordSerializer)
    private val adjustments = ListSerializer(AdjustmentRecordSerializer)
    private val rates = MapSerializer(String.serializer(), ExactSerializer)
    private val sectionOrder = ListSerializer(String.serializer())
    private val rowOrder = MapSerializer(String.serializer(), ListSerializer(String.serializer()))

    override val descriptor =
        buildClassSerialDescriptor("Portfolio") {
            element("accounts", accounts.descriptor)
            element("movements", movements.descriptor)
            element("instruments", instruments.descriptor)
            element("holdings", holdings.descriptor)
            element("adjustments", adjustments.descriptor)
            element("rates", rates.descriptor)
            element("sectionOrder", sectionOrder.descriptor)
            element("rowOrder", rowOrder.descriptor)
            element("separators", SeparatorPairSerializer.descriptor)
            element("nextId", Int.serializer().descriptor)
        }

    override fun serialize(
        encoder: Encoder,
        value: Portfolio,
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(descriptor, 0, accounts, value.accounts)
        composite.encodeSerializableElement(descriptor, 1, movements, value.movements)
        composite.encodeSerializableElement(descriptor, 2, instruments, value.instruments)
        composite.encodeSerializableElement(descriptor, FIELD_3, holdings, value.holdings)
        composite.encodeSerializableElement(descriptor, FIELD_4, adjustments, value.adjustments)
        composite.encodeSerializableElement(descriptor, FIELD_5, rates, value.rates)
        composite.encodeSerializableElement(descriptor, FIELD_6, sectionOrder, value.sectionOrder)
        composite.encodeSerializableElement(descriptor, FIELD_7, rowOrder, value.rowOrder)
        composite.encodeSerializableElement(descriptor, FIELD_8, SeparatorPairSerializer, value.separators)
        composite.encodeIntElement(descriptor, FIELD_9, value.nextId)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Portfolio =
        decoder.read(descriptor, { index, input, fields ->
            when (index) {
                0 -> fields.serial(input, descriptor, 0, "accounts", accounts)
                1 -> fields.serial(input, descriptor, 1, "movements", movements)
                2 -> fields.serial(input, descriptor, 2, "instruments", instruments)
                FIELD_3 -> fields.serial(input, descriptor, FIELD_3, "holdings", holdings)
                FIELD_4 -> fields.serial(input, descriptor, FIELD_4, "adjustments", adjustments)
                FIELD_5 -> fields.serial(input, descriptor, FIELD_5, "rates", rates)
                FIELD_6 -> fields.serial(input, descriptor, FIELD_6, "sectionOrder", sectionOrder)
                FIELD_7 -> fields.serial(input, descriptor, FIELD_7, "rowOrder", rowOrder)
                FIELD_8 -> fields.serial(input, descriptor, FIELD_8, "separators", SeparatorPairSerializer)
                FIELD_9 -> fields.int(input, descriptor, FIELD_9, "nextId")
                else -> unexpected(index)
            }
        }) { fields ->
            Portfolio(
                accounts = fields.require("accounts"),
                movements = fields.require("movements"),
                instruments = fields.require("instruments"),
                holdings = fields.require("holdings"),
                adjustments = fields.require("adjustments"),
                rates = fields.require("rates"),
                sectionOrder = fields.require("sectionOrder"),
                rowOrder = fields.require("rowOrder"),
                separators = fields.require("separators"),
                nextId = fields.require("nextId"),
            )
        }
}

private class DocumentFields {
    private val values = mutableMapOf<String, Any?>()
    private val seen = mutableSetOf<String>()

    fun put(
        name: String,
        value: Any?,
    ) {
        seen += name
        values[name] = value
    }

    fun <T> require(name: String): T {
        if (name !in seen) throw SerializationException("Field '$name' is required")
        @Suppress("UNCHECKED_CAST")
        return values.getValue(name) as T
    }

    fun string(
        input: CompositeDecoder,
        descriptor: SerialDescriptor,
        index: Int,
        name: String,
    ) {
        put(name, input.decodeStringElement(descriptor, index))
    }

    fun nullableString(
        input: CompositeDecoder,
        descriptor: SerialDescriptor,
        index: Int,
        name: String,
    ) {
        put(name, input.decodeNullableSerializableElement(descriptor, index, String.serializer().nullable))
    }

    fun int(
        input: CompositeDecoder,
        descriptor: SerialDescriptor,
        index: Int,
        name: String,
    ) {
        put(name, input.decodeIntElement(descriptor, index))
    }

    fun long(
        input: CompositeDecoder,
        descriptor: SerialDescriptor,
        index: Int,
        name: String,
    ) {
        put(name, input.decodeLongElement(descriptor, index))
    }

    fun boolean(
        input: CompositeDecoder,
        descriptor: SerialDescriptor,
        index: Int,
        name: String,
    ) {
        put(name, input.decodeBooleanElement(descriptor, index))
    }

    fun <T> serial(
        input: CompositeDecoder,
        descriptor: SerialDescriptor,
        index: Int,
        name: String,
        serializer: DeserializationStrategy<T>,
    ) {
        put(name, input.decodeSerializableElement(descriptor, index, serializer))
    }
}

private fun unexpected(index: Int): Nothing = throw SerializationException("Unknown field index $index")

private inline fun <T> Decoder.read(
    descriptor: SerialDescriptor,
    readField: (index: Int, input: CompositeDecoder, fields: DocumentFields) -> Unit,
    build: (DocumentFields) -> T,
): T {
    val fields = DocumentFields()
    val input = beginStructure(descriptor)
    while (true) {
        val index = input.decodeElementIndex(descriptor)
        if (index == CompositeDecoder.DECODE_DONE) break
        readField(index, input, fields)
    }
    input.endStructure(descriptor)
    return build(fields)
}
