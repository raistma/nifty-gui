package de.lessvoid.nifty.controls.checkbox;

import static org.easymock.EasyMock.capture;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.*;

import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;

public class CheckBoxImplTest {
  private CheckBoxImpl checkBox = new CheckBoxImpl(null);
  private CheckBoxView view;
  private Capture<CheckBoxStateChangedEvent> capturedEvent = new Capture<CheckBoxStateChangedEvent>();

  @Before
  public void before() {
    view = createMock(CheckBoxView.class);
    checkBox.bindToView(view);
  }

  @After
  public void after() {
    verify(view);
  }

  @Test
  public void testDefault() {
    replay(view);
    assertFalse(checkBox.isChecked());
  }

  @Test
  public void testCheck() {
    expectViewUpdate(true);
    checkBox.check();
    assertCheckBoxState(true);
  }

  @Test
  public void testUncheck() {
    expectViewUpdate(false);
    checkBox.uncheck();
    assertCheckBoxState(false);
  }

  @Test
  public void testSetToChecked() {
    expectViewUpdate(true);
    checkBox.setChecked(true);
    assertCheckBoxState(true);
  }

  @Test
  public void testSetToUnchecked() {
    expectViewUpdate(false);
    checkBox.setChecked(false);
    assertCheckBoxState(false);
  }

  @Test
  public void testToggle() {
    expectViewUpdate(true);
    checkBox.toggle();
    assertCheckBoxState(true);
  }

  @Test
  public void testToggleToOff() {
    view.update(false);
    view.publish(capture(capturedEvent));
    view.update(true);
    view.publish(capture(capturedEvent));
    replay(view);

    checkBox.uncheck();
    assertEquals(false, capturedEvent.getValue().isChecked());

    checkBox.toggle();
    assertEquals(true, capturedEvent.getValue().isChecked());

    assertEquals(true, checkBox.isChecked());
  }

  private void expectViewUpdate(final boolean expectedCheck) {
    view.update(expectedCheck);
    view.publish(capture(capturedEvent));
    replay(view);
  }

  private void assertCheckBoxState(final boolean expectedState) {
    assertEquals(expectedState, checkBox.isChecked());
    assertEquals(expectedState, capturedEvent.getValue().isChecked());
  }
}
