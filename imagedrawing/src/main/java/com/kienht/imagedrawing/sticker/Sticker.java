package com.kienht.imagedrawing.sticker;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static java.lang.Math.round;

/**
 * @author wupanjie
 */
public abstract class Sticker {

    @IntDef(flag = true, value = {Position.CENTER, Position.TOP, Position.BOTTOM, Position.LEFT, Position.RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Position {
        int CENTER = 1;
        int TOP = 1 << 1;
        int LEFT = 1 << 2;
        int RIGHT = 1 << 3;
        int BOTTOM = 1 << 4;
    }

    private final float[] matrixValues = new float[9];
    private final float[] unrotatedWrapperCorner = new float[8];
    private final float[] unrotatedPoint = new float[2];
    private final float[] boundPoints = new float[8];
    private final float[] mappedBounds = new float[8];
    private final RectF trappedRect = new RectF();
    private final Matrix matrix = new Matrix();
    private boolean isFlippedHorizontally;
    private boolean isFlippedVertically;

    private float paddingLeftBorder = 0f;
    private float paddingRightBorder = 0f;
    private float paddingTopBorder = 0f;
    private float paddingBottomBorder = 0f;

    public boolean isFlippedHorizontally() {
        return isFlippedHorizontally;
    }

    @NonNull
    public Sticker setFlippedHorizontally(boolean flippedHorizontally) {
        isFlippedHorizontally = flippedHorizontally;
        return this;
    }

    public boolean isFlippedVertically() {
        return isFlippedVertically;
    }

    @NonNull
    public Sticker setFlippedVertically(boolean flippedVertically) {
        isFlippedVertically = flippedVertically;
        return this;
    }

    @NonNull
    public Matrix getMatrix() {
        return matrix;
    }

    public Sticker setMatrix(@Nullable Matrix matrix) {
        this.matrix.set(matrix);
        return this;
    }

    public abstract void draw(@NonNull Canvas canvas);

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract Sticker setDrawable(@NonNull Drawable drawable);

    @NonNull
    public abstract Drawable getDrawable();

    @NonNull
    public abstract Sticker setAlpha(@IntRange(from = 0, to = 255) int alpha);

    public float[] getBoundPoints() {
        float[] points = new float[8];
        getBoundPoints(points);
        return points;
    }

    public void getBoundPoints(@NonNull float[] points) {
        if (!isFlippedHorizontally) {
            if (!isFlippedVertically) {
                points[0] = 0f - paddingLeftBorder;
                points[1] = 0f - paddingTopBorder;
                points[2] = getWidth() + paddingRightBorder;
                points[3] = 0f - paddingTopBorder;
                points[4] = 0f - paddingLeftBorder;
                points[5] = getHeight() + paddingBottomBorder;
                points[6] = getWidth() + paddingRightBorder;
                points[7] = getHeight() + paddingBottomBorder;
            } else {
                points[0] = 0f - paddingLeftBorder;
                points[1] = getHeight() - paddingTopBorder;
                points[2] = getWidth() + paddingRightBorder;
                points[3] = getHeight() - paddingTopBorder;
                points[4] = 0f - paddingLeftBorder;
                points[5] = 0f + paddingBottomBorder;
                points[6] = getWidth() + paddingRightBorder;
                points[7] = 0f + paddingBottomBorder;
            }
        } else {
            if (!isFlippedVertically) {
                points[0] = getWidth() - paddingLeftBorder;
                points[1] = 0f - paddingTopBorder;
                points[2] = 0f + paddingRightBorder;
                points[3] = 0f - paddingTopBorder;
                points[4] = getWidth() - paddingLeftBorder;
                points[5] = getHeight() + paddingBottomBorder;
                points[6] = 0f + paddingRightBorder;
                points[7] = getHeight() + paddingBottomBorder;
            } else {
                points[0] = getWidth() - paddingLeftBorder;
                points[1] = getHeight() - paddingTopBorder;
                points[2] = 0f + paddingRightBorder;
                points[3] = getHeight() - paddingTopBorder;
                points[4] = getWidth() - paddingLeftBorder;
                points[5] = 0f + paddingBottomBorder;
                points[6] = 0f + paddingRightBorder;
                points[7] = 0f + paddingBottomBorder;
            }
        }
    }

    @NonNull
    public float[] getMappedBoundPoints() {
        float[] dst = new float[8];
        getMappedPoints(dst, getBoundPoints());
        return dst;
    }

    @NonNull
    public float[] getMappedPoints(@NonNull float[] src) {
        float[] dst = new float[src.length];
        matrix.mapPoints(dst, src);
        return dst;
    }

    public void getMappedPoints(@NonNull float[] dst, @NonNull float[] src) {
        matrix.mapPoints(dst, src);
    }

    @NonNull
    public RectF getBound() {
        RectF bound = new RectF();
        getBound(bound);
        return bound;
    }

    public void getBound(@NonNull RectF dst) {
        dst.set(0, 0, getWidth(), getHeight());
    }

    @NonNull
    public RectF getMappedBound() {
        RectF dst = new RectF();
        getMappedBound(dst, getBound());
        return dst;
    }

    public void getMappedBound(@NonNull RectF dst, @NonNull RectF bound) {
        matrix.mapRect(dst, bound);
    }

    @NonNull
    public PointF getCenterPoint() {
        PointF center = new PointF();
        getCenterPoint(center);
        return center;
    }

    public void getCenterPoint(@NonNull PointF dst) {
        dst.set(getWidth() * 1f / 2, getHeight() * 1f / 2);
    }

    @NonNull
    public PointF getMappedCenterPoint() {
        PointF pointF = getCenterPoint();
        getMappedCenterPoint(pointF, new float[2], new float[2]);
        return pointF;
    }

    public void getMappedCenterPoint(@NonNull PointF dst, @NonNull float[] mappedPoints,
                                     @NonNull float[] src) {
        getCenterPoint(dst);
        src[0] = dst.x;
        src[1] = dst.y;
        getMappedPoints(mappedPoints, src);
        dst.set(mappedPoints[0], mappedPoints[1]);
    }

    public float getCurrentScale() {
        return getMatrixScale(matrix);
    }

    public float getCurrentHeight() {
        return getMatrixScale(matrix) * getHeight();
    }

    public float getCurrentWidth() {
        return getMatrixScale(matrix) * getWidth();
    }

    /**
     * This method calculates scale value for given Matrix object.
     */
    public float getMatrixScale(@NonNull Matrix matrix) {
        return (float) Math.sqrt(Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X), 2) + Math.pow(
                getMatrixValue(matrix, Matrix.MSKEW_Y), 2));
    }

