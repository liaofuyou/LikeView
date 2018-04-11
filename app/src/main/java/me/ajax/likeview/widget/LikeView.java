package me.ajax.likeview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.LinearInterpolator;

import static me.ajax.likeview.utils.GeometryUtils.getXFromPolar;
import static me.ajax.likeview.utils.GeometryUtils.getYFromPolar;


/**
 * Created by aj on 2018/4/2
 */

public class LikeView extends View {

    Paint mPaint = new Paint();

    SparseArray<ValueAnimator> animArr = new SparseArray<>(6);

    Path heartPath = new Path();
    int heartRadius = dp2Dx(50);
    int circleRadius = heartRadius + dp2Dx(15);

    PointF[] pointsF1 = new PointF[3];
    PointF[] pointsF2 = new PointF[3];
    PointF[] pointsF3 = new PointF[3];
    PointF[] pointsF4 = new PointF[3];

    int[] pointColors = new int[]{0xFF9ff048, 0xFF2A5200, 0xFFFF534D, 0xFF25C6FC, 0xFFFF5938, 0xFFC1194E, 0xFF1DB0B8, 0xFF37c6c0
            , 0xFF2E68AA, 0xFF77C34F, 0xFF65A36C, 0xFF5E8579, 0xFFFF534D, 0xFF1DB0B8, 0xFFFF5938, 0xFF2E68AA};

    int ANIM_GRAY_HEART = 0;
    int ANIM_PURPLE_CIRCLE = 1;
    int ANIM_WHITE_CIRCLE = 2;
    int ANIM_RED_HEART = 3;
    int ANIM_POINT_SHOW = 4;
    int ANIM_POINT_HIDE = 5;

    boolean isDrawGrayHeart = false;
    boolean isDrawPurpleCircle = false;
    boolean isDrawWhiteCircle = false;
    boolean isDrawRedHeart = false;
    boolean isDrawPointsShow = false;
    boolean isDrawPointsHide = false;

    boolean isStart = false;

    public LikeView(Context context) {
        super(context);
        init();
    }

    public LikeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LikeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//关闭硬件加速

        //画笔
        mPaint.setColor(0xFFFF00FF);
        mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setPathEffect(new CornerPathEffect(dp2Dx(9)));

        //实例化点
        for (int i = 0; i < 3; i++) {
            pointsF1[i] = new PointF();
            pointsF2[i] = new PointF();
            pointsF3[i] = new PointF();
            pointsF4[i] = new PointF();
        }
        refreshHeart(heartRadius);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startGrayHeart();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int mWidth = getWidth();
        int mHeight = getHeight();

        canvas.save();
        canvas.translate(mWidth / 2, mHeight / 2);

        //桃心
        if (isDrawGrayHeart) {
            mPaint.setColor(0XFFA2B4BA);
            refreshHeart(heartRadius);
            float fraction = animArr.get(ANIM_GRAY_HEART).getAnimatedFraction();
            canvas.scale(fraction, fraction, pointsF3[1].x, pointsF3[1].y);
            drawHeart(canvas, heartPath);
        }
        //圆1
        if (isDrawPurpleCircle) {
            mPaint.setColor(0xFF795DDE);
            float fraction = animArr.get(ANIM_PURPLE_CIRCLE).getAnimatedFraction();
            canvas.drawCircle(0, 0, circleRadius * fraction, mPaint);
        }
        //圆2
        if (isDrawWhiteCircle) {
            mPaint.setColor(Color.WHITE);
            float fraction = animArr.get(ANIM_WHITE_CIRCLE).getAnimatedFraction();
            canvas.drawCircle(0, 0, circleRadius * fraction, mPaint);
        }
        //桃心2
        if (!isStart || isDrawRedHeart) {
            mPaint.setColor(0xFFF10B2D);
            float fraction = !isStart ? 1 : animArr.get(ANIM_RED_HEART).getAnimatedFraction();
            refreshHeart(heartRadius * fraction);
            drawHeart(canvas, heartPath);
        }
        //小球运动
        if (!isStart || isDrawPointsShow) {
            float fraction = !isStart ? 1 : animArr.get(ANIM_POINT_SHOW).getAnimatedFraction();
            drawPoints(canvas, circleRadius + dp2Dx(30) * fraction, dp2Dx(5));
        }
        //小球隐藏
        if (isDrawPointsHide) {
            float fraction = animArr.get(ANIM_POINT_HIDE).getAnimatedFraction();
            drawPoints(canvas, circleRadius + dp2Dx(30),
                    (int) (dp2Dx(5) * (1 - fraction)));
        }

