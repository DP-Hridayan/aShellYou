package in.hridayan.ashell.UI;

public class Category {
  private String name;

  public Category(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static class CategoryAItem {
    private String title;
    private String description;
    private int imageResource;

    public CategoryAItem(String title, String description, int imageResource) {
      this.title = title;
      this.description = description;
      this.imageResource = imageResource;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public int getImageResource() {
      return imageResource;
    }
  }

  public static class CategoryBItem {
    private String id, title, description;
    private int imageResource;

    public CategoryBItem(String id, String title, String description, int imageResource) {
      this.id = id;
      this.title = title;
      this.description = description;
      this.imageResource = imageResource;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public int getImageResource() {
      return imageResource;
    }
  }

  public static class CategoryCItem {
    private String id, title, description;
    private int imageResource;

    public CategoryCItem(String id, String title, String description, int imageResource) {
      this.id = id;
      this.title = title;
      this.description = description;
      this.imageResource = imageResource;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public int getImageResource() {
      return imageResource;
    }
  }
}
