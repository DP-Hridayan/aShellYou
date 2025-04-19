package in.hridayan.ashell.utils;

import android.content.Context;
import android.content.res.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import in.hridayan.ashell.items.CommandItems;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import in.hridayan.ashell.R;

public class Commands {
  private static List<CommandItems> mPackages = null;

  // Define a model for JSON parsing
  private static class CommandModel {
    String command;
    String example;
        String description;
  }

  // Load commands from JSON
  public static List<CommandItems> commandList(Context context) {
    List<CommandItems> mCommands = new ArrayList<>();
    try {
      // Load the JSON file from res/raw
      Resources resources = context.getResources();
      InputStream inputStream = resources.openRawResource(R.raw.commands);
      Reader reader = new InputStreamReader(inputStream);

      Gson gson = new Gson();
      Type commandListType = new TypeToken<List<CommandModel>>() {}.getType();
      List<CommandModel> commandModels = gson.fromJson(reader, commandListType);

      // Convert JSON models to CommandItems
      for (CommandModel model : commandModels) {
        mCommands.add(new CommandItems(model.command, model.example,model.description, context));
      }

      // Close the reader
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return mCommands;
  }

  public static List<CommandItems> getCommand(String command, Context context) {
    List<CommandItems> mCommands = new ArrayList<>();
    for (CommandItems commands : commandList(context)) {
      if (commands.getTitle().contains(command)) {
        mCommands.add(commands);
      }
    }
    return mCommands;
  }

  public static List<CommandItems> getPackageInfo(String command, Context context) {
    loadPackageInfo(context);
    List<CommandItems> mCommands = new ArrayList<>();
    for (CommandItems packages : mPackages) {
      if (packages.getTitle().contains(command)) {
        mCommands.add(packages);
      }
    }
    return mCommands;
  }

  public static void loadPackageInfo(Context context) {
    mPackages = new ArrayList<>();

    try {
      Process mProcess = Runtime.getRuntime().exec("pm list packages");
      BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
      String line;
      while ((line = mInput.readLine()) != null) {
        if (line.startsWith("package:")) {
          mPackages.add(new CommandItems(line.replace("package:", ""), null,null, context));
        }
      }
      mProcess.waitFor();
    } catch (Exception ignored) {
    }
  }
}