        canvas.restore();
    }

    /**
     * 刷新桃心参数
     */
    void refreshHeart(float circleRadius) {

        pointsF1[0].x = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointsF1[0].y = -circleRadius;
        pointsF1[1].x = 0;
        pointsF1[1].y = -circleRadius;
        pointsF1[2].x = (float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointsF1[2].y = -circleRadius;

        pointsF2[0].x = circleRadius;
        pointsF2[0].y = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointsF2[1].x = circleRadius;
        pointsF2[1].y = 0;
        pointsF2[2].x = circleRadius;
        pointsF2[2].y = (float) (Math.tan(Math.toRadians(30)) * circleRadius);

        pointsF3[0].x = (float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointsF3[0].y = circleRadius;
        pointsF3[1].x = 0;
        pointsF3[1].y = circleRadius;
        pointsF3[2].x = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointsF3[2].y = circleRadius;

        pointsF4[0].x = -circleRadius;
        pointsF4[0].y = (float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointsF4[1].x = -circleRadius;
        pointsF4[1].y = 0;
        pointsF4[2].x = -circleRadius;
        pointsF4[2].y = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);

        pointsF1[1].y += circleRadius / 3;
        pointsF3[1].y += circleRadius / 3;
    }

    /**
     * 绘制桃心
     */
    void drawHeart(Canvas canvas, Path path) {

        path.reset();
        path.moveTo(pointsF1[1].x, pointsF1[1].y);
        path.cubicTo(pointsF1[2].x, pointsF1[2].y, pointsF2[0].x, pointsF2[0].y, pointsF2[1].x, pointsF2[1].y);
        path.cubicTo(pointsF2[2].x, pointsF2[2].y, pointsF3[0].x, pointsF3[0].y, pointsF3[1].x, pointsF3[1].y);
        path.cubicTo(pointsF3[2].x, pointsF3[2].y, pointsF4[0].x, pointsF4[0].y, pointsF4[1].x, pointsF4[1].y);
        path.cubicTo(pointsF4[2].x, pointsF4[2].y, pointsF1[0].x, pointsF1[0].y, pointsF1[1].x, pointsF1[1].y);
        canvas.drawPath(path, mPaint);
    }

    /**
     * 绘制小点点
     */
    void drawPoints(Canvas canvas, float p, int size) {

        float p2 = p - dp2Dx(10);
        int j = 0;
        for (int i = 0; i < 8; i++, j += 2) {

            mPaint.setColor(pointColors[j]);
            canvas.drawCircle(getXFromPolar(p, i * 45), getYFromPolar(p, i * 45), size, mPaint);

            mPaint.setColor(pointColors[j + 1]);
            canvas.drawCircle(getXFromPolar(p2, i * 45 + 10), getYFromPolar(p2, i * 45 + 10), size, mPaint);
        }
    }


    /**
     * 启动灰色桃心动画
     */
    private void startGrayHeart() {

        isDrawGrayHeart = false;
        isDrawPurpleCircle = false;
        isDrawWhiteCircle = false;
        isDrawRedHeart = false;
        isDrawPointsShow = false;
        isDrawPointsHide = false;

        ValueAnimator animator = animArr.get(ANIM_GRAY_HEART);
        if (animator == null) {

            animator = ValueAnimator.ofFloat(0F, 1F);
            animator.setDuration(300);
            animator.setInterpolator(new LinearInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isDrawGrayHeart = false;
                    startPurpleCircle();
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
            animArr.append(ANIM_GRAY_HEART, animator);
        }

        if (!animator.isStarted() && !animator.isRunning()) {
            animator.cancel();
            animator.start();
            isDrawGrayHeart = true;
            isStart = true;
        }
    }

    /**
     * 启动紫色圆动画
     */
    private void startPurpleCircle() {

        ValueAnimator animator = animArr.get(ANIM_PURPLE_CIRCLE);
        if (animator == null) {

            animator = ValueAnimator.ofFloat(0F, 1F);
            animator.setDuration(300);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedFraction() > 0.1F) {
                        startWhiteCircle();
                    }
                    if (animation.getAnimatedFraction() > 0.5F) {
                        startRedHeart();
                    }
                }
            });
            animArr.append(ANIM_PURPLE_CIRCLE, animator);
        }

        if (!animator.isStarted() && !animator.isRunning()) {
            animator.cancel();
            animator.start();
            isDrawPurpleCircle = true;
        }
    }

    /**
     * 启动白色圆动画
     */
    private void startWhiteCircle() {

        ValueAnimator animator = animArr.get(ANIM_WHITE_CIRCLE);
        if (animator == null) {

            animator = ValueAnimator.ofFloat(0F, 1F);
            animator.setDuration(300);
            animator.setInterpolator(new LinearInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isDrawPurpleCircle = false;
                    isDrawWhiteCircle = false;
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedFraction() > 0.8F) {
                        startPointShow();
                    }
                    invalidate();
                }
            });
            animArr.append(ANIM_WHITE_CIRCLE, animator);
        }

        if (!animator.isStarted() && !animator.isRunning()) {
            animator.cancel();
            animator.start();
            isDrawWhiteCircle = true;
        }
    }

    /**
     * 启动红色桃心动画
     */
    private void startRedHeart() {

        ValueAnimator animator = animArr.get(ANIM_RED_HEART);
        if (animator == null) {

            animator = ValueAnimator.ofFloat(0F, 1F);
            animator.setDuration(300);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
            animArr.append(ANIM_RED_HEART, animator);
        }

        if (!animator.isStarted() && !animator.isRunning()) {
            animator.cancel();
            animator.start();
            isDrawRedHeart = true;
        }
    }

    /**
     * 启动小点点显示动画
     */
    private void startPointShow() {

        ValueAnimator animator = animArr.get(ANIM_POINT_SHOW);
        if (animator == null) {

            animator = ValueAnimator.ofFloat(0F, 1F);
            animator.setDuration(300);
            animator.setInterpolator(new LinearInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startPointHide();
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
            animArr.append(ANIM_POINT_SHOW, animator);
        }

        if (!animator.isStarted() && !animator.isRunning()) {
            animator.cancel();
            animator.start();
            isDrawPointsShow = true;
        }
    }

    /**
     * 启动小点点隐藏动画
     */
    private void startPointHide() {

        ValueAnimator animator = animArr.get(ANIM_POINT_HIDE);
        if (animator == null) {

            animator = ValueAnimator.ofFloat(0F, 1F);
            animator.setDuration(300);
            animator.setInterpolator(new LinearInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isDrawPointsShow = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isDrawPointsHide = false;
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
            animArr.append(ANIM_POINT_HIDE, animator);
        }

        if (!animator.isStarted() && !animator.isRunning()) {
            animator.cancel();
            animator.start();
            isDrawPointsHide = true;
        }
    }

    int dp2Dx(float dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }

    void l(Object o) {
        Log.e("######", o.toString());
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimAndRemoveCallbacks();
    }

    private void stopAnimAndRemoveCallbacks() {
        isStart = false;
        for (int i = 0; i < animArr.size(); i++) {
            if (animArr.get(i) != null) {
                animArr.get(i).end();
            }
        }
        Handler handler = this.getHandler();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

}
