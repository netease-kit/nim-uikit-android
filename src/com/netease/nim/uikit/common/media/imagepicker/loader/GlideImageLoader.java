package com.netease.nim.uikit.common.media.imagepicker.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

import java.io.File;


public class GlideImageLoader implements ImageLoader {

    @Override
    public void displayImage(Context context, String path, ImageView imageView, int width, int height) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.nim_placeholder_normal_impl)     //设置占位图片
                .error(R.drawable.nim_placeholder_normal_impl)           //设置错误图片
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transforms(new CenterCrop(), new RoundedCorners(ScreenUtil.dip2px(4)))
                .override(width, height)
                .dontAnimate();          //缓存全尺寸
        Glide.with(context)                             //配置上下文
             .asDrawable()
             .apply(options)
             .load(Uri.fromFile(new File(path)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
             .into(imageView);
    }

    @Override
    public void displayImage(Context context, String path, ImageView imageView, int width, int height, final GlideImageLoader.LoadListener listener) {
        RequestOptions options = new RequestOptions()
                .error(0)           //设置错误图片
                .placeholder(0)     //设置占位图片
                .diskCacheStrategy(DiskCacheStrategy.ALL)      //缓存全尺寸
                .disallowHardwareConfig();
        Glide.with(context)                             //配置上下文
             .asDrawable()
             .apply(options)
             .load(Uri.fromFile(new File(path)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
             .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (listener != null){
                            listener.onLoadSuccess();
                        }
                        return false;
                    }
                })
             .into(imageView);
    }

    @Override
    public void downloadImage(Context context, String path, final GlideImageLoader.LoadListener listener) {
        RequestOptions options = new RequestOptions()
                .error(0)           //设置错误图片
                .placeholder(0)     //设置占位图片
                .diskCacheStrategy(DiskCacheStrategy.ALL)      //缓存全尺寸
                .disallowHardwareConfig();
        Glide.with(context)                             //配置上下文
             .asFile()
             .apply(options)
             .load(Uri.fromFile(new File(path)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
             .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        if (listener != null) {
                            listener.onLoadSuccess();
                        }
                        return true;
                    }
                }).submit();
    }

    @Override
    public void clearRequest(View view) {
        Glide.with(view).clear(view);
    }

    @Override
    public void clearMemoryCache() {
        Glide.get(NimUIKit.getContext()).clearMemory();
    }

    public interface LoadListener {
        void onLoadSuccess();

        void onLoadFailed();
    }


    public static void displayAlbumThumb(ImageView imageView, String path, int placeHolder) {
        displayAlbum(imageView, path, createRounded(), placeHolder);
    }
    private static Transformation<Bitmap> createRounded() {
        return createRounded(4);
    }

    private static Transformation<Bitmap> createRounded(int dp) {
        return new RoundedCorners(ScreenUtil.dip2px(dp));
    }


    private static void displayAlbum(ImageView imageView, String path, Transformation<Bitmap> transformation,
                                     int placeHoder) {
        Context context = imageView.getContext();
        RequestOptions options = new RequestOptions().error(placeHoder).placeholder(placeHoder).diskCacheStrategy(
                DiskCacheStrategy.RESOURCE);

        if (transformation != null) {
            options = options.transforms(new CenterCrop(), transformation);
        } else {
            options = options.transform(new CenterCrop());
        }

        Glide.with(context).asDrawable().apply(options).load(Uri.fromFile(new File(path))).into(imageView);
    }

    public static void displayVideo(ImageView imageView, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Context context = imageView.getContext();
        RequestOptions options = new RequestOptions().error(R.drawable.nim_placeholder_video_impl).placeholder(
                R.drawable.nim_placeholder_video_impl).diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop();

        Glide.with(context).asDrawable().apply(options).load(url).into(imageView);
    }

    public static void displayVideo(ImageView imageView, Uri uri) {
        if (uri == null) {
            return;
        }
        displayAlbum(imageView, uri.getPath(), null, R.drawable.nim_placeholder_video_impl);
    }
}
