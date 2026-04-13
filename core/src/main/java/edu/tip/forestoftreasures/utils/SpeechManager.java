package edu.tip.forestoftreasures.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import com.badlogic.gdx.Gdx;

import edu.tip.forestoftreasures.Model.SettingsConfiguration;

import com.k2fsa.sherpa.onnx.OfflineTts;
import com.k2fsa.sherpa.onnx.OfflineTtsCallback;
import com.k2fsa.sherpa.onnx.OfflineTtsConfig;
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig;
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig;

/**
 * Streaming-first Text-to-Speech engine powered by Sherpa-ONNX with Piper VITS models.
 *
 * <p>Architecture summary:
 * <ul>
 *   <li>A single dedicated worker thread ("Speech-Worker") drains a lock-free
 *       {@link ConcurrentLinkedQueue} of {@link SpeechRequest} records.</li>
 *   <li>Synthesis uses {@code OfflineTts.generateWithCallback} so PCM audio
 *       chunks stream directly to a {@link SourceDataLine} — zero disk I/O,
 *       playback begins on the first generated phoneme.</li>
 *   <li>An LRU cache ({@code Map<Integer, byte[]>}) stores recently-spoken
 *       sentences for instant replay (0ms latency on cache hit).</li>
 *   <li>A {@link WordCallback} allows callers to sync UI typing effects
 *       with audio word boundaries.</li>
 * </ul>
 *
 * <p>This class is lifecycle-managed by {@code GameLauncher} — it is NOT a
 * static singleton. Call {@link #dispose()} when the game shuts down to
 * release ONNX native resources.</p>
 */
public class SpeechManager {
    private static final String TAG = "SpeechManager";

    /** Maximum number of cached speech buffers before the oldest is evicted. */
    private static final int MAX_CACHE_ENTRIES = 32;

    /** Default speech speed factor (1.0 = normal). 0.85f provides natural pacing (no rapping). */
    private static final float DEFAULT_SPEED = 0.85f;

    /** Default speaker ID for single-speaker models. */
    private static final int DEFAULT_SPEAKER_ID = 0;

    /**
     * Regex to strip TextraTypist formatting tokens before feeding text to TTS.
     * Matches curly-brace tokens like {COLOR=#FFDB51}, {SHAKE}, {WAVE}, {WAIT=0.5}
     * and square-bracket tokens like [{RESET}], [%150], [#FFDB51], [WHITE], etc.
     */
    private static final Pattern TEXTRA_TOKEN_PATTERN = Pattern.compile(
        "\\{[^}]*}|\\[[^\\]]*]"
    );

    // -------------------------------------------------------------------------
    // FUNCTIONAL INTERFACES
    // -------------------------------------------------------------------------

    /**
     * Callback fired when a word boundary is estimated during streaming synthesis.
     * The offsets refer to character positions in the cleaned (token-stripped) text.
     */
    @FunctionalInterface
    public interface WordCallback {
        void onWordBoundary(int charOffset, int charLength);
    }

    // -------------------------------------------------------------------------
    // SPEECH REQUEST RECORD
    // -------------------------------------------------------------------------

    /**
     * Immutable request submitted to the worker thread.
     *
     * @param text      Clean, token-stripped text to synthesize.
     * @param interrupt If true, abort the current sentence and start this one.
     */
    private record SpeechRequest(String text, boolean interrupt) {}

    // -------------------------------------------------------------------------
    // FIELDS
    // -------------------------------------------------------------------------

    private final SettingsConfiguration settingsConfig;
    private final String modelBasePath;

    // Worker thread infrastructure
    private final ConcurrentLinkedQueue<SpeechRequest> requestQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean interrupted = new AtomicBoolean(false);
    private Thread workerThread;

    // Sherpa-ONNX engine (initialized lazily on the worker thread)
    private OfflineTts tts;
    private int sampleRate;
    private final AtomicBoolean engineInitialized = new AtomicBoolean(false);
    private final AtomicBoolean engineFailed = new AtomicBoolean(false);

    // Audio output line (opened once engine is initialized)
    private SourceDataLine audioLine;

    // LRU cache: insertion-ordered map that evicts the eldest entry on overflow
    private final Map<Integer, byte[]> audioCache;

    // Optional word-boundary callback
    private volatile WordCallback wordCallback;

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------