    /**
     * @return - current image rotation angle.
     */
    public float getCurrentAngle() {
        return getMatrixAngle(matrix);
    }

    /**
     * This method calculates rotation angle for given Matrix object.
     */
    public float getMatrixAngle(@NonNull Matrix matrix) {
        return (float) Math.toDegrees(-(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X),
                getMatrixValue(matrix, Matrix.MSCALE_X))));
    }

    public float getMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = 9) int valueIndex) {
        matrix.getValues(matrixValues);
        return matrixValues[valueIndex];
    }

    public boolean contains(float x, float y) {
        return contains(new float[]{x, y});
    }

    public boolean contains(@NonNull float[] point) {
        Matrix tempMatrix = new Matrix();
        tempMatrix.setRotate(-getCurrentAngle());
        getBoundPoints(boundPoints);
        getMappedPoints(mappedBounds, boundPoints);
        tempMatrix.mapPoints(unrotatedWrapperCorner, mappedBounds);
        tempMatrix.mapPoints(unrotatedPoint, point);
        trapToRect(trappedRect, unrotatedWrapperCorner);
        return trappedRect.contains(unrotatedPoint[0], unrotatedPoint[1]);
    }

    private void trapToRect(@NonNull RectF r, @NonNull float[] array) {
        r.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY);
        for (int i = 1; i < array.length; i += 2) {
            float x = round(array[i - 1] * 10) / 10.f;
            float y = round(array[i] * 10) / 10.f;
            r.left = (x < r.left) ? x : r.left;
            r.top = (y < r.top) ? y : r.top;
            r.right = (x > r.right) ? x : r.right;
            r.bottom = (y > r.bottom) ? y : r.bottom;
        }
        r.sort();
    }

    public void setPadding(float left, float top, float right, float bottom) {
        this.paddingLeftBorder = left;
        this.paddingTopBorder = top;
        this.paddingRightBorder = right;
        this.paddingBottomBorder = bottom;
    }

    public float getPaddingLeftBorder() {
        return paddingLeftBorder;
    }

    public void setPaddingLeftBorder(float paddingLeftBorder) {
        this.paddingLeftBorder = paddingLeftBorder;
    }

    public float getPaddingRightBorder() {
        return paddingRightBorder;
    }

    public void setPaddingRightBorder(float paddingRightBorder) {
        this.paddingRightBorder = paddingRightBorder;
    }

    public float getPaddingTopBorder() {
        return paddingTopBorder;
    }

    public void setPaddingTopBorder(float paddingTopBorder) {
        this.paddingTopBorder = paddingTopBorder;
    }

    public float getPaddingBottomBorder() {
        return paddingBottomBorder;
    }

    public void setPaddingBottomBorder(float paddingBottomBorder) {
        this.paddingBottomBorder = paddingBottomBorder;
    }

    public void release() {
    }
}
