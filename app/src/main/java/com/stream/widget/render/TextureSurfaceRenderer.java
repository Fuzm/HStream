package com.stream.widget.render;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLUtils;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by Fuzm on 2017/4/16 0016.
 */

public abstract class TextureSurfaceRenderer implements Runnable {

    private static final String TAG = TextureSurfaceRenderer.class.getSimpleName();

    protected SurfaceTexture mTexture;
    protected int mWidth;
    protected int mHeight;

    private boolean mRunning;

    private EGL10 egl;
    private EGLContext eglContext;
    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;

    public TextureSurfaceRenderer(SurfaceTexture surfaceTexture, int width, int height) {
        mTexture = surfaceTexture;

        mWidth = width;
        mHeight = height;
        mRunning = true;

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        initEGL();
        initGLComponents();
        Log.d(TAG, "OpenGL init OK. start draw...");
        while (mRunning) {
            if (draw()) {
                egl.eglSwapBuffers(eglDisplay, eglSurface);
            }
        }
        deinitGLComponents();
        deinitEGL();
    }

    /**
     * 初始化EGL环境
     * 1.获取display    2.初始画egl
     * 3.选择config     4.构造surface
     * 5.创建context    5.进行绘制
     */
    private void initEGL() {
        egl = (EGL10) EGLContext.getEGL();
        //获取显示设备
        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        //version中存放EGL 版本号，int[0]为主版本号，int[1]为子版本号
        int version[] = new int[2];
        egl.eglInitialize(eglDisplay, version);

        EGLConfig eglConfig = chooseEglConfig();
        //创建EGL 的window surface 并且返回它的handles(eslSurface)
        eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, mTexture, null);

        eglContext = createContext(egl, eglDisplay, eglConfig);

        /**绑定context到当前渲染线程并且去绘制（通过opengl去绘制）和读取surface（通过eglSwapBuffers（EGLDisplay dpy, EGLContext ctx）来显示）*/
        try {
            if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
                throw new RuntimeException("GL error:" + GLUtils.getEGLErrorString(egl.eglGetError()));
            }
            if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                throw new RuntimeException("GL Make current Error" + GLUtils.getEGLErrorString(egl.eglGetError()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void deinitEGL() {
        egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(eglDisplay, eglSurface);
        egl.eglDestroyContext(eglDisplay, eglContext);
        egl.eglTerminate(eglDisplay);
        Log.d(TAG, "OpenGL deinit OK.");
    }

    /**
     * 主要的绘制函数， 需在子类中去实现绘制
     */
    protected abstract boolean draw();

    /***
     * 抽象函数，初始化opengl绘制所必需的一些组件比如vertextBuffer,sharders,textures等，
     * 通常在Opengl context 初始化以后被调用，需要子类去实现
     */
    protected abstract void initGLComponents();
    /***
     * 于init操作相反，做一些释放资源，停止绘制工作
     */
    protected abstract void deinitGLComponents();
    /***
     * 获取生产的 SurfaceTexture,在子类中实现
     */
    public abstract SurfaceTexture getSurfaceTexture();

    /**
     * 为当前渲染的API创建一个渲染上下文
     * @return a handle to the context
     */
    private EGLContext createContext(EGL10 egl, EGLDisplay eglDisplay, EGLConfig eglConfig) {
        int[] attrs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };
        return egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrs);
    }

    /***
     *  选择一个你所期望的配置.
     * @return 一个与你所指定最相近的一个EGL 帧缓存配置.
     */
    private EGLConfig chooseEglConfig() {
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] attributes = getAttributes();
        int confSize = 1;

        if (!egl.eglChooseConfig(eglDisplay, attributes, configs, confSize, configsCount)) {    //获取满足attributes的config个数
            throw new IllegalArgumentException("Failed to choose config:"+ GLUtils.getEGLErrorString(egl.eglGetError()));
        }
        else if (configsCount[0] > 0) {
            return configs[0];
        }

        return null;
    }

    /**
     * 构造你期望的绘制时所需要的特性配置,如ARGB,DEPTH...
     */
    private int[] getAttributes()
    {
        return new int[] {
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,  //指定渲染api类别
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE      //总是以EGL10.EGL_NONE结尾
        };
    }

    /**
     * 当 当前activity停止时调用，用来停止渲染线程，并释放资源.
     */
    public void onPause()
    {
        mRunning = false;
    }

    @Override
    protected  void finalize() throws Throwable {
        super.finalize();
        mRunning = false;
    }
}
