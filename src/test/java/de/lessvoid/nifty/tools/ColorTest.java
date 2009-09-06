package de.lessvoid.nifty.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColorTest {

  @Test
  public void testMultiply() {
    Color c = new Color(1.0f, 0.5f, 0.6f, 0.8f);
    Color m = c.mutiply(0.5f);
    checkColor(m, 0.5f, 0.25f, 0.3f, 0.4f);
  }

  @Test
  public void testColorShortModeWithoutAlpha() {
    Color c = new Color("#123");
    checkColor(c, 0.1f, 0.2f, 0.3f, 1.0f);
  }

  @Test
  public void testColorLongModeWithoutAlpha() {
    Color c = new Color("#102030");
    checkColor(c, 0.1f, 0.2f, 0.3f, 1.0f);
  }

  @Test
  public void testColorInvalid() {
    Color c = new Color("#1");
    checkColor(c, 1.0f, 1.0f, 1.0f, 1.0f);
    c = new Color("1");
    checkColor(c, 1.0f, 1.0f, 1.0f, 1.0f);
  }

  @Test
  public void testColorValid() {
    Color c = new Color("#1238");
    checkColor(c, 0.1f, 0.2f, 0.3f, 0.8f);

    c = new Color("#10203080");
    checkColor(c, 0.1f, 0.2f, 0.3f, 0.8f);
  }

  private void checkColor(Color m, float red, float green, float blue, float alpha) {
    assertEquals(red, m.getRed());
    assertEquals(green, m.getGreen());
    assertEquals(blue, m.getBlue());
    assertEquals(alpha, m.getAlpha());
  }
}
