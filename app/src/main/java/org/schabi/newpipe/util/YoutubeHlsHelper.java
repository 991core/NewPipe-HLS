/*
 * SPDX-FileCopyrightText: 2026 NewPipe HLS contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.schabi.newpipe.util;

import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsMultivariantPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParserFactory;
import com.google.android.exoplayer2.upstream.ParsingLoadable;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** HLS VOD workaround: retain the master playlist so external audio is not lost. */
public final class YoutubeHlsHelper {
    private static final String ID_PREFIX = "youtube-hls-";

    private YoutubeHlsHelper() {
    }

    /**
     * Called inside the extraction Single, before StreamInfo enters the cache.
     * @param info extracted stream information
     * @return information with HLS video variants added
     */
    public static StreamInfo prepare(final StreamInfo info) throws IOException, ReCaptchaException {
        if (info.getServiceId() != ServiceList.YouTube.getServiceId()
                || info.getStreamType() != StreamType.VIDEO_STREAM
                || info.getHlsUrl().isEmpty()) {
            return info;
        }
        final String url = info.getHlsUrl();
        final String body = NewPipe.getDownloader().get(url, Collections.singletonMap(
                "User-Agent", List.of(YoutubeParsingHelper.getVisionOsUserAgent(null))))
                .responseBody();
        final HlsPlaylist parsed = new HlsPlaylistParser().parse(Uri.parse(url),
                new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        if (!(parsed instanceof HlsMultivariantPlaylist)) {
            throw new IOException("YouTube HLS master playlist expected");
        }
        final List<VideoStream> streams = videoStreams((HlsMultivariantPlaylist) parsed, url);
        if (streams.isEmpty()) {
            throw new IOException("No supported AVC/AAC YouTube HLS variants");
        }
        // Preserve direct formats for downloads and background audio. Video playback selects HLS.
        final int variantCount = streams.size();
        streams.addAll(info.getVideoStreams());
        info.setVideoStreams(streams);
        Log.i("NewTubeHls", "prepared video=" + info.getId()
                + " variants=" + variantCount);
        return info;
    }

    static List<VideoStream> videoStreams(final HlsMultivariantPlaylist master, final String url) {
        final List<VideoStream> streams = new ArrayList<>();
        for (int index = 0; index < master.variants.size(); index++) {
            final Format format = master.variants.get(index).format;
            // TS AVC/AAC is supported by the existing player; skip AV1/HEVC/Opus variants.
            if (format.height <= 0 || format.codecs == null
                    || !format.codecs.contains("avc1") || !format.codecs.contains("mp4a")) {
                continue;
            }
            final String resolution = format.height + "p"
                    + (format.frameRate > 30 ? Math.round(format.frameRate) : "");
            streams.add(new VideoStream.Builder()
                    .setId(ID_PREFIX + index)
                    .setContent(url, true)
                    .setManifestUrl(url)
                    .setMediaFormat(MediaFormat.MPEG_4)
                    .setDeliveryMethod(DeliveryMethod.HLS)
                    .setResolution(resolution)
                    .setIsVideoOnly(false)
                    .build());
        }
        return streams;
    }

    public static boolean isWorkaroundStream(final VideoStream stream) {
        return stream.getDeliveryMethod() == DeliveryMethod.HLS
                && stream.getId().startsWith(ID_PREFIX);
    }

    public static HlsPlaylistParserFactory parserFactory(final VideoStream stream) {
        final int variantIndex = Integer.parseInt(stream.getId().substring(ID_PREFIX.length()));
        final HlsPlaylistParserFactory delegate = new DefaultHlsPlaylistParserFactory();
        return new HlsPlaylistParserFactory() {
            @Override
            public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser() {
                final ParsingLoadable.Parser<HlsPlaylist> parser = delegate.createPlaylistParser();
                return (uri, input) -> {
                    final HlsPlaylist playlist = parser.parse(uri, input);
                    if (!(playlist instanceof HlsMultivariantPlaylist)) {
                        throw new IOException("YouTube HLS master playlist expected");
                    }
                    return selectVariant((HlsMultivariantPlaylist) playlist, variantIndex);
                };
            }

            @Override
            public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser(
                    final HlsMultivariantPlaylist master, final HlsMediaPlaylist previous) {
                return delegate.createPlaylistParser(master, previous);
            }
        };
    }

    static HlsMultivariantPlaylist selectVariant(final HlsMultivariantPlaylist master,
                                                 final int index) throws IOException {
        if (index < 0 || index >= master.variants.size()) {
            throw new IOException("Selected YouTube HLS variant is unavailable");
        }
        final List<StreamKey> keys = new ArrayList<>();
        keys.add(new StreamKey(HlsMultivariantPlaylist.GROUP_INDEX_VARIANT, index));
        final HlsMultivariantPlaylist.Variant variant = master.variants.get(index);
        for (int i = 0; i < master.audios.size(); i++) {
            if (Objects.equals(variant.audioGroupId, master.audios.get(i).groupId)) {
                keys.add(new StreamKey(HlsMultivariantPlaylist.GROUP_INDEX_AUDIO, i));
            }
        }
        for (int i = 0; i < master.subtitles.size(); i++) {
            if (Objects.equals(variant.subtitleGroupId, master.subtitles.get(i).groupId)) {
                keys.add(new StreamKey(HlsMultivariantPlaylist.GROUP_INDEX_SUBTITLE, i));
            }
        }
        return master.copy(keys);
    }
}
