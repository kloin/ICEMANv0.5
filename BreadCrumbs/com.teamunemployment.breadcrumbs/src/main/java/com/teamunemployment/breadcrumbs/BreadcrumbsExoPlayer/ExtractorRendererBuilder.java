package com.teamunemployment.breadcrumbs.BreadcrumbsExoPlayer;

/**
 * Created by jek40 on 1/05/2016.
 */
import com.danikula.videocache.HttpProxyCacheServer;
import com.devbrackets.android.exomedia.util.MediaUtil;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;

import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.text.TextTrackRenderer;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.upstream.cache.Cache;
import com.google.android.exoplayer.upstream.cache.CacheDataSource;
import com.google.android.exoplayer.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer.upstream.cache.SimpleCache;
import com.teamunemployment.breadcrumbs.caching.Mp4ProxyCache;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;

/**
 * A {@link BreadcrumbsExoPlayer.RendererBuilder} for streams that can be read using an {@link Extractor}.
 */
public class ExtractorRendererBuilder implements BreadcrumbsExoPlayer.RendererBuilder {

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

    private final Context context;
    private final String userAgent;
    private final Uri uri;
    private final boolean isLocal;

    public ExtractorRendererBuilder(Context context, String userAgent, Uri uri, boolean isLocal) {
        this.context = context;
        this.userAgent = userAgent;
        this.uri = uri;
        this.isLocal = isLocal;
    }

    @Override
    public void buildRenderers(BreadcrumbsExoPlayer player) {
        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        Handler mainHandler = player.getMainHandler();

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mainHandler, null);
        DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);

        // If its not a local datasource, we use the proxy cache. If it is local, just load from the local uri
        Uri proxyUri;
        if (!isLocal) {
            HttpProxyCacheServer proxyCacheServer = Mp4ProxyCache.GetProxy(context);
            String proxyUrl = proxyCacheServer.getProxyUrl(uri.toString());
            proxyUri= Uri.parse(proxyUrl);
        } else {
            proxyUri = uri;
        }

        // This caching only works for in memory cahcing of DASH Streaming - this is not what we want.
        //Cache cache = new SimpleCache(context.getCacheDir(), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 10));
        //CacheDataSource cacheDataSource = new CacheDataSource(cache, dataSource, false, false);

        // Build our extractor to get the source.
        BreadcrumbsExtractorSampleSource sampleSource = new BreadcrumbsExtractorSampleSource(proxyUri, dataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE, mainHandler, player, 0);
        sampleSource.prepare(0);
        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context,
                sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
                mainHandler, player, 50);

        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
                MediaCodecSelector.DEFAULT, null, true, mainHandler, player,
                AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);

        TrackRenderer textRenderer = new TextTrackRenderer(sampleSource, player, mainHandler.getLooper());

        // Invoke the callback.
        TrackRenderer[] renderers = new TrackRenderer[BreadcrumbsExoPlayer.RENDERER_COUNT];
        renderers[BreadcrumbsExoPlayer.TYPE_VIDEO] = videoRenderer;
        renderers[BreadcrumbsExoPlayer.TYPE_AUDIO] = audioRenderer;
        renderers[BreadcrumbsExoPlayer.TYPE_TEXT] = textRenderer;
        player.onRenderers(renderers, bandwidthMeter);
    }

    @Override
    public void cancel() {
        // Do nothing.
    }

}
