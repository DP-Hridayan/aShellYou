package `in`.hridayan.ashell.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.items.CommandItems
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader

object Commands {
    private lateinit var mPackages: MutableList<CommandItems>

    // Load commands from JSON
    fun commandList(context: Context): List<CommandItems> {
        val mCommands: MutableList<CommandItems> = ArrayList<CommandItems>()
        try {
            // Load the JSON file from res/raw
            val resources = context.resources
            val inputStream = resources.openRawResource(R.raw.commands)
            val reader: Reader = InputStreamReader(inputStream)

            val gson = Gson()
            val commandListType = object : TypeToken<List<CommandModel?>?>() {}.type
            val commandModels = gson.fromJson<List<CommandModel>>(reader, commandListType)

            // Convert JSON models to CommandItems
            for (model in commandModels) {
                mCommands.add(
                    CommandItems(
                        model.command,
                        model.example,
                        model.description,
                        context
                    )
                )
            }

            // Close the reader
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mCommands
    }

    @JvmStatic
    fun getCommand(command: String, context: Context): List<CommandItems> {
        val mCommands: MutableList<CommandItems> = ArrayList<CommandItems>()
        for (commands in commandList(context)) {
            if (commands.title.startsWith(command)) {
                mCommands.add(commands)
            }
        }
        return mCommands
    }

    @JvmStatic
    fun getPackageInfo(command: String, context: Context?): List<CommandItems> {
        loadPackageInfo(context)
        val mCommands: MutableList<CommandItems> = ArrayList<CommandItems>()
        for (packages in mPackages) {
            if (packages.title.startsWith(command)) {
                mCommands.add(packages)
            }
        }
        return mCommands
    }

    private fun loadPackageInfo(context: Context?) {
        mPackages = ArrayList<CommandItems>()

        try {
            val mProcess = Runtime.getRuntime().exec("pm list packages")
            val mInput = BufferedReader(InputStreamReader(mProcess.inputStream))
            var line: String
            while ((mInput.readLine().also { line = it }) != null) {
                if (line.startsWith("package:")) {
                    mPackages.add(CommandItems(line.replace("package:", ""), null, null, context))
                }
            }
            mProcess.waitFor()
        } catch (ignored: Exception) {
        }
    }

    // Define a model for JSON parsing
    private class CommandModel {
        var command: String? = null
        var example: String? = null
        var description: String? = null
    }
}
