//package com.teamunemployment.breadcrumbs.Camera;
//
//import android.content.Context;
//import android.graphics.SurfaceTexture;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.TextureView;
//
///**
// * @author Josiah Kendall.
// *
// * The custom view for our camera. Implements
// */
//public class CameraSurface extends TextureView implements TextureView.SurfaceTextureListener {
//
//    private CameraSurfaceContract surfaceContract;
//
//    public CameraSurface(Context context) {
//        super(context);
//    }
//
//    public void setSurfaceListener(CameraSurfaceContract surfaceContract) {
//        this.surfaceContract = surfaceContract;
//    }
//
//    @Override
//    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
//        this.surfaceContract.createCamera();
//    }
//
//    @Override
//    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
//
//    }
//
//    @Override
//    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//        return false;
//    }
//
//    @Override
//    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//
//    }
//}
