package com.naxanria.mappy.map.waypoint;

import com.naxanria.mappy.client.DrawableHelperBase;
import com.naxanria.mappy.client.ScreenBase;
import com.naxanria.mappy.util.BiValue;
import com.naxanria.mappy.util.MathUtil;
import com.naxanria.mappy.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WayPointListEditor extends ScreenBase
{
  public static final Identifier DEFAULT_IDENTIFIER = new Identifier("textures/block/dirt.png");
  public static final HashMap<String, BiValue<String, Identifier>> DIMENSION_INFO = new HashMap<String, BiValue<String, Identifier>>(){{
    put("minecraft:overworld", new BiValue<>("Overworld", new Identifier("textures/block/stone.png")));
    put("minecraft:the_nether", new BiValue<>("Nether", new Identifier("textures/block/nether_bricks.png")));
    put("minecraft:the_end", new BiValue<>("The End", new Identifier("textures/block/end_stone_bricks.png")));
  }};
  
  private static class Entry extends DrawableHelperBase implements Element
  {
    private MinecraftClient minecraft;
  
    private int x;
    private int y;
    private int width, height;
    private WayPoint wayPoint;
    private ButtonWidget editButton;
    private ButtonWidget deleteButton;
  
    public Entry(WayPointListEditor wayPointListEditor, int x, int y, int width, int height, WayPoint wayPoint)
    {
      this.width = width;
      this.height = height;
      this.wayPoint = wayPoint;
      
      minecraft = MinecraftClient.getInstance();
      
      editButton = new ButtonWidget(0, 0, 40, height, "Edit", (b) -> wayPointListEditor.edit(wayPoint));
      deleteButton = new ButtonWidget(0, 0, 40, height, "Delete", (b) -> wayPointListEditor.delete(wayPoint));
      
      setPosition(x, y);
    }
  
    public void setPosition(int x, int y)
    {
      this.x = x;
      this.y = y;
      
      editButton.y = y;
      editButton.x = x + width - editButton.getWidth() - deleteButton.getWidth();
      deleteButton.y = y;
      deleteButton.x = x + width - deleteButton.getWidth();
    }
    
    public void render(int mouseX, int mouseY, float delta)
    {
      TextRenderer font = minecraft.textRenderer;
      
      // background
      boolean hover = isMouseOver(mouseX, mouseY);
      int bgColor = hover ? 0x88aaaaaa : 0x88333333;
      fill(x, y, x + width, y + height, bgColor);
      
      // todo: other icons if needed
      int diamondSize = height - 2;
      diamond(x, y + 1, diamondSize, diamondSize, wayPoint.color);
      
      int stringY = y + 6;
      
      int nameX = x + diamondSize + 2;

      drawString(font, wayPoint.name, nameX, stringY, WHITE);
      
      int posX = x + width - editButton.getWidth() - deleteButton.getWidth() - 2;
      drawRightAlignedString(font, Util.prettyFy(wayPoint.pos), posX, stringY, WHITE);
      
      editButton.render(mouseX, mouseY, delta);
      deleteButton.render(mouseX, mouseY, delta);
    }
  
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1)
    {
      return editButton.mouseClicked(double_1, double_2, int_1) || deleteButton.mouseClicked(double_1, double_2, int_1);
    }
  
    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1)
    {
      return editButton.mouseReleased(double_1, double_2, int_1) || deleteButton.mouseReleased(double_1, double_2, int_1);
    }
  
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
  }
  
  private static final TextComponent title = new StringTextComponent("Waypoints");
  
  private WayPointManager manager = WayPointManager.INSTANCE;
  private int currentDim = 0;
  private int currentDimIndex = 0;
  private List<Integer> dimensions;
  private List<WayPoint> wayPoints;
  private List<Entry> entries = new ArrayList<>();
  private BiValue<String, Identifier> info;

  private int scrollAmount = 0;
  private int maxScroll = 0;
  
  private ButtonWidget prevDimensionButton, nextDimensionButton;
  private ButtonWidget closeButton;
  
  private int x, y;
  private int width, height;
  
  public WayPointListEditor(Screen parent)
  {
    super(title, parent);
    if (minecraft == null)
    {
      minecraft = MinecraftClient.getInstance();
    }
    
    dimensions = manager.getWaypointDimensions();
    currentDim = minecraft.player.dimension.getRawId();
    currentDimIndex = getDimIndex(currentDim);
  }
  
  @Override
  protected void init()
  {
    width = Math.max(300, minecraft.window.getScaledWidth() / 2);
    height = minecraft.window.getScaledHeight();
  
    x = minecraft.window.getScaledWidth() / 2 - width / 2;
    
    prevDimensionButton = new ButtonWidget(x + 10, 10, 20, 20, "<", (b) -> cycleDimension(-1));
    nextDimensionButton = new ButtonWidget(x + width - 20 - 10, 10, 20, 20, ">", (b) -> cycleDimension(1));
    
    closeButton = new ButtonWidget(x + 10, height - 25, 60, 20, "Close", (b) -> onClose());
  
    reset();
  }
  
  private void createEntries()
  {
    entries.clear();
  
    int y = 40;
    for (WayPoint wp :
      wayPoints)
    {
      Entry entry = new Entry(this, x + 10, scrollAmount + y, width - 10 - 10, 20, wp);
      entries.add(entry);
      
      y += entry.height;
    }
  }
  
  private void updateEntries()
  {
    int y = 40;
    for (Entry entry :
      entries)
    {
      entry.setPosition(x + 10, scrollAmount + y);
      y += entry.height;
    }
  }
  
  
  private void cycleDimension(int i)
  {
    currentDimIndex += i;
    if (currentDimIndex >= dimensions.size())
    {
      currentDimIndex = 0;
    }
    else if (currentDimIndex < 0)
    {
      currentDimIndex = dimensions.size() - 1;
    }
    
    currentDim = dimensions.get(currentDimIndex);
    reset();
  }
  
  private int getDimIndex(int dim)
  {
    for (int i = 0; i < dimensions.size(); i++)
    {
      int dimId = dimensions.get(i);
      if (dimId == dim)
      {
        return i;
      }
    }
    
    return 0;
  }
  
  public void reset()
  {
    info = getDimensionInfo(currentDim);
    
    wayPoints = manager.getWaypoints(currentDim);
    createEntries();
    maxScroll = wayPoints.size() * 20;
    
    children.clear();
    children.addAll(entries);
    children.add(closeButton);
    children.add(prevDimensionButton);
    children.add(nextDimensionButton);
  }
  
  @Override
  public void render(int mouseX, int mouseY, float delta)
  {
    // background
    fill(x, 0, x + width, height, 0x33444444);
    
    // entries
    entries.forEach(e -> e.render(mouseX, mouseY, delta));
    
    drawBorders(mouseX, mouseY, delta);
//    System.out.println("RENDER");
    
    prevDimensionButton.render(mouseX, mouseY, delta);
  
    String dimensionName = info == null ? "Unknown" : info.A;
    drawCenteredString(font, dimensionName, 130 / 2 + prevDimensionButton.x + prevDimensionButton.getWidth(), 15, 0xffffffff);
    
    nextDimensionButton.render(mouseX, mouseY, delta);
    
    closeButton.render(mouseX, mouseY, delta);
    
    drawScrollBar();
  }
  
  private BiValue<String, Identifier> getDimensionInfo(int dim)
  {
    DimensionType type = DimensionType.byRawId(dim);
    String key = "unknown";
    if (type != null)
    {
       key = type.toString();
    }
    
    return DIMENSION_INFO.getOrDefault(key, null);
  }
  
  private void drawScrollBar()
  {
  }
  
  private void drawBorders(int mouseX, int mouseY, float delta)
  {
    Identifier id = info.B;
    if (id == null)
    {
      id = DEFAULT_IDENTIFIER;// "textures/block/dirt.png";
    }

//    fill(x, 0, x + width, 40, bgColor);
    renderTextureRepeating(x, 0, width, 40, 16, 16, id);
//    fill(x, height - 40, x + width, height, bgColor);
    renderTextureRepeating(x, height - 40, width, 40, 16, 16, id);
    
  }
  
  private void edit(WayPoint wayPoint)
  {
    minecraft.openScreen(new WayPointEditor(wayPoint, this, null));
  }
  
  private void tp(int wp)
  {
    // todo: Teleporting player.
  }
  
  private void delete(WayPoint wayPoint)
  {
    manager.remove(wayPoint);
    reset();
  }
  
  @Override
  public boolean mouseScrolled(double double_1, double double_2, double double_3)
  {
    scrollAmount = MathUtil.clamp(scrollAmount + (int) (double_3 * 12), -maxScroll + 80, 0);
    updateEntries();
    
    return true;
  }
}
