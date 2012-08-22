package com.airlocksoftware.holo.slideout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Debug;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.airlocksoftware.holo.R;
import com.airlocksoftware.holo.anim.AnimationParams;
import com.airlocksoftware.holo.anim.AnimationParams.Exclusivity;
import com.airlocksoftware.holo.anim.AnimationParams.FillType;
import com.airlocksoftware.holo.anim.OverlayManager;
import com.airlocksoftware.holo.anim.OverlayManager.AnimationFinishedListener;
import com.airlocksoftware.holo.interfaces.OnDestroyListener;
import com.airlocksoftware.holo.interfaces.OnStopListener;

/**
 * The frame the SlideoutView lives in. Consists of the ImageView that holds a screenshot of the
 * screen we're sliding over, and a FrameLayout to hold the content.
 **/
public class SlideoutFrame extends FrameLayout implements OnDestroyListener, OnStopListener {

	private FrameLayout mSlideoutContent;
//	private ImageView mScreenshotView;
	private FrameLayout mScreenshotView;

	private View mShadowView;
	GradientDrawable mShadowGradient;

	private Context mContext;
	Activity mActivity;

	OverlayManager mOverlayManager;
	private int mSlideWidth = 72; // default value
	private int mRootId;

	private Animation mInAnimation;
	private Animation mOutAnimation;

	private boolean mOpen;
	private boolean mIsAnimating;
	private int mDuration = 150; // default

	// CONSTANTS
	private static final int ID = R.id.slideout_frame;
	private static final int DEFAULT_LAYOUT = R.layout.slideout_frame_no_image;
	private static final String TAG = SlideoutFrame.class.getSimpleName();