    /**
     * Creates a new SpeechManager and starts the background worker thread.
     *
     * @param settingsConfig Game settings (controls Read Aloud toggle).
     * @param modelBasePath  Relative path inside assets to the model directory,
     *                       e.g. {@code "tts-models/vits-piper-en_US-amy-low"}.
     */
    public SpeechManager(SettingsConfiguration settingsConfig, String modelBasePath) {
        this.settingsConfig = settingsConfig;
        this.modelBasePath = modelBasePath;

        this.audioCache = new LinkedHashMap<>(MAX_CACHE_ENTRIES, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, byte[]> eldest) {
                return size() > MAX_CACHE_ENTRIES;
            }
        };

        workerThread = new Thread(this::workerLoop, "Speech-Worker");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    // -------------------------------------------------------------------------
    // PUBLIC API
    // -------------------------------------------------------------------------

    /**
     * Speaks the given text asynchronously. If the "Read Aloud" setting is
     * disabled, this method is a no-op.
     *
     * @param text      Dialogue text, potentially containing TextraTypist tokens.
     * @param interrupt If true, stops current speech and starts this text
     *                  immediately. If false, queues after any pending speech.
     */
    public void speak(String text, boolean interrupt) {
        if (text == null || text.isBlank()) return;
        if (settingsConfig == null || !settingsConfig.getGameSettings().isReadAloudEnabled()) {
            Gdx.app.log(TAG, "Speech skipped: 'Read Aloud' is disabled in settings.");
            return;
        }

        String cleanText = stripFormatting(text);
        if (cleanText.isBlank()) return;

        // Split into sentences for pipelined streaming
        String[] sentences = cleanText.split("(?<=[.!?])\\s+");

        if (interrupt) {
            interrupted.set(true);
            requestQueue.clear();
        }

        for (String sentence : sentences) {
            if (!sentence.isBlank()) {
                requestQueue.offer(new SpeechRequest(sentence.trim(), interrupt));
                // Only the first sentence of an interrupt batch carries the flag
                interrupt = false;
            }
        }
    }

    /**
     * Convenience method that narrates text and interrupts current speech.
     * This ensures dialogue doesn't queue up when the player skips text.
     *
     * @param rawText Dialogue text, potentially containing TextraTypist tokens.
     */
    public void say(String rawText) {
        speak(rawText, true);
    }

    /**
     * Stops any currently playing or queued speech immediately.
     */
    public void stop() {
        requestQueue.clear();
        interrupted.set(true);
        flushAudioLine();
    }

    /**
     * Registers a callback that fires when a word boundary is estimated
     * during streaming synthesis. The callback is dispatched on the libGDX
     * render thread via {@code Gdx.app.postRunnable()}.
     *
     * @param callback The word-boundary listener, or null to clear.
     */
    public void setWordCallback(WordCallback callback) {
        this.wordCallback = callback;
    }

    /**
     * Synchronizes the engine's output volume with the latest game settings.
     * Called by the controller when the ambience volume slider is moved.
     */
    public void updateVolume() {
        if (settingsConfig != null) {
            float volume = settingsConfig.getGameSettings().ambienceVolume();
            applyVolume(volume);
        }
    }

    /**
     * Releases the Sherpa-ONNX engine, shuts down the worker thread, closes
     * the audio line, and clears the cache. Must be called from
     * {@code GameLauncher.dispose()}.
     */
    public void dispose() {
        running.set(false);
        interrupted.set(true);
        requestQueue.clear();

        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        closeAudioLine();
        releaseEngine();

        synchronized (audioCache) {
            audioCache.clear();
        }

        Gdx.app.log(TAG, "Disposed.");
    }

    // -------------------------------------------------------------------------
    // WORKER THREAD
    // -------------------------------------------------------------------------

    /**
     * Event loop running on the dedicated Speech-Worker thread.
     * Drains the request queue and processes each speech request sequentially.
     */
    private void workerLoop() {
        // Pre-initialize engine on startup to eliminate cold-start latency
        initializeEngineIfNeeded();

        while (running.get()) {
            SpeechRequest request = requestQueue.poll();

            if (request == null) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    if (!running.get()) break;
                }
                continue;
            }

