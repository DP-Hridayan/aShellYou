package `in`.hridayan.ashell.core.common.serializers

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TextFieldValueSerializer : KSerializer<TextFieldValue> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TextFieldValue", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TextFieldValue) {
        encoder.encodeString(value.text)
    }

    override fun deserialize(decoder: Decoder): TextFieldValue {
        return TextFieldValue(decoder.decodeString())
    }
}
