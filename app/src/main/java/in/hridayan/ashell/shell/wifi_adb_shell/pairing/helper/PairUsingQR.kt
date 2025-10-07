package `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.helper

import android.graphics.Bitmap
import android.graphics.Color
import io.nayuki.qrcodegen.QrCode
import io.nayuki.qrcodegen.QrCode.Ecc
import androidx.core.graphics.set
import androidx.core.graphics.createBitmap

class PairUsingQR {

    /**
     * Generates a QR code bitmap for wireless debugging pairing
     * @param sessionId The session ID (e.g., "ashell_you")
     * @param pairingCode The pairing code (e.g., 123456)
     * @param size The desired width/height of the QR code bitmap
     */
    fun generateQrBitmap(sessionId: String, pairingCode: Int, size: Int = 512): Bitmap {
        val content = "WIFI:T:ADB;S:$sessionId;P:$pairingCode;;"

        val qr = QrCode.encodeText(content, Ecc.MEDIUM)

        val qrSize = qr.size
        val scale = size / qrSize

        val bitmap = createBitmap(qrSize * scale, qrSize * scale)
        for (y in 0 until qrSize) {
            for (x in 0 until qrSize) {
                val color = if (qr.getModule(x, y)) Color.BLACK else Color.WHITE
                for (dy in 0 until scale) {
                    for (dx in 0 until scale) {
                        bitmap[x * scale + dx, y * scale + dy] = color
                    }
                }
            }
        }

        return bitmap
    }
}
