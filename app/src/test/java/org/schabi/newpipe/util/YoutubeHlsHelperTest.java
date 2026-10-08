/*
 * SPDX-FileCopyrightText: 2026 NewPipe HLS contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.schabi.newpipe.util;

import android.net.Uri;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.hls.playlist.HlsMultivariantPlaylist;

import org.junit.Test;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class YoutubeHlsHelperTest {
    private static HlsMultivariantPlaylist master() {
        final Format avc = new Format.Builder().setHeight(720).setFrameRate(60)
                .setCodecs("avc1.640020,mp4a.40.2").build();
        final Format av1 = new Format.Builder().setHeight(1080)
                .setCodecs("av01.0.08M.08,opus").build();
        final Format audio = new Format.Builder().setCodecs("mp4a.40.2").build();
        return new HlsMultivariantPlaylist("https://example.org/master.m3u8", List.of(),
                List.of(new HlsMultivariantPlaylist.Variant(mock(Uri.class), avc,
                                null, "audio", null, null),
                        new HlsMultivariantPlaylist.Variant(mock(Uri.class), av1,
                                null, "opus", null, null)),
                List.of(), List.of(new HlsMultivariantPlaylist.Rendition(mock(Uri.class), audio,
                                "audio", "English"),
                        new HlsMultivariantPlaylist.Rendition(mock(Uri.class),
                                new Format.Builder().setCodecs("opus").build(), "opus", "English")),
                List.of(), List.of(), null, null,
                false, Map.of(), List.of());
    }

    @Test
    public void exposesSupportedQualitiesUsingMasterToPreserveAudio() {
        final List<VideoStream> streams = YoutubeHlsHelper.videoStreams(master(),
                "https://example.org/master.m3u8");
        assertEquals(1, streams.size());
        final VideoStream stream = streams.get(0);
        assertEquals("720p60", stream.getResolution());
        assertEquals("https://example.org/master.m3u8", stream.getContent());
        assertEquals(DeliveryMethod.HLS, stream.getDeliveryMethod());
        assertFalse(stream.isVideoOnly());
        assertTrue(YoutubeHlsHelper.isWorkaroundStream(stream));
    }

    @Test
    public void selectsOneVideoVariantAndKeepsExternalAudio() throws IOException {
        final HlsMultivariantPlaylist selected = YoutubeHlsHelper.selectVariant(master(), 0);
        assertEquals(1, selected.variants.size());
        assertEquals(720, selected.variants.get(0).format.height);
        assertEquals(1, selected.audios.size());
        assertEquals("audio", selected.audios.get(0).groupId);
    }

    @Test(expected = IOException.class)
    public void rejectsUnavailableVariantInsteadOfSilentlySwitchingQuality() throws IOException {
        YoutubeHlsHelper.selectVariant(master(), 2);
    }
}
