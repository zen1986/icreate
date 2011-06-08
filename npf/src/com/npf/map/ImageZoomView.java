package com.npf.map;

/*
 * Copyright (cursor) 2010, Sony Ericsson Mobile Communication AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this 
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the Sony Ericsson Mobile Communication AB nor the names
 *      of its contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import com.npf.data.MapNode;
import com.npf.main.OutputManager;

/**
 * View capable of drawing an image at different zoom state levels
 */
public class ImageZoomView extends View implements Observer {

    /** Paint object used when drawing bitmap. */
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    /** Rectangle used (and re-used) for cropping source image. */
    private final Rect mRectSrc = new Rect();

    /** Rectangle used (and re-used) for specifying drawing area on canvas. */
    private final Rect mRectDst = new Rect();

    /** Object holding aspect quotient */
    private final AspectQuotient mAspectQuotient = new AspectQuotient();

    /** The bitmap that we're zooming in, and drawing on the screen. */
    private Bitmap bmMap;

    /** State of the zoom. */
    private ZoomState mState;
    
    private OutputManager mm;
    // Public methods

    /**
     * Constructor
     */
    

    
    public ImageZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mm = OutputManager.getInstance(null);
        bmMap = mm.getBitmap();
        int w=bmMap.getWidth();
        int h=bmMap.getHeight();
        mAspectQuotient.updateAspectQuotient(getWidth(), getHeight(), w, h);
        mAspectQuotient.notifyObservers();
        invalidate();
    }
    


    /**
     * Set image bitmap
     * 
     * @param bitmap The bitmap to view and zoom into
     */
    public void setImage(Bitmap bitmap) {
        bmMap = bitmap;

        
    }
    

    /**
     * Set object holding the zoom state that should be used
     * 
     * @param state The zoom state
     */
    public void setZoomState(ZoomState state) {
        if (mState != null) {
            mState.deleteObserver(this);
        }

        mState = state;
        mState.addObserver(this);

        invalidate();
    }


    /**
     * Gets reference to object holding aspect quotient
     * 
     * @return Object holding aspect quotient
     */
    public AspectQuotient getAspectQuotient() {
        return mAspectQuotient;
    }

    // Superclass overrides

    @Override
    protected void onDraw(Canvas canvas) {
    	bmMap = mm.getBitmap();
        if (bmMap != null && mState != null) {
            final float aspectQuotient = mAspectQuotient.get();

            final int viewWidth = getWidth();
            final int viewHeight = getHeight();
            final int bitmapWidth = bmMap.getWidth();
            final int bitmapHeight = bmMap.getHeight();

            final float panX = mState.getPanX();
            final float panY = mState.getPanY();
            final float zoomX = mState.getZoomX(aspectQuotient) * viewWidth / bitmapWidth;
            final float zoomY = mState.getZoomY(aspectQuotient) * viewHeight / bitmapHeight;

            // Setup source and destination rectangles
            mRectSrc.left = (int)(panX * bitmapWidth - viewWidth / (zoomX * 2));
            mRectSrc.top = (int)(panY * bitmapHeight - viewHeight / (zoomY * 2));
            mRectSrc.right = (int)(mRectSrc.left + viewWidth / zoomX);
            mRectSrc.bottom = (int)(mRectSrc.top + viewHeight / zoomY);
            mRectDst.left = getLeft();
            mRectDst.top = getTop();
            mRectDst.right = getRight();
            mRectDst.bottom = getBottom();

            // Adjust source rectangle so that it fits within the source image.
            if (mRectSrc.left < 0) {
                mRectDst.left += -mRectSrc.left * zoomX;
                mRectSrc.left = 0;
            }
            if (mRectSrc.right > bitmapWidth) {
                mRectDst.right -= (mRectSrc.right - bitmapWidth) * zoomX;
                mRectSrc.right = bitmapWidth;
            }
            if (mRectSrc.top < 0) {
                mRectDst.top += -mRectSrc.top * zoomY;
                mRectSrc.top = 0;
            }
            if (mRectSrc.bottom > bitmapHeight) {
                mRectDst.bottom -= (mRectSrc.bottom - bitmapHeight) * zoomY;
                mRectSrc.bottom = bitmapHeight;
            }
            
            Bitmap bm = addOverlay(bmMap, zoomX, zoomY);
            canvas.drawBitmap(bm, mRectSrc, mRectDst, mPaint);
        }
    }

    public Bitmap addOverlay(Bitmap map, float zX, float zY) {
    	
	    Bitmap bmOverlay = mm.getOverlay();
	    bmOverlay.eraseColor(0);
	    Canvas c = new Canvas(bmOverlay);
	    c.drawBitmap(map, new Matrix(), mPaint);
	    Bitmap bmMarker = mm.getMarkerBm();
	    
	    Iterator<MapNode> i = mm.getMarkers().iterator();
	    MapNode n1 = i.next();
	    while (i.hasNext()) {
	    	MapNode n2 = i.next();
	    	int x1px, y1px, x2px, y2px;
		    x1px = (int) (n1.texu * bmMap.getWidth());
		    y1px = (int) (n1.texv * bmMap.getHeight());
		    x2px = (int) (n2.texu * bmMap.getWidth());
		    y2px = (int) (n2.texv * bmMap.getHeight());
	    	c.drawLine(x1px, y1px, x2px, y2px, mPaint);
	    	n1 = n2;
	    }	    
	    
	    /*for (MapNode marker: mm.getMarkers()) {
		    Matrix mMat1 = new Matrix();
		    int xpx, ypx;
		    xpx = (int) (marker.texu * bmMap.getWidth());
		    ypx = (int) (marker.texv * bmMap.getHeight());
		    mMat1.setTranslate(xpx-bmMarker.getWidth()/2-28, ypx-bmMarker.getHeight());
		    mMat1.postScale(1/zX, 1/zY, xpx, ypx);
	    	c.drawBitmap(bmMarker, mMat1, mPaint);
	    }*/
	    return bmOverlay;
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
       mAspectQuotient.updateAspectQuotient(right - left, bottom - top, bmMap.getWidth(),bmMap.getHeight());
       mAspectQuotient.notifyObservers();
    }

    // implements Observer
    public void update(Observable observable, Object data) {
        invalidate();
    }

}
