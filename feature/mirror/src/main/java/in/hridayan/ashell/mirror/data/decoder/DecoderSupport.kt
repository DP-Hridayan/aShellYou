package `in`.hridayan.ashell.mirror.data.decoder

import android.media.MediaCodecList

object DecoderSupport {

    fun isSupported(mimeType: String): Boolean =
        MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.any { info ->
            !info.isEncoder && info.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }
        }
}
