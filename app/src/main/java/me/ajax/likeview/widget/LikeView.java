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

import static me.ajax.likeview.utils.GeometryUtils.polarX;
import static me.ajax.likeview.utils.GeometryUtils.polarY;


/**
 * Created by aj on 2018/4/2
 */

public class LikeView extends View {

    Paint mPaint = new Paint();

    SparseArray<ValueAnimator> animArr = new SparseArray<>(6);

    Path heartPath = new Path();
    int heartRadius = dp2Dx(50);
    int circleRadius = heartRadius + dp2Dx(15);

    PointF[] dotsF1 = new PointF[3];
    PointF[] dotsF2 = new PointF[3];
    PointF[] dotsF3 = new PointF[3];
    PointF[] dotsF4 = new PointF[3];

    int[] dotColors = new int[]{0xFF9ff048, 0xFF2A5200, 0xFFFF534D, 0xFF25C6FC, 0xFFFF5938, 0xFFC1194E, 0xFF1DB0B8, 0xFF37c6c0
            , 0xFF2E68AA, 0xFF77C34F, 0xFF65A36C, 0xFF5E8579, 0xFFFF534D, 0xFF1DB0B8, 0xFFFF5938, 0xFF2E68AA};

    int ANIM_GRAY_HEART = 0;
    int ANIM_PURPLE_CIRCLE = 1;
    int ANIM_WHITE_CIRCLE = 2;
    int ANIM_RED_HEART = 3;
    int ANIM_DOT_SHOW = 4;
    int ANIM_DOT_HIDE = 5;

    boolean isDrawGrayHeart = false;
    boolean isDrawPurpleCircle = false;
    boolean isDrawWhiteCircle = false;
    boolean isDrawRedHeart = false;
    boolean isDrawDotsShow = false;
    boolean isDrawDotsHide = false;

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
            dotsF1[i] = new PointF();
            dotsF2[i] = new PointF();
            dotsF3[i] = new PointF();
            dotsF4[i] = new PointF();
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
            canvas.scale(fraction, fraction, dotsF3[1].x, dotsF3[1].y);
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
        if (!isStart || isDrawDotsShow) {
            float fraction = !isStart ? 1 : animArr.get(ANIM_DOT_SHOW).getAnimatedFraction();
            drawDots(canvas, circleRadius + dp2Dx(30) * fraction, dp2Dx(5));
        }
        //小球隐藏
        if (isDrawDotsHide) {
            float fraction = animArr.get(ANIM_DOT_HIDE).getAnimatedFraction();
            drawDots(canvas, circleRadius + dp2Dx(30),
                    (int) (dp2Dx(5) * (1 - fraction)));
        }

        canvas.restore();
    }

    /**
     * 刷新桃心参数
     */
    void refreshHeart(float circleRadius) {

        dotsF1[0].x = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);
        dotsF1[0].y = -circleRadius;
        dotsF1[1].x = 0;
        dotsF1[1].y = -circleRadius;
        dotsF1[2].x = (float) (Math.tan(Math.toRadians(30)) * circleRadius);
        dotsF1[2].y = -circleRadius;

        dotsF2[0].x = circleRadius;
        dotsF2[0].y = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);
        dotsF2[1].x = circleRadius;
        dotsF2[1].y = 0;
        dotsF2[2].x = circleRadius;
        dotsF2[2].y = (float) (Math.tan(Math.toRadians(30)) * circleRadius);

        dotsF3[0].x = (float) (Math.tan(Math.toRadians(30)) * circleRadius);
        dotsF3[0].y = circleRadius;
        dotsF3[1].x = 0;
        dotsF3[1].y = circleRadius;
        dotsF3[2].x = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);
        dotsF3[2].y = circleRadius;

        dotsF4[0].x = -circleRadius;
        dotsF4[0].y = (float) (Math.tan(Math.toRadians(30)) * circleRadius);
        dotsF4[1].x = -circleRadius;
        dotsF4[1].y = 0;
        dotsF4[2].x = -circleRadius;
        dotsF4[2].y = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);

        dotsF1[1].y += circleRadius / 3;
        dotsF3[1].y += circleRadius / 3;
    }

    /**
     * 绘制桃心
     */
    void drawHeart(Canvas canvas, Path path) {

        path.reset();
        path.moveTo(dotsF1[1].x, dotsF1[1].y);
        path.cubicTo(dotsF1[2].x, dotsF1[2].y, dotsF2[0].x, dotsF2[0].y, dotsF2[1].x, dotsF2[1].y);
        path.cubicTo(dotsF2[2].x, dotsF2[2].y, dotsF3[0].x, dotsF3[0].y, dotsF3[1].x, dotsF3[1].y);
        path.cubicTo(dotsF3[2].x, dotsF3[2].y, dotsF4[0].x, dotsF4[0].y, dotsF4[1].x, dotsF4[1].y);
        path.cubicTo(dotsF4[2].x, dotsF4[2].y, dotsF1[0].x, dotsF1[0].y, dotsF1[1].x, dotsF1[1].y);
        canvas.drawPath(path, mPaint);
    }

    /**
     * 绘制小点点
     */
    void drawDots(Canvas canvas, float p, int size) {

        float p2 = p - dp2Dx(10);
        int j = 0;
        for (int i = 0; i < 8; i++, j += 2) {

            mPaint.setColor(dotColors[j]);
            canvas.drawCircle(polarX(p, i * 45), polarY(p, i * 45), size, mPaint);

            mPaint.setColor(dotColors[j + 1]);
            canvas.drawCircle(polarX(p2, i * 45 + 10), polarY(p2, i * 45 + 10), size, mPaint);
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
        isDrawDotsShow = false;
        isDrawDotsHide = false;

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
                    invalidateView();
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
                        startDotsShow();
                    }
                    invalidateView();
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
                    invalidateView();
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
    private void startDotsShow() {

        ValueAnimator animator = animArr.get(ANIM_DOT_SHOW);
        if (animator == null) {

            animator = ValueAnimator.ofFloat(0F, 1F);
            animator.setDuration(300);
            animator.setInterpolator(new LinearInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startDotsHide();
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidateView();
                }
            });
            animArr.append(ANIM_DOT_SHOW, animator);
        }

        if (!animator.isStarted() && !animator.isRunning()) {
            animator.cancel();
            animator.start();
            isDrawDotsShow = true;
        }
    }

    /**
     * 启动小点点隐藏动画
     */
    private void startDotsHide() {

        ValueAnimator animator = animArr.get(ANIM_DOT_HIDE);
        if (animator == null) {

            animator = ValueAnimator.ofFloat(0F, 1F);
            animator.setDuration(300);
            animator.setInterpolator(new LinearInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isDrawDotsShow = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isDrawDotsHide = false;
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidateView();
                }
            });
            animArr.append(ANIM_DOT_HIDE, animator);
        }

        if (!animator.isStarted() && !animator.isRunning()) {
            animator.cancel();
            animator.start();
            isDrawDotsHide = true;
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