            try {
                processRequest(request);
            } catch (Exception e) {
                Gdx.app.error(TAG, "Failed to process speech request: \""
                    + truncate(request.text()) + "\"", e);
            }
        }
    }

    /**
     * Processes a single speech request: checks cache, synthesizes if needed,
     * and streams audio to the output line.
     */
    private void processRequest(SpeechRequest request) {
        if (!engineInitialized.get() || engineFailed.get()) return;

        // Check if TTS was disabled mid-stream
        if (!settingsConfig.getGameSettings().isReadAloudEnabled()) {
            requestQueue.clear();
            interrupted.set(true);
            return;
        }

        // Handle interrupt: flush audio line
        if (request.interrupt()) {
            flushAudioLine();
        }

        // Clear any stale interrupted flag so this request can proceed
        interrupted.set(false);

        String text = request.text();
        int cacheKey = text.hashCode();

        // Check cache
        byte[] cachedPcm;
        synchronized (audioCache) {
            cachedPcm = audioCache.get(cacheKey);
        }

        if (cachedPcm != null) {
            Gdx.app.log(TAG, "Cache hit: \"" + truncate(text) + "\"");
            playCachedAudio(cachedPcm, text);
            return;
        }

        // Synthesize with streaming callback
        Gdx.app.log(TAG, "Synthesizing: \"" + truncate(text) + "\"");
        synthesizeStreaming(text, cacheKey);
    }

    // -------------------------------------------------------------------------
    // ENGINE LIFECYCLE
    // -------------------------------------------------------------------------

    /**
     * Lazily initializes the Sherpa-ONNX OfflineTts engine on the worker thread.
     * This is moderately expensive (~1-3 seconds) and only happens once.
     */
    private void initializeEngineIfNeeded() {
        if (engineInitialized.get() || engineFailed.get()) return;

        try {
            Gdx.app.log(TAG, "Initializing Sherpa-ONNX engine...");
            long start = System.currentTimeMillis();

            String modelPath = modelBasePath + "/en_US-amy-low.onnx";
            String tokensPath = modelBasePath + "/tokens.txt";
            String dataDirPath = "tts-models/espeak-ng-data";

            OfflineTtsVitsModelConfig vitsConfig = OfflineTtsVitsModelConfig.builder()
                .setModel(modelPath)
                .setTokens(tokensPath)
                .setDataDir(dataDirPath)
                .build();

            OfflineTtsModelConfig modelConfig = OfflineTtsModelConfig.builder()
                .setVits(vitsConfig)
                .setNumThreads(2)
                .build();

            OfflineTtsConfig ttsConfig = OfflineTtsConfig.builder()
                .setModel(modelConfig)
                .build();

            tts = new OfflineTts(ttsConfig);
            sampleRate = tts.getSampleRate();

            openAudioLine(sampleRate);
        
        // Apply initial volume from settings
        updateVolume();

        engineInitialized.set(true);
            long elapsed = System.currentTimeMillis() - start;
            Gdx.app.log(TAG, "Sherpa-ONNX engine ready in " + elapsed
                + "ms (sample rate: " + sampleRate + " Hz).");
        } catch (Throwable t) {
            engineFailed.set(true);
            Gdx.app.error(TAG, "Failed to initialize Sherpa-ONNX. TTS will be unavailable.", t);
        }
    }

    /** Safely closes the Sherpa-ONNX engine, releasing ONNX native memory. */
    private void releaseEngine() {
        if (tts != null) {
            try {
                tts.release();
                Gdx.app.log(TAG, "Sherpa-ONNX engine released.");
            } catch (Exception e) {
                Gdx.app.error(TAG, "Error releasing Sherpa-ONNX engine.", e);
            }
            tts = null;
            engineInitialized.set(false);
        }
    }

    // -------------------------------------------------------------------------
    // STREAMING SYNTHESIS
    // -------------------------------------------------------------------------

    /**
     * Synthesizes text using {@code generateWithCallback}, streaming each PCM
     * chunk directly to the audio line as it is generated. The full PCM buffer
     * is also assembled for caching.
     */
    private void synthesizeStreaming(String text, int cacheKey) {
        // Accumulate all chunks for cache storage
        PcmAccumulator accumulator = new PcmAccumulator();

        // Track word boundaries for text-sync
        WordTracker wordTracker = new WordTracker(text, wordCallback);

        OfflineTtsCallback callback = new OfflineTtsCallback() {
            @Override
            public Integer invoke(float[] samples) {
                if (interrupted.get()) {
                    return 0; // Signal engine to stop generation
                }

                byte[] pcmBytes = floatToPcm16(samples);
                accumulator.append(pcmBytes);

                // Stream to audio output immediately
                writeToAudioLine(pcmBytes);

                // Fire word-boundary callbacks based on sample progress
                wordTracker.onSamplesGenerated(samples.length, sampleRate);

                return 1; // Continue generation
            }
        };

        tts.generateWithCallback(text, DEFAULT_SPEAKER_ID, DEFAULT_SPEED, callback);

        // Cache the complete PCM for future replays (unless interrupted)
        if (!interrupted.get()) {
            byte[] fullPcm = accumulator.toByteArray();
            if (fullPcm.length > 0) {
                synchronized (audioCache) {
                    audioCache.put(cacheKey, fullPcm);
                }
            }
        }
    }

    /**
     * Plays pre-cached PCM audio directly to the audio line, bypassing synthesis.
     * Also fires word-boundary callbacks based on estimated timing.
     */
    private void playCachedAudio(byte[] pcmData, String text) {
        WordTracker wordTracker = new WordTracker(text, wordCallback);

        // Write in chunks to allow interrupt checks and word-boundary callbacks
        int chunkSize = sampleRate * 2; // ~0.5 seconds of 16-bit mono audio
        int offset = 0;

        while (offset < pcmData.length && !interrupted.get()) {
            int remaining = pcmData.length - offset;
            int writeLen = Math.min(chunkSize, remaining);

            writeToAudioLine(pcmData, offset, writeLen);

            int samplesInChunk = writeLen / 2; // 16-bit = 2 bytes per sample
            wordTracker.onSamplesGenerated(samplesInChunk, sampleRate);

            offset += writeLen;
        }
    }

    // -------------------------------------------------------------------------
    // AUDIO OUTPUT (SourceDataLine — zero disk I/O)
    // -------------------------------------------------------------------------

    /**
     * Opens a {@link SourceDataLine} configured for the model's sample rate.
     * Mono, 16-bit signed PCM, little-endian.
     */
    private void openAudioLine(int rate) throws Exception {
        AudioFormat format = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            rate,       // sample rate
            16,         // sample size in bits
            1,          // channels (mono)
            2,          // frame size (16-bit mono = 2 bytes)
            rate,       // frame rate
            false       // little-endian
        );

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        audioLine = (SourceDataLine) AudioSystem.getLine(info);
        audioLine.open(format, rate * 2); // ~1 second buffer
        audioLine.start();

        Gdx.app.log(TAG, "Audio line opened: " + format);
    }

    /** Writes PCM bytes to the audio output line (blocking until written). */
    private void writeToAudioLine(byte[] pcmBytes) {
        if (audioLine != null && audioLine.isOpen()) {
            audioLine.write(pcmBytes, 0, pcmBytes.length);
        }
    }

    /** Writes a region of PCM bytes to the audio output line. */
    private void writeToAudioLine(byte[] pcmBytes, int offset, int length) {
        if (audioLine != null && audioLine.isOpen()) {
            audioLine.write(pcmBytes, offset, length);
        }
    }

    /** Flushes pending audio data and drains the line for a clean stop. */
    private void flushAudioLine() {
        if (audioLine != null && audioLine.isOpen()) {
            try {
                audioLine.stop();
                audioLine.flush();
                audioLine.start(); // Resume the line so next write plays immediately
            } catch (Exception e) {
                Gdx.app.error(TAG, "Error flushing audio line.", e);
            }
        }
    }

    /** Closes the audio output line. */
    private void closeAudioLine() {
        if (audioLine != null) {
            try {
                audioLine.stop();
                audioLine.close();
            } catch (Exception e) {
                Gdx.app.error(TAG, "Error closing audio line.", e);
            }
            audioLine = null;
        }
    }

    /**
     * Applies the given volume (0.0 to 1.0) to the open audio line.
     * Uses a logarithmic scale to convert to decibels.
     */
    private void applyVolume(float volume) {
        if (audioLine != null && audioLine.isOpen()) {
            try {
                if (audioLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
                    // Standard conversion: dB = 20 * log10(volume).
                    // Clamp to 0.0001 to avoid -infinity at volume=0.
                    float dB = (float) (Math.log10(Math.max(0.0001f, volume)) * 20.0f);
                    gainControl.setValue(dB);
                }
            } catch (Exception e) {
                Gdx.app.error(TAG, "Failed to apply volume: " + volume, e);
            }
        }
    }

    // -------------------------------------------------------------------------
    // PCM CONVERSION
    // -------------------------------------------------------------------------

    /**
     * Converts normalized float samples [-1.0, 1.0] to 16-bit signed PCM
     * in little-endian byte order.
     */
    private static byte[] floatToPcm16(float[] samples) {
        byte[] pcm = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            // Clamp to [-1.0, 1.0] and scale to 16-bit range
            float clamped = Math.max(-1.0f, Math.min(1.0f, samples[i]));
            short value = (short) (clamped * Short.MAX_VALUE);

            // Little-endian byte order
            pcm[i * 2] = (byte) (value & 0xFF);
            pcm[i * 2 + 1] = (byte) ((value >> 8) & 0xFF);
        }
        return pcm;
    }

    // -------------------------------------------------------------------------
    // WORD TRACKING (TEXT-SYNC)
    // -------------------------------------------------------------------------

    /**
     * Estimates word boundaries during streaming synthesis by correlating
     * the cumulative sample count with proportional progress through the text.
     *
     * <p>Since Piper VITS does not expose phoneme-level timing, this uses a
     * linear interpolation model: each character is assumed to take an equal
     * share of the total audio duration. This gives ~±100ms accuracy, which
     * is sufficient for syncing a typing effect.</p>
     */
    private static class WordTracker {
        private final WordCallback callback;
        private final int[] wordStarts;   // char offsets of each word
        private final int[] wordLengths;  // char lengths of each word
        private final int totalChars;

        private long totalSamplesGenerated = 0;
        private int nextWordIndex = 0;

        WordTracker(String text, WordCallback callback) {
            this.callback = callback;
            this.totalChars = text.length();

            // Pre-compute word boundaries
            String[] words = text.split("\\s+");
            wordStarts = new int[words.length];
            wordLengths = new int[words.length];

            int pos = 0;
            for (int i = 0; i < words.length; i++) {
                int idx = text.indexOf(words[i], pos);
                wordStarts[i] = idx;
                wordLengths[i] = words[i].length();
                pos = idx + words[i].length();
            }
        }

        /**
         * Called each time a chunk of samples is generated. Fires word
         * callbacks for any word boundaries that have been crossed.
         */
        void onSamplesGenerated(int sampleCount, int sampleRate) {
            if (callback == null || nextWordIndex >= wordStarts.length) return;

            totalSamplesGenerated += sampleCount;

            // We don't know total duration until synthesis finishes, so
            // we estimate based on a heuristic: ~80ms per character at
            // 1.0x speed. This is recalibrated as more samples arrive.
            float estimatedTotalSamples = totalChars * (0.08f / DEFAULT_SPEED) * sampleRate;
            float progress = (float) totalSamplesGenerated / Math.max(1, estimatedTotalSamples);
            progress = Math.min(1.0f, progress);

            int currentCharPos = (int) (progress * totalChars);

            while (nextWordIndex < wordStarts.length
                && wordStarts[nextWordIndex] <= currentCharPos) {
                int wordStart = wordStarts[nextWordIndex];
                int wordLen = wordLengths[nextWordIndex];
                nextWordIndex++;

                // Dispatch on libGDX render thread
                Gdx.app.postRunnable(() ->
                    callback.onWordBoundary(wordStart, wordLen));
            }
        }
    }

    // -------------------------------------------------------------------------
    // PCM ACCUMULATOR
    // -------------------------------------------------------------------------

    /**
     * Lightweight byte buffer that accumulates PCM chunks for caching.
     * Not thread-safe — used exclusively on the worker thread.
     */
    private static class PcmAccumulator {
        private byte[] buffer = new byte[16384];
        private int position = 0;

        void append(byte[] data) {
            ensureCapacity(position + data.length);
            System.arraycopy(data, 0, buffer, position, data.length);
            position += data.length;
        }

        byte[] toByteArray() {
            byte[] result = new byte[position];
            System.arraycopy(buffer, 0, result, 0, position);
            return result;
        }

        private void ensureCapacity(int minCapacity) {
            if (minCapacity > buffer.length) {
                int newCapacity = Math.max(buffer.length * 2, minCapacity);
                byte[] newBuffer = new byte[newCapacity];
                System.arraycopy(buffer, 0, newBuffer, 0, position);
                buffer = newBuffer;
            }
        }
    }

    // -------------------------------------------------------------------------
    // TEXT UTILITIES
    // -------------------------------------------------------------------------

    /**
     * Removes TextraTypist formatting tokens so the TTS engine receives
     * clean, readable prose.
     */
    private static String stripFormatting(String text) {
        return TEXTRA_TOKEN_PATTERN.matcher(text)
            .replaceAll("")
            .replaceAll("\\s+", " ")
            .trim();
    }

    /** Truncates text to 60 characters for log readability. */
    private static String truncate(String text) {
        return text.length() <= 60 ? text : text.substring(0, 57) + "...";
    }
}