	// CONSTRUCTOR
	public SlideoutFrame(Context context, Activity activity, OverlayManager overlay, int rootId) {
		super(context, null);
		mContext = context;
		mOverlayManager = overlay;
		mActivity = activity;
		mRootId = rootId;

		setId(ID);
		LayoutInflater inflater = LayoutInflater.from(mContext);
		inflater.inflate(DEFAULT_LAYOUT, this);
		mSlideoutContent = (FrameLayout) findViewById(R.id.slideout_content);
//		mScreenshotView = (ImageView) findViewById(R.id.screenshot);
		mScreenshotView = (FrameLayout) findViewById(R.id.screenshot);
		mScreenshotView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mIsAnimating) close();
			}
		});

		mOverlayManager.addView(this,
				new AnimationParams(FillType.FILL_SCREEN).exclusivity(Exclusivity.EXCLUDE_ALL));

		int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
		String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
		Log.d(TAG, usedMegsString);

	}

	// PUBLIC METHODS

	public void open() {
		mOpen = true;
		mIsAnimating = true;

		int screenWidth = getScreenWidth();
		int frameWidth = (2 * screenWidth) - mSlideWidth;

		LayoutParams params = (LayoutParams) this.getLayoutParams();
		params.width = frameWidth;
		params.gravity = Gravity.LEFT;
		this.setLayoutParams(params);

		// mBitmap = mScreenshot.snap();
		// SlideoutHelper.prepare(mActivity, mRootId);
		// mScreenshotView.setImageBitmap(SlideoutHelper.screenshot(mActivity, mRootId));
		// Drawable drawable = mContext.getResources().getDrawable(R.drawable.screenshot);
		// mScreenshotView.setImageDrawable(drawable);
//		SlideoutFrame.screenshot(mActivity, mRootId);
//		mScreenshotView.setImageBitmap(sBitmap);
//		mScreenshotView.setImageBitmap(this.loadBitmapFromView(mActivity.findViewById(mRootId)));
		int width = mActivity.findViewById(mRootId).getWidth();
		int height = mActivity.findViewById(mRootId).getHeight();
		RelativeLayout.LayoutParams imageParams = (android.widget.RelativeLayout.LayoutParams) mScreenshotView.getLayoutParams();
		imageParams.width = width;
		imageParams.height = height;
		mScreenshotView.setLayoutParams(imageParams);
//		mScreenshotView.setImageDrawable(null);

		mInAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE,
				-(screenWidth - mSlideWidth), TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE,
				0, TranslateAnimation.ABSOLUTE, 0);
		mInAnimation.setDuration(mDuration);
		mInAnimation.setInterpolator(mContext, android.R.anim.accelerate_interpolator);
		mInAnimation.setFillAfter(true);

		mOverlayManager.showViewById(ID, mInAnimation, new AnimationFinishedListener() {

			@Override
			public void onFinished(View overlay) {
				mIsAnimating = false;
			}
		});
	}

	public void close() {
		mOpen = false;
		mIsAnimating = true;

		int screenWidth = getScreenWidth();

		mOutAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0,
				TranslateAnimation.ABSOLUTE, -(screenWidth - mSlideWidth), TranslateAnimation.ABSOLUTE, 0,
				TranslateAnimation.ABSOLUTE, 0);
		mOutAnimation.setDuration(mDuration);
		mOutAnimation.setInterpolator(mContext, android.R.anim.accelerate_interpolator);

		mOverlayManager.hideViewById(ID, mOutAnimation, new AnimationFinishedListener() {
			@Override
			public void onFinished(View overlay) {
				mIsAnimating = false;
			}
		});
	}

	public void toggle() {
		if (isOpen()) close();
		else open();
	}

	/** Set the width of the Screenshot in pixels **/
	public SlideoutFrame slideWidth(int slideWidth) {
		mSlideWidth = slideWidth;
		return this;
	}

	public void showShadowEdge(int width) {
		mShadowView = new View(mContext, null);
		mShadowView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, 0));
	}

	public void hideShadowEdge() {

	}

	/** Get duration animation in millis **/
	public int duration() {
		return mDuration;
	}

	/** Set duration of animation in millis **/
	public SlideoutFrame duration(int duration) {
		mDuration = duration;
		return this;
	}

	public void setContentView(View child) {
		mSlideoutContent.addView(child);
	}

	public boolean isOpen() {
		return mOpen;
	}

	// PRIVATE METHODS

	@SuppressWarnings("deprecation")
	private int getScreenWidth() {
		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		return display.getWidth();
	}

	@Override
	public void onStop() {
//		if (sBitmap != null) {
//			sBitmap.recycle();
//			sBitmap = null;
//		}
		
//		if(mScreenshotView != null && mScreenshotView.getDrawable() != null) {
//			((BitmapDrawable) mScreenshotView.getDrawable()).getBitmap().recycle();
//			mScreenshotView.setImageBitmap(null);
//			mScreenshotView.setImageDrawable(null);
//		}
		
//		System.gc();
//		System.runFinalization();
//		System.gc();

		Log.d(TAG, "Stopped - Recycled Bitmap");
		int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
		String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
		Log.d(TAG, usedMegsString);
	}

	@Override
	public void onDestroy() {

	}

	// STATIC BITMAP SCREENSHOTTER
//	private static Bitmap sBitmap = null;

//	public static void screenshot(Activity activity, int rootId) {
//		// if (sBitmap != null) {
//		// sBitmap.recycle();
//		// }
//		Rect rectgle = new Rect();
//		Window window = activity.getWindow();
//		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
//		int statusBarHeight = rectgle.top;
//
//		Bitmap source = null;
//		try {
//			View view = activity.findViewById(rootId);
//			source = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
//			Canvas canvas = new Canvas(source);
//			view.draw(canvas);
//			sBitmap = source;
//			canvas = null;
//
//			// ViewGroup v1 = (ViewGroup) activity.findViewById(rootId).getRootView();
//			// v1.setDrawingCacheEnabled(true);
//			// source = Bitmap.createBitmap(v1.getDrawingCache());
//			// v1.setDrawingCacheEnabled(false);
//			// if (statusBarHeight != 0) {
//			// sBitmap = Bitmap.createBitmap(source, 0, statusBarHeight, source.getWidth(),
//			// source.getHeight() - statusBarHeight);
//			// source.recycle();
//			// } else {
//			// sBitmap = source;
//			// }
//
//			Log.d(TAG, "bitmap width=" + source.getWidth() + " and height=" + source.getHeight());
//		} catch (OutOfMemoryError e) {
//			Log.d(TAG, "Now we're out of memory");
//		}
//	}

	public static Bitmap loadBitmapFromView(View v) {
		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
		v.draw(c);
		c = null;
		return b;
	}

}
