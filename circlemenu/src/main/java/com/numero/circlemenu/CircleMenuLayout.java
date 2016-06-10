package com.numero.circlemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CircleMenuLayout extends ViewGroup {

    private int radius;
    private float padding;
    private double startAngle = 0;
    private int itemImages[];
    private String itemTexts[];
    private int menuItemCount;
    private float tmpAngle;
    private long downTime;
    private boolean isFling;
    private float lastX;
    private float lastY;

    private AutoFlingRunnable flingRunnable;
    private OnMenuItemClickListener onMenuItemClickListener;

    public CircleMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(Math.min(width, height), Math.min(width, height));

        radius = Math.max(getMeasuredWidth(), getMeasuredHeight());

        int count = getChildCount();
        int childSize = (int) (radius * (1 / 4f));
        int childMode = MeasureSpec.EXACTLY;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            int makeMeasureSpec;
            makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize, childMode);
            child.measure(makeMeasureSpec, makeMeasureSpec);
        }

        padding = (1 / 20f) * radius;

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left, top;
        int count = getChildCount();
        int childSize = (int) (radius * (1 / 4f));
        float angleDelay = 360 / getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            startAngle %= 360;

            float tmp = radius / 2f - childSize / 2 - padding;
            left = radius / 2 + (int) Math.round(tmp * Math.cos(Math.toRadians(startAngle - 90)) - 1 / 2f * childSize);
            top = radius / 2 + (int) Math.round(tmp * Math.sin(Math.toRadians(startAngle - 90)) - 1 / 2f * childSize);

            child.layout(left, top, left + childSize, top + childSize);
            startAngle += angleDelay;
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float positionX = event.getX();
        float positionY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = positionX;
                lastY = positionY;
                downTime = System.currentTimeMillis();
                tmpAngle = 0;

                if (isFling) {
                    removeCallbacks(flingRunnable);
                    isFling = false;
                    return true;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                float start = getAngle(lastX, lastY);
                float end = getAngle(positionX, positionY);

                if (getQuadrant(positionX, positionY) == 1 || getQuadrant(positionX, positionY) == 4) {
                    startAngle += end - start;
                    tmpAngle += end - start;
                } else {
                    startAngle += start - end;
                    tmpAngle += start - end;
                }
                requestLayout();

                lastX = positionX;
                lastY = positionY;

                break;
            case MotionEvent.ACTION_UP:
                float anglePerSecond = tmpAngle * 1000 / (System.currentTimeMillis() - downTime);
                if (Math.abs(anglePerSecond) > 300 && !isFling) {
                    post(flingRunnable = new AutoFlingRunnable(anglePerSecond));
                    return true;
                }
                if (Math.abs(tmpAngle) > 3) {
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private float getAngle(float positionX, float positionY) {
        double x = positionX - (radius / 2d);
        double y = positionY - (radius / 2d);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - radius / 2);
        int tmpY = (int) (y - radius / 2);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }
    }

    public void setMenuItemIconsAndTexts(int images[], String texts[]) {
        if (images == null || texts == null) {
            throw new IllegalArgumentException();
        }
        itemImages = images;
        itemTexts = texts;
        menuItemCount = Math.min(images.length, texts.length);
        addMenuItems();
    }

    private void addMenuItems() {
        LayoutInflater mInflater = LayoutInflater.from(getContext());

        for (int i = 0; i < menuItemCount; i++) {
            final int j = i;
            View view = mInflater.inflate(R.layout.circle_item_layout, this, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            TextView textView = (TextView) view.findViewById(R.id.text);

            if (imageView != null) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(itemImages[i]);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onMenuItemClickListener != null) {
                            onMenuItemClickListener.itemClick(v, j);
                        }
                    }
                });
            }
            if (textView != null) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(itemTexts[i]);
            }

            addView(view);
        }
    }

    public interface OnMenuItemClickListener {
        void itemClick(View view, int position);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener mOnMenuItemClickListener) {
        this.onMenuItemClickListener = mOnMenuItemClickListener;
    }

    private class AutoFlingRunnable implements Runnable {

        private float angelPerSecond;

        public AutoFlingRunnable(float velocity) {
            this.angelPerSecond = velocity;
        }

        public void run() {
            if ((int) Math.abs(angelPerSecond) < 20) {
                isFling = false;
                return;
            }
            isFling = true;
            startAngle += (angelPerSecond / 30);
            angelPerSecond /= 1.0666F;
            postDelayed(this, 30);
            requestLayout();
        }
    }
}
