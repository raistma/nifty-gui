/*
 * Copyright (c) 2015, Nifty GUI Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.lessvoid.nifty.node;

import de.lessvoid.nifty.NiftyState;
import de.lessvoid.nifty.spi.node.NiftyLayoutReceiver;
import de.lessvoid.nifty.spi.node.NiftyNodeStateImpl;
import de.lessvoid.nifty.types.NiftyRect;
import de.lessvoid.niftyinternal.math.Mat4;

import javax.annotation.Nonnull;

import static de.lessvoid.nifty.NiftyState.NiftyStandardState.NiftyStateTransformationLocal;
import static de.lessvoid.nifty.NiftyState.NiftyStandardState.NiftyStateTransformationLocalToScreen;
import static de.lessvoid.nifty.NiftyState.NiftyStandardState.NiftyStateTransformationChanged;

/**
 * Created by void on 09.08.15.
 */
class NiftyTransformationNodeImpl
    implements
      NiftyNodeStateImpl<NiftyTransformationNode>,
      NiftyLayoutReceiver<NiftyTransformationNode> {
  private boolean transformationChanged = true;
  private double pivotX = 0.5;
  private double pivotY = 0.5;
  private double angleX = 0.0;
  private double angleY = 0.0;
  private double angleZ = 0.0;
  private double scaleX = 1.0;
  private double scaleY = 1.0;
  private double scaleZ = 1.0;
  private double posX = 0.0;
  private double posY = 0.0;
  private double posZ = 0.0;

  private Mat4 localTransformation;
  private Mat4 localTransformationInverse;
  private Mat4 localToScreenTransformation;
  private Mat4 screenToLocalTransformation;
  private float layoutWidth = 1.f;
  private float layoutHeight = 1.f;
  private float layoutX = 0.f;
  private float layoutY = 0.f;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NiftyNodeImpl
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public NiftyTransformationNode getNiftyNode() {
    return new NiftyTransformationNode(this);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NiftyNodeStateImpl
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void update(final NiftyState niftyState) {
    Mat4 parentLocalToScreen = niftyState.getState(NiftyStateTransformationLocalToScreen, Mat4.createIdentity());
    boolean parentLocalToScreenChanged = niftyState.getState(NiftyStateTransformationChanged, false);
    boolean thisStateChanged = isTransformationChanged() || parentLocalToScreenChanged;
    niftyState.setState(NiftyStateTransformationLocalToScreen, updateTransformation(parentLocalToScreen, parentLocalToScreenChanged));
    niftyState.setState(NiftyStateTransformationLocal, localTransformation);
    niftyState.setState(NiftyStateTransformationChanged, thisStateChanged);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NiftyLayoutReceiver
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void setLayoutResult(@Nonnull NiftyRect rect) {
    layoutX = rect.getOrigin().getX();
    layoutY = rect.getOrigin().getY();
    layoutWidth = rect.getSize().getWidth();
    layoutHeight = rect.getSize().getHeight();
  }

  public double getPivotX() {
    return pivotX;
  }

  public void setPivotX(final double pivotX) {
    updateTransformationChanged(this.pivotX, pivotX);
    this.pivotX = pivotX;
  }

  public double getPivotY() {
    return pivotY;
  }

  public void setPivotY(final double pivotY) {
    updateTransformationChanged(this.pivotY, pivotY);
    this.pivotY = pivotY;
  }

  public double getAngleX() {
    return angleX;
  }

  public void setAngleX(final double angleX) {
    updateTransformationChanged(this.angleX, angleX);
    this.angleX = angleX;
  }

  public double getAngleY() {
    return angleY;
  }

  public void setAngleY(final double angleY) {
    updateTransformationChanged(this.angleY, angleY);
    this.angleY = angleY;
  }

  public double getAngleZ() {
    return angleZ;
  }

  public void setAngleZ(final double angleZ) {
    updateTransformationChanged(this.angleZ, angleZ);
    this.angleZ = angleZ;
  }

  public double getScaleX() {
    updateTransformationChanged(this.scaleX, scaleX);
    return scaleX;
  }

  public void setScaleX(final double scaleX) {
    this.scaleX = scaleX;
  }

  public double getScaleY() {
    return scaleY;
  }

  public void setScaleY(final double scaleY) {
    updateTransformationChanged(this.scaleY, scaleY);
    this.scaleY = scaleY;
  }

  public double getScaleZ() {
    return scaleZ;
  }

  public void setScaleZ(final double scaleZ) {
    updateTransformationChanged(this.scaleZ, scaleZ);
    this.scaleZ = scaleZ;
  }

  public double getPosX() {
    return posX;
  }

  public void setPosX(final double posX) {
    updateTransformationChanged(this.posX, posX);
    this.posX = posX;
  }

  public double getPosY() {
    return posY;
  }

  public void setPosY(final double posY) {
    updateTransformationChanged(this.posY, posY);
    this.posY = posY;
  }

  public double getPosZ() {
    return posZ;
  }

  public void setPosZ(final double posZ) {
    updateTransformationChanged(this.posZ, posZ);
    this.posZ = posZ;
  }

  private Mat4 updateTransformation(final Mat4 parentTransformation, final boolean parentTransformationChanged) {
    if (!(isTransformationChanged() || parentTransformationChanged)) {
      return localToScreenTransformation;
    }
    localTransformation = new Mat4(buildLocalTransformation());
    localTransformationInverse = Mat4.invert(localTransformation, new Mat4());

    localToScreenTransformation = Mat4.mul(parentTransformation, localTransformation);
    screenToLocalTransformation = Mat4.invert(localToScreenTransformation, new Mat4());

    transformationChanged = false;
    return localToScreenTransformation;
  }

  private void updateTransformationChanged(final double oldValue, final double newValue) {
    if (newValue != oldValue) {
      transformationChanged = true;
    }
  }

  private Mat4 buildLocalTransformation() {
    float pivotX = (float) this.pivotX * layoutWidth;
    float pivotY = (float) this.pivotY * layoutHeight;
    return
        Mat4.mul(
          Mat4.mul(
              Mat4.mul(
                  Mat4.mul(
                      Mat4.mul(
                          Mat4.mul(
                                Mat4.createTranslate((float) posX, (float) posY, 0.f),
                            Mat4.createTranslate(pivotX, pivotY, 0.0f)),
                        Mat4.createRotate((float) angleX, 1.f, 0.f, 0.f)),
                    Mat4.createRotate((float) angleY, 0.f, 1.f, 0.f)),
                Mat4.createRotate((float) angleZ, 0.f, 0.f, 1.f)),
            Mat4.createScale((float) scaleX, (float) scaleY, (float) scaleZ)),
          Mat4.createTranslate(-pivotX, -pivotY, 0.0f));
  }

  private boolean isTransformationChanged() {
    return transformationChanged;
  }
}