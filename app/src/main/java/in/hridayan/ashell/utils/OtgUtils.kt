package `in`.hridayan.ashell.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.cgutman.adblib.AdbBase64
import com.cgutman.adblib.AdbConnection
import `in`.hridayan.ashell.activities.MainActivity
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.ui.ToastUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import androidx.core.content.edit

class OtgUtils {
    object MessageOtg {
        const val DEVICE_NOT_FOUND: Int = 0
        const val CONNECTING: Int = 1
        const val DEVICE_FOUND: Int = 2
        const val FLASHING: Int = 3
        const val INSTALLING_PROGRESS: Int = 4
        const val PUSH_PART: Int = 5
        const val PM_INST_PART: Int = 6
        const val USB_PERMISSION: String = "hridayan.usb.permission"
    }

    object ByteUtils {
        fun concat(vararg arrays: ByteArray): ByteArray {
            // Determine the length of the result array
            var totalLength = 0
            for (i in arrays.indices) {
                totalLength += arrays[i].size
            }

            // create the result array
            val result = ByteArray(totalLength)

            // copy the source arrays into the result array
            var currentIndex = 0
            for (i in arrays.indices) {
                System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].size)
                currentIndex += arrays[i].size
            }

            return result
        }

        fun intToByteArray(value: Int): ByteArray {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
        }
    }

    @Suppress("DEPRECATION")
    class UsbReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val device =
                intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    ?: return

            val manufacturer = device.manufacturerName
            val product = device.productName

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                ToastUtils.showToast(
                    context, "USB Device Attached: $manufacturer $product",
                    ToastUtils.LENGTH_SHORT
                )
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                ToastUtils.showToast(
                    context,
                    "USB Device Detached: $manufacturer $product",
                    ToastUtils.LENGTH_SHORT
                )
                sendIntentUponDetached(context)
            }
        }

        private fun sendIntentUponDetached(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.setAction("in.hridayan.ashell.ACTION_USB_DETACHED")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    class Push(
        private val adbConnection: AdbConnection,
        private val local: File,
        private val remotePath: String
    ) {
        @Throws(InterruptedException::class, IOException::class)
        fun execute(handler: Handler) {
            val stream = adbConnection.open("sync:")

            val sendId = "SEND"

            val mode = ",33206"

            val length = (remotePath + mode).length

            stream.write(ByteUtils.concat(sendId.toByteArray(), ByteUtils.intToByteArray(length)))

            stream.write(remotePath.toByteArray())

            stream.write(mode.toByteArray())

            val buff = ByteArray(adbConnection.maxData)
            val `is`: InputStream = FileInputStream(local)

            var sent: Long = 0
            val total = local.length()
            var lastProgress = 0
            while (true) {
                val read = `is`.read(buff)
                if (read < 0) {
                    break
                }

                stream.write(ByteUtils.concat("DATA".toByteArray(), ByteUtils.intToByteArray(read)))

                if (read == buff.size) {
                    stream.write(buff)
                } else {
                    val tmp = ByteArray(read)
                    System.arraycopy(buff, 0, tmp, 0, read)
                    stream.write(tmp)
                }

                sent += read.toLong()

                val progress = (sent * 100 / total).toInt()
                if (lastProgress != progress) {
                    handler.sendMessage(
                        handler.obtainMessage(
                            MessageOtg.INSTALLING_PROGRESS, MessageOtg.PUSH_PART, progress
                        )
                    )
                    lastProgress = progress
                }
            }

            stream.write(
                ByteUtils.concat(
                    "DONE".toByteArray(),
                    ByteUtils.intToByteArray(System.currentTimeMillis().toInt())
                )
            )

            val res = stream.read()
            // TODO: test if res contains "OKEY" or "FAIL"
            Log.d(Const.TAG, String(res))

            stream.write(ByteUtils.concat("QUIT".toByteArray(), ByteUtils.intToByteArray(0)))
        }
    }

    object ExternalCmdStore {
        private var sharedPreferences: SharedPreferences? = null
        private const val CMD_KEY = "cmd_key"

        private fun initShared(context: Context) {
            if (sharedPreferences == null) sharedPreferences =
                context.getSharedPreferences("cmd", Context.MODE_PRIVATE)
        }

        fun put(context: Context, cmd: String?) {
            initShared(context)
            sharedPreferences?.edit() { putString(CMD_KEY, cmd) }
        }

        fun get(context: Context): String? {
            initShared(context)
            return sharedPreferences?.getString(CMD_KEY, null)
        }
    }

    class Install(
        private val adbConnection: AdbConnection,
        private val remotePath: String,
        installTimeAssumption: Long
    ) {
        private var installTimeAssumption: Long = 0

        init {
            this.installTimeAssumption = installTimeAssumption
        }

        @Throws(IOException::class, InterruptedException::class)
        fun execute(handler: Handler) {
            val done = AtomicBoolean(false)
            try {
                val stream = adbConnection.open("shell:pm install -r $remotePath")
                // we assume installation will take installTimeAssumption milliseconds.
                object : Thread() {
                    override fun run() {
                        var percent = 0

                        while (!done.get()) {
                            handler.sendMessage(
                                handler.obtainMessage(
                                    MessageOtg.INSTALLING_PROGRESS, MessageOtg.PM_INST_PART, percent
                                )
                            )

                            if (percent < 95) {
                                percent += 1
                                try {
                                    sleep(installTimeAssumption / 100)
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }.start()

                while (!stream.isClosed) {
                    try {
                        Log.d(Const.TAG, String(stream.read()))
                    } catch (e: IOException) {
                        // there must be a Stream Close Exception
                        break
                    }
                }
            } finally {
                done.set(true)
                handler.sendMessage(
                    handler.obtainMessage(
                        MessageOtg.INSTALLING_PROGRESS,
                        MessageOtg.PM_INST_PART,
                        100
                    )
                )
            }
        }
    }

    class MyAdbBase64 : AdbBase64 {
        override fun encodeToString(data: ByteArray): String {
            return Base64.encodeToString(data, Base64.NO_WRAP)
        }
    }
}
