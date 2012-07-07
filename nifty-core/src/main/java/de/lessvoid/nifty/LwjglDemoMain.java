package de.lessvoid.nifty;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Matrix4f;

import de.lessvoid.coregl.CheckGL;
import de.lessvoid.coregl.Shader;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.layout.Box;
import de.lessvoid.nifty.tools.Color;
import de.lessvoid.nifty.tools.DeltaTime;
import de.lessvoid.nifty.tools.timeprovider.FastTimeProvider;

public class LwjglDemoMain {
  private static Logger log = Logger.getLogger(LwjglDemoMain.class.getName());
  static float left = 0;
  static float right = 1024;
  static float top = 0;
  static float bottom = 768;
  static float zNear = -9999;
  static float zFar = 9999;

  public static void main(final String[] args) throws Exception {
    initialize("Nifty 2.0 Demo", 1024, 768);

    final Element e = new Element();

    for (int i=0; i<1000; i++) {
      Element ee = new Element();
      e.addElement(ee);
    }

    Matrix4f projection = new Matrix4f();
    projection.m00 = 2 / (right-left);
    projection.m30 = - (right+left) / (right-left);
    projection.m11 = 2 / (top-bottom);
    projection.m31 = - (top+bottom) / (top-bottom);
    projection.m22 = -2 / (zFar-zNear);
    projection.m32 = - (zFar+zNear) / (zFar-zNear);
    projection.m33 = 1;
    System.out.println(projection);

    final Shader shader = new Shader("vVertex", "vTexCoords");
    shader.compile("nifty.vs", "nifty.fs");
    shader.activate();
    shader.setUniform("mProjection", projection);
    shader.setUniform("tex", 0);

    renderLoop(new RenderLoopCallback() {
      @Override
      public boolean process() {
        e.update();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
//        e.render(shader);
        return false;
      }
    });
    destroy();
  }

  private static void renderLoop(final RenderLoopCallback renderLoop) {
    boolean done = false;
    long frames = 0;
    long lastFrames = 0;
    long time = System.currentTimeMillis();

    while (!Display.isCloseRequested() && !done) {
      done = renderLoop.process();
      Display.update();
      CheckGL.checkGLError("render loop check for errors");

      frames++;
      long diff = System.currentTimeMillis() - time;
      if (diff >= 1000) {
        time += diff;
        lastFrames = frames;
        System.out.println("fps: " + frames + " (" + 1000 / (float) frames + " ms)");
        frames = 0;
      }
    }
  }

  private static void initialize(final String title, final int width, final int height) throws Exception {
    initLogging();
    initGraphics(title, width, height);
    initInput();
  }

  private static void initLogging() {
    for (Handler handler : Logger.getLogger("").getHandlers()) {
      handler.setFormatter(new Formatter() {
        @Override
        public String format(final LogRecord record) {
          Throwable throwable = record.getThrown();
          if (throwable != null) {
            throwable.printStackTrace();
          }
           return
             record.getMillis() + " " +  
             record.getLevel() + " [" +
             record.getSourceClassName() + "] " +
             record.getMessage() + "\n";
        }
      });
    }
  }

  private static void initGraphics(final String title, final int WIDTH, final int HEIGHT) throws Exception {
    int width = 1920;
    int height = 1200;
    DisplayMode currentMode = Display.getDisplayMode();
    log.fine("currentmode: " + currentMode.getWidth() + ", " + currentMode.getHeight() + ", "
        + currentMode.getBitsPerPixel() + ", " + currentMode.getFrequency());

    width = currentMode.getWidth();
    height = currentMode.getHeight();

    // get available modes, and print out
    DisplayMode[] modes = Display.getAvailableDisplayModes();
    log.fine("Found " + modes.length + " display modes");

    List<DisplayMode> matching = new ArrayList<DisplayMode>();
    for (int i = 0; i < modes.length; i++) {
      DisplayMode mode = modes[i];
      if (mode.getWidth() == WIDTH && mode.getHeight() == HEIGHT && mode.getBitsPerPixel() == 32) {
        log.fine(mode.getWidth() + ", " + mode.getHeight() + ", " + mode.getBitsPerPixel() + ", " + mode.getFrequency());
        matching.add(mode);
      }
    }

    DisplayMode[] matchingModes = matching.toArray(new DisplayMode[0]);

    // find mode with matching freq
    boolean found = false;
    for (int i = 0; i < matchingModes.length; i++) {
      if (matchingModes[i].getFrequency() == currentMode.getFrequency()) {
        log.fine("using mode: " + matchingModes[i].getWidth() + ", " + matchingModes[i].getHeight() + ", "
            + matchingModes[i].getBitsPerPixel() + ", " + matchingModes[i].getFrequency());
        Display.setDisplayMode(matchingModes[i]);
        found = true;
        break;
      }
    }

    if (!found) {
      Arrays.sort(matchingModes, new Comparator<DisplayMode>() {
        public int compare(final DisplayMode o1, final DisplayMode o2) {
          if (o1.getFrequency() > o2.getFrequency()) {
            return 1;
          } else if (o1.getFrequency() < o2.getFrequency()) {
            return -1;
          } else {
            return 0;
          }
        }
      });

      for (int i = 0; i < matchingModes.length; i++) {
        log.fine("using fallback mode: " + matchingModes[i].getWidth() + ", " + matchingModes[i].getHeight() + ", "
            + matchingModes[i].getBitsPerPixel() + ", " + matchingModes[i].getFrequency());
        Display.setDisplayMode(matchingModes[i]);
        break;
      }
    }

    int x = (width - Display.getDisplayMode().getWidth()) / 2;
    int y = (height - Display.getDisplayMode().getHeight()) / 2;
    Display.setLocation(x, y);

    // Create the actual window
    try {
      Display.setFullscreen(false);
      Display.create(new PixelFormat(), new ContextAttribs(3, 2).withProfileCore(true));
      Display.setVSyncEnabled(false);
      Display.setTitle(title);
    } catch (Exception e) {
      e.printStackTrace();
      log.warning("Unable to create window!, exiting...");
      System.exit(-1);
    }

    log.info("Width: " + Display.getDisplayMode().getWidth() + ", Height: " + Display.getDisplayMode().getHeight()
        + ", Bits per pixel: " + Display.getDisplayMode().getBitsPerPixel() + ", Frequency: "
        + Display.getDisplayMode().getFrequency() + ", Title: " + Display.getTitle());

    // just output some infos about the system we're on
    log.info("plattform: " + LWJGLUtil.getPlatformName());
    log.info("opengl version: " + GL11.glGetString(GL11.GL_VERSION));
    log.info("opengl vendor: " + GL11.glGetString(GL11.GL_VENDOR));
    log.info("opengl renderer: " + GL11.glGetString(GL11.GL_RENDERER));
    CheckGL.checkGLError("init phase 1");

    IntBuffer viewportBuffer = BufferUtils.createIntBuffer(4 * 4);
    GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer);
    int viewportWidth = viewportBuffer.get(2);
    int viewportHeight = viewportBuffer.get(3);

    GL11.glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());

    GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    CheckGL.checkGLError("global initialize");
  }

  private static void initInput() throws Exception {
  }

  private static void destroy() {
    Display.destroy();
  }

  public interface RenderLoopCallback {
    boolean process();
  }
}