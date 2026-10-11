/*
 * SPDX-FileCopyrightText: 2026 NewPipe HLS contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.schabi.newpipe.player;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.player.resolver.VideoPlaybackResolver.SourceType;

import java.util.List;

public class PlaybackVisibilityTest {
    private static StreamInfo video(final boolean separateAudio) {
        final StreamInfo info = mock(StreamInfo.class);
        when(info.getStreamType()).thenReturn(StreamType.VIDEO_STREAM);
        when(info.getAudioStreams()).thenReturn(
                separateAudio ? List.of(mock(AudioStream.class)) : List.of());
        return info;
    }

    @Test
    public void rotationDoesNotReloadAnAlreadyVisibleCombinedSource() {
        assertFalse(Player.playQueueManagerReloadingNeeded(false, false,
                SourceType.VIDEO_WITH_AUDIO_OR_AUDIO_ONLY, video(true), 0));
    }

    @Test
    public void repeatedResumeBeforeTracksAreMappedDoesNotReload() {
        assertFalse(Player.playQueueManagerReloadingNeeded(false, false,
                SourceType.VIDEO_WITH_AUDIO_OR_AUDIO_ONLY, video(true),
                Player.RENDERER_UNAVAILABLE));
    }

    @Test
    public void hidingHlsKeepsItsAudioEvenWhenDirectAudioIsAvailable() {
        assertFalse(Player.playQueueManagerReloadingNeeded(true, true,
                SourceType.VIDEO_WITH_AUDIO_OR_AUDIO_ONLY, video(true), 0));
    }

    @Test
    public void restoringHlsDoesNotReloadWhenVideoRendererWasDisabled() {
        assertFalse(Player.playQueueManagerReloadingNeeded(true, true,
                SourceType.VIDEO_WITH_AUDIO_OR_AUDIO_ONLY, video(true),
                Player.RENDERER_UNAVAILABLE));
    }

    @Test
    public void directCombinedSourceStillSwitchesToAvailableAudio() {
        assertTrue(Player.playQueueManagerReloadingNeeded(true, false,
                SourceType.VIDEO_WITH_AUDIO_OR_AUDIO_ONLY, video(true), 0));
    }

    @Test
    public void separatedSourcesStillKeepTheirSharedTimeline() {
        assertFalse(Player.playQueueManagerReloadingNeeded(true, false,
                SourceType.VIDEO_WITH_SEPARATED_AUDIO, video(true), 0));
    }

    @Test
    public void combinedSourceWithoutSeparateAudioIsRetained() {
        assertFalse(Player.playQueueManagerReloadingNeeded(true, false,
                SourceType.VIDEO_WITH_AUDIO_OR_AUDIO_ONLY, video(false), 0));
    }
}
