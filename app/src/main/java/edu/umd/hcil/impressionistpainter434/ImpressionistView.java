package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.Random;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;

    Toast notLoaded = Toast.makeText(getContext(), "Please load an image.", Toast.LENGTH_SHORT);

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //TODO
        _offScreenCanvas.drawColor(Color.WHITE, PorterDuff.Mode.ADD);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            Log.i("OnDraw", "Drew pixel");
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        } else {
            Log.i("OnDraw", "Specified pixel does not exist.");
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        int currX = (int)motionEvent.getX();
        int currY = (int)motionEvent.getY();
        Bitmap imageViewBitmap = _imageView.getDrawingCache();

        try {
            int colorAtTouchPixelInImage  = imageViewBitmap.getPixel(currX, currY);
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    _paint.setColor(colorAtTouchPixelInImage);
                    draw(currX, currY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("OnTouch", "Touched down");
                    if (_imageView.getDrawable() == null) {
                        if (notLoaded.getView().isShown()) {
                            return false;
                        } else {
                            notLoaded.show();
                            return false;
                        }
                    } else {
                        Log.i("OnTouch", "Image is loaded, proceed to draw impressionist");
                        for (int idx = 0; idx < motionEvent.getHistorySize(); idx++) {
                            float oldX = motionEvent.getHistoricalX(idx);
                            float oldY = motionEvent.getHistoricalY(idx);

                            int oldPixel = imageViewBitmap.getPixel((int)oldX, (int)oldY);
                            _paint.setColor(colorAtTouchPixelInImage);

                            draw(currX, currY);
//                            Log.i("OnTouch", "Set Pixel color " + colorAtTouchPixelInImage);

                        }

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    Log.i("Default motion event", motionEvent.getAction() + "");
                    break;
            }
        } catch (Exception e) {
            Log.i("OnTouch", e.getLocalizedMessage());
        }


        invalidate();

        return true;
    }

    private void draw(float currX, float currY) {
        switch(_brushType) {
            case Square:
                float left = currX - _defaultRadius;
                float top = currY - _defaultRadius;
                float right = currX + _defaultRadius;
                float bottom = currY + _defaultRadius;
                _offScreenCanvas.drawRect(left, top, right, bottom, _paint);
                break;
            case Circle:
                _offScreenCanvas.drawCircle(currX, currY, _defaultRadius, _paint);
                break;
            case Line:
                _offScreenCanvas.drawLine(currX, currY, currX + _defaultRadius, currY + _defaultRadius, _paint);
                break;
            case LineSplatter:
                break;
            case CircleSplatter:
                break;
            default:
                _offScreenCanvas.drawCircle(currX, currY, _defaultRadius, _paint);
                break;

        }
    }


    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}

