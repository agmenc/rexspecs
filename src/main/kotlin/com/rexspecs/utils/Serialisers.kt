package com.rexspecs.utils
import com.rexspecs.Either
import com.rexspecs.InvalidStructure
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.*


class EitherSerializer<L, R>(
    private val leftSerializer: KSerializer<L>,
    private val rightSerializer: KSerializer<R>,
) : KSerializer<Either<L, R>> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Either") {
        element("Left", leftSerializer.descriptor, isOptional = true)
        element("Right", rightSerializer.descriptor, isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: Either<L, R>) {
        encoder.encodeStructure(descriptor) {
            when (value) {
                is Either.Left -> encodeSerializableElement(descriptor, 0, leftSerializer, value.left)
                is Either.Right -> encodeSerializableElement(descriptor, 1, rightSerializer, value.right)
            }
        }
    }

    override fun deserialize(decoder: Decoder): Either<L, R> {
        val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())

        return when {
            jsonElement is JsonPrimitive -> throw InvalidStructure("Nup. JsonElement: $jsonElement")
            jsonElement.jsonObject.containsKey("Left") -> {
                val reallyAString: String = jsonElement.jsonObject["Left"]?.jsonPrimitive?.content ?: "wurgle"
                return Either.Left(reallyAString) as Either<L, R>

                // TODO - Work out WhyTF the leftSerializer (a String serializer) can't see the String
                // Either.Left(leftSerializer.deserialize(decoder))
            }
            jsonElement.jsonObject.containsKey("Right") -> Either.Right(rightSerializer.deserialize(decoder))
            else -> throw IllegalStateException("Either must contain either 'Left' or 'Right'. JsonElement: $jsonElement")
        }
    }
}
