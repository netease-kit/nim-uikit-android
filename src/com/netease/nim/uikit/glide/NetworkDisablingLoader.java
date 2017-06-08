package com.netease.nim.uikit.glide;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by huangjun on 2017/4/1.
 */
public class NetworkDisablingLoader implements StreamModelLoader<String> {
    @Override
    public DataFetcher<InputStream> getResourceFetcher(final String model, int width, int height) {
        return new DataFetcher<InputStream>() {
            @Override
            public InputStream loadData(Priority priority) throws Exception {
                throw new IOException("Forced Glide network failure");
            }

            @Override
            public void cleanup() {
            }

            @Override
            public String getId() {
                return model;
            }

            @Override
            public void cancel() {
            }
        };
    }
}
