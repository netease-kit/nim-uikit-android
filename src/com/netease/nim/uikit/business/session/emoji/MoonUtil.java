package com.netease.nim.uikit.business.session.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.netease.nim.uikit.business.recent.TeamMemberAitHelper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoonUtil {
    private static final float DEF_SCALE = 0.6f;
    private static final float SMALL_SCALE = 0.45F;

    public static void identifyFaceExpression(Context context,
                                              View textView, String value, int align) {
        identifyFaceExpression(context, textView, value, align, DEF_SCALE);
    }

    public static void identifyFaceExpressionAndATags(Context context,
                                                      View textView, String value, int align) {
        SpannableString mSpannableString = makeSpannableStringTags(context, value, DEF_SCALE, align);
        viewSetText(textView, mSpannableString);
    }

    /**
     * 具体类型的view设置内容
     *
     * @param textView
     * @param mSpannableString
     */
    private static void viewSetText(View textView, SpannableString mSpannableString) {
        if (textView instanceof TextView) {
            TextView tv = (TextView) textView;
            tv.setText(mSpannableString);
        } else if (textView instanceof EditText) {
            EditText et = (EditText) textView;
            et.setText(mSpannableString);
        }
    }

    public static void identifyFaceExpression(Context context,
                                              View textView, String value, int align, float scale) {
        SpannableString mSpannableString = replaceEmoticons(context, value, scale, align);
        viewSetText(textView, mSpannableString);
    }

    public static void identifyRecentVHFaceExpressionAndTags(Context context, View textView,
                                                             String value, int align, float scale) {
        SpannableString mSpannableString = makeSpannableStringTags(context, value, scale, align, false);
        TeamMemberAitHelper.replaceAitForeground(value, mSpannableString);
        viewSetText(textView, mSpannableString);
    }

    /**
     * lstmsgviewholder类使用,只需显示a标签对应的文本
     */
    public static void identifyFaceExpressionAndTags(Context context,
                                                     View textView, String value, int align, float scale) {
        SpannableString mSpannableString = makeSpannableStringTags(context, value, scale, align, false);
        viewSetText(textView, mSpannableString);
    }

    private static SpannableString replaceEmoticons(Context context, String value, float scale, int align) {
        if (TextUtils.isEmpty(value)) {
            value = "";
        }

        SpannableString mSpannableString = new SpannableString(value);
        Matcher matcher = EmojiManager.getPattern().matcher(value);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String emot = value.substring(start, end);
            Drawable d = getEmotDrawable(context, emot, scale);
            if (d != null) {
                ImageSpan span = new ImageSpan(d, align);
                mSpannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return mSpannableString;
    }

    private static Pattern mATagPattern = Pattern.compile("<a.*?>.*?</a>");

    public static SpannableString makeSpannableStringTags(Context context, String value, float scale, int align) {
        return makeSpannableStringTags(context, value, DEF_SCALE, align, true);
    }

    public static SpannableString makeSpannableStringTags(Context context, String value, float scale, int align, boolean bTagClickable) {
        ArrayList<ATagSpan> tagSpans = new ArrayList<ATagSpan>();
        if (TextUtils.isEmpty(value)) {
            value = "";
        }
        //a标签需要替换原始文本,放在moonutil类中
        Matcher aTagMatcher = mATagPattern.matcher(value);

        int start = 0;
        int end = 0;
        while (aTagMatcher.find()) {
            start = aTagMatcher.start();
            end = aTagMatcher.end();
            String atagString = value.substring(start, end);
            ATagSpan tagSpan = getTagSpan(atagString);
            value = value.substring(0, start) + tagSpan.getTag() + value.substring(end);
            tagSpan.setRange(start, start + tagSpan.getTag().length());
            tagSpans.add(tagSpan);
            aTagMatcher = mATagPattern.matcher(value);
        }


        SpannableString mSpannableString = new SpannableString(value);
        Matcher matcher = EmojiManager.getPattern().matcher(value);
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            String emot = value.substring(start, end);
            Drawable d = getEmotDrawable(context, emot, scale);
            if (d != null) {
                ImageSpan span = align == -1 ? new ImageSpanAlignCenter(d) : new ImageSpan(d, align);
                mSpannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        if (bTagClickable) {
            for (ATagSpan tagSpan : tagSpans) {
                mSpannableString.setSpan(tagSpan, tagSpan.start, tagSpan.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return mSpannableString;
    }

    public static void replaceEmoticons(Context context, Editable editable, int start, int count) {
        if (count <= 0 || editable.length() < start + count)
            return;

        CharSequence s = editable.subSequence(start, start + count);
        Matcher matcher = EmojiManager.getPattern().matcher(s);
        while (matcher.find()) {
            int from = start + matcher.start();
            int to = start + matcher.end();
            String emot = editable.subSequence(from, to).toString();
            Drawable d = getEmotDrawable(context, emot, SMALL_SCALE);
            if (d != null) {
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                editable.setSpan(span, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static Drawable getEmotDrawable(Context context, String text, float scale) {
        Drawable drawable = EmojiManager.getDrawable(context, text);

        // scale
        if (drawable != null) {
            int width = (int) (drawable.getIntrinsicWidth() * scale);
            int height = (int) (drawable.getIntrinsicHeight() * scale);
            drawable.setBounds(0, 0, width, height);
        }

        return drawable;
    }

    private static ATagSpan getTagSpan(String text) {
        String href = null;
        String tag = null;
        if (text.toLowerCase().contains("href")) {
            int start = text.indexOf("\"");
            int end = text.indexOf("\"", start + 1);
            if (end > start)
                href = text.substring(start + 1, end);
        }
        int start = text.indexOf(">");
        int end = text.indexOf("<", start);
        if (end > start)
            tag = text.substring(start + 1, end);
        return new ATagSpan(tag, href);

    }

    private static class ATagSpan extends ClickableSpan {
        private int start;
        private int end;
        private String mUrl;
        private String tag;

        ATagSpan(String tag, String url) {
            this.tag = tag;
            this.mUrl = url;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(true);
        }

        public String getTag() {
            return tag;
        }

        public void setRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void onClick(View widget) {
            try {
                if (TextUtils.isEmpty(mUrl))
                    return;
                Uri uri = Uri.parse(mUrl);
                String scheme = uri.getScheme();
                if (TextUtils.isEmpty(scheme)) {
                    mUrl = "http://" + mUrl;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
