package in.hridayan.ashell.UI;

public class CategoryAbout {
  private String name;

  public CategoryAbout(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static class LeadDeveloperItem {
    private String title;
    private String description;
    private int imageResource;

    public LeadDeveloperItem(String title, String description, int imageResource) {
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

  public static class ContributorsItem {
    private String id, title, description;
    private int imageResource;

    public ContributorsItem(String id, String title, String description, int imageResource) {
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

  public static class AppItem {
    private String id, title, description;
    private int imageResource;

    public AppItem(String id, String title, String description, int imageResource) {
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
