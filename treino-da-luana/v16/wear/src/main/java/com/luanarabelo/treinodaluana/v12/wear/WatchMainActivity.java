package com.luanarabelo.treinodaluana.v12.wear;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;
import com.luanarabelo.treinodaluana.v12.WorkoutData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public final class WatchMainActivity extends Activity implements DataClient.OnDataChangedListener {
    private static final int BG = Color.rgb(7, 7, 7);
    private static final int OBSIDIAN = Color.rgb(16, 16, 16);
    private static final int CARD = Color.rgb(26, 26, 28);
    private static final int CARD_DONE = Color.rgb(83, 43, 23);
    private static final int LINE = Color.rgb(74, 66, 62);
    private static final int WHITE = Color.rgb(247, 243, 239);
    private static final int MUTED = Color.rgb(170, 162, 157);
    private static final int ORANGE = Color.rgb(255, 138, 61);

    private static final int SCREEN_HOME = 0;
    private static final int SCREEN_WORKOUT = 1;
    private static final int SCREEN_EXERCISE = 2;
    private static final int SCREEN_SUMMARY = 3;
    private static final String PREFS = "treino_v12_watch8";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences preferences;
    private int screen = SCREEN_HOME;
    private int workout = -1;
    private int block = -1;
    private int exerciseOffset = 0;
    private long summaryStart;
    private long summaryEnd;
    private long summaryDeadline;
    private TextView countdownView;
    private Runnable summaryTick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        WatchProgressSync.prepareWeek(this);
        showHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
        WatchProgressSync.pullRemote(this, changed -> runOnUiThread(() -> {
            if (changed) refreshCurrentScreen();
        }));
    }

    @Override
    protected void onPause() {
        Wearable.getDataClient(this).removeListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cancelSummaryTimer();
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        boolean changed = false;
        for (DataEvent event : WearBufferCompat.<DataEvent>iterable(dataEvents)) {
            changed |= WatchProgressSync.applyEvent(this, event);
        }
        if (changed) runOnUiThread(this::refreshCurrentScreen);
    }

    private void showHome() {
        cancelSummaryTimer();
        screen = SCREEN_HOME;
        workout = -1;
        block = -1;

        LinearLayout content = contentColumn(14, 14);
        int done = totalDoneBlocks();
        ProgressRingView ring = new ProgressRingView(this, done, WorkoutData.TOTAL_BLOCKS,
                String.valueOf(done), "DE 24 DUPLAS");
        content.addView(ring, centeredFixed(62, 62));
        content.addView(spacer(3));

        for (int selected = 0; selected < WorkoutData.LETTERS.length; selected++) {
            final int selectedWorkout = selected;
            TextView card = workoutCard(selected, workoutDoneBlocks(selected));
            card.setOnClickListener(v -> openWorkoutSynced(selectedWorkout));
            content.addView(card, fullWrapWithMargins(0, 4));
        }
        content.addView(spacer(22));
        setShell(header("TREINO DA LUANA", true, false, this::finishAndRemoveTask), content, null);
    }

    private void openWorkoutSynced(int selectedWorkout) {
        WatchProgressSync.pullRemote(this, changed -> runOnUiThread(() -> showWorkout(selectedWorkout)));
    }

    private void showWorkout(int selectedWorkout) {
        cancelSummaryTimer();
        screen = SCREEN_WORKOUT;
        workout = selectedWorkout;
        block = -1;
        ensureWorkoutStarted();

        LinearLayout content = contentColumn(10, 68);
        content.addView(label(WorkoutData.TYPES[workout], 8, ORANGE, true), fullWrapWithMargins(0, 1));
        content.addView(label("Escolha qualquer dupla", 7, MUTED, true), fullWrapWithMargins(0, 5));
        for (int selectedBlock = 0; selectedBlock < WorkoutData.BLOCKS_PER_WORKOUT; selectedBlock++) {
            final int targetBlock = selectedBlock;
            View card = duoCard(selectedBlock);
            card.setOnClickListener(v -> showExercise(targetBlock, firstIncompleteOffset(targetBlock)));
            content.addView(card, fullWrapWithMargins(0, 7));
        }
        content.addView(spacer(18));

        TextView finish = compactButton("FINALIZAR TREINO", true, 8);
        finish.setOnClickListener(v -> showSummary());
        setShell(
                header("TREINO " + WorkoutData.LETTERS[workout], true, false,
                        () -> confirmExitWorkout(this::showHome)),
                content,
                singleFooter(finish)
        );
    }

    private void showExercise(int selectedBlock, int offset) {
        cancelSummaryTimer();
        ensureWorkoutStarted();
        screen = SCREEN_EXERCISE;
        block = selectedBlock;
        exerciseOffset = Math.max(0, Math.min(offset, WorkoutData.blockSize(block) - 1));
        int exercise = currentExercise();
        String position = block == WorkoutData.BLOCKS_PER_WORKOUT - 1
                ? "FINAL"
                : (block + 1) + (exerciseOffset == 0 ? "A" : "B");

        LinearLayout content = contentColumn(14, 3);
        content.addView(label(WorkoutData.TYPES[workout], 6, ORANGE, true), fullWrapWithMargins(0, 2));
        content.addView(exerciseImage(workout, exercise), fullHeightWithMargins(94, 0, 4));

        TextView title = label(WorkoutData.NAMES[workout][exercise], 10, WHITE, true);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        content.addView(title, fullWrapWithMargins(0, 2));

        String savedLoad = preferences.getString(loadKey(workout, exercise), "");
        String details = compactReps(WorkoutData.REPS[workout][exercise])
                + (savedLoad.isEmpty() ? "" : "  •  " + formatLoad(savedLoad));
        content.addView(label(details, 6, MUTED, true), fullWrapWithMargins(0, 3));
        content.addView(interactiveSeries(exercise), fullHeightWithMargins(31, 0, 4));

        boolean done = isExerciseDone(workout, exercise);
        TextView complete = compactButton(done ? "✓  CONCLUÍDO" : "CONCLUIR EXERCÍCIO", false, 6);
        complete.setOnClickListener(v -> {
            setExerciseDone(workout, exercise, !isExerciseDone(workout, exercise));
            showExercise(block, exerciseOffset);
        });
        content.addView(complete, fullHeightWithMargins(21, 0, 1));

        setShell(
                header("TREINO " + WorkoutData.LETTERS[workout] + " • " + position,
                        true, true, () -> showWorkout(workout)),
                content,
                exerciseFooter(exercise)
        );
    }

    private void showSummary() {
        cancelSummaryTimer();
        screen = SCREEN_SUMMARY;
        block = -1;
        summaryEnd = System.currentTimeMillis();
        summaryStart = preferences.getLong(startKey(workout), summaryEnd - 60_000L);
        if (summaryStart > summaryEnd || summaryEnd - summaryStart > 6L * 60L * 60L * 1000L) {
            summaryStart = summaryEnd - 60_000L;
        }
        preferences.edit().remove(startKey(workout)).apply();

        LinearLayout content = contentColumn(14, 14);
        ProgressRingView check = new ProgressRingView(this, 1, 1, "✓", "FINALIZADO");
        content.addView(check, centeredFixed(58, 58));
        content.addView(label("RESUMO DO TREINO", 9, WHITE, true), fullWrapWithMargins(3, 5));

        LinearLayout metrics = horizontal();
        metrics.addView(metric("TEMPO", elapsed(summaryEnd - summaryStart), WHITE), weightedHeight(39, 3));
        metrics.addView(metric("EXERCÍCIOS", completedExercises(workout) + " / 11", ORANGE), weightedHeight(39, 0));
        content.addView(metrics, fullHeightWithMargins(39, 0, 5));

        int pending = WorkoutData.NAMES[workout].length - completedExercises(workout);
        content.addView(label(pending == 0 ? "Todos os exercícios concluídos" : pending + " exercícios pendentes",
                7, MUTED, true), fullWrapWithMargins(0, 5));

        TextView sync = compactButton("SINCRONIZAR SAMSUNG HEALTH", true, 7);
        sync.setOnClickListener(v -> {
            WatchProgressSync.publishWorkoutSummary(this, workout, summaryStart, summaryEnd,
                    completedExercises(workout));
            sync.setText("SOLICITAÇÃO ENVIADA");
            sync.setBackground(panel(CARD_DONE, ORANGE, 18));
            sync.setTextColor(ORANGE);
        });
        content.addView(sync, fullHeightWithMargins(30, 0, 5));

        countdownView = label("Voltando ao início em 10 s", 7, MUTED, true);
        content.addView(countdownView, fullWrapWithMargins(0, 0));
        setShell(header("TREINO FINALIZADO", false, false, null), content, null);
        startSummaryTimer();
    }

    private View duoCard(int selectedBlock) {
        boolean done = isBlockDone(workout, selectedBlock);
        LinearLayout card = vertical();
        card.setPadding(dp(8), dp(7), dp(8), dp(8));
        card.setBackground(panel(done ? CARD_DONE : CARD, done ? ORANGE : LINE, 18));
        makeClickable(card);

        String title = selectedBlock == WorkoutData.BLOCKS_PER_WORKOUT - 1
                ? "FINALIZADOR"
                : "DUPLA " + (selectedBlock + 1);
        card.addView(label((done ? "✓  " : "") + title, 9, done ? ORANGE : WHITE, true), fullWrapWithMargins(0, 5));

        int start = WorkoutData.blockStart(selectedBlock);
        int size = WorkoutData.blockSize(selectedBlock);
        for (int offset = 0; offset < size; offset++) {
            card.addView(exercisePreview(start + offset), fullHeightWithMargins(51, 0, offset + 1 < size ? 5 : 0));
        }
        return card;
    }

    private View exercisePreview(int exercise) {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout photo = new FrameLayout(this);
        photo.setBackground(panel(OBSIDIAN, LINE, 12));
        Bitmap bitmap = loadBitmap(WorkoutData.imagePath(workout, exercise));
        if (bitmap != null) {
            ImageView image = new ImageView(this);
            image.setImageBitmap(bitmap);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photo.addView(image, new FrameLayout.LayoutParams(-1, -1));
        }
        row.addView(photo, new LinearLayout.LayoutParams(dp(54), dp(48)));

        LinearLayout copy = vertical();
        copy.setPadding(dp(7), 0, 0, 0);
        TextView name = label(WorkoutData.NAMES[workout][exercise], 8, WHITE, false);
        name.setMaxLines(2);
        name.setEllipsize(TextUtils.TruncateAt.END);
        copy.addView(name, new LinearLayout.LayoutParams(-1, 0, 1f));
        int completed = Integer.bitCount(seriesMask(workout, exercise));
        int total = WorkoutData.SETS[workout][exercise];
        String status = completed + " de " + total + " séries" + (isExerciseDone(workout, exercise) ? "  ✓" : "");
        copy.addView(label(status, 7, isExerciseDone(workout, exercise) ? ORANGE : MUTED, false), fullWrap());
        row.addView(copy, new LinearLayout.LayoutParams(0, -1, 1f));
        return row;
    }

    private View interactiveSeries(int exercise) {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.CENTER);
        int mask = seriesMask(workout, exercise);
        int sets = WorkoutData.SETS[workout][exercise];
        for (int index = 0; index < sets; index++) {
            final int setIndex = index;
            boolean checked = (mask & (1 << index)) != 0;
            TextView button = label(checked ? "✓" : String.valueOf(index + 1), 10,
                    checked ? BG : WHITE, true);
            button.setBackground(panel(checked ? ORANGE : CARD, checked ? ORANGE : LINE, 16));
            makeClickable(button);
            button.setOnClickListener(v -> {
                int updated = seriesMask(workout, exercise) ^ (1 << setIndex);
                preferences.edit().putInt(seriesKey(workout, exercise), updated).apply();
                WatchProgressSync.publishExerciseMask(this, workout, exercise, updated);
                showExercise(block, exerciseOffset);
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(26), dp(26));
            params.setMargins(dp(3), 0, dp(3), 0);
            row.addView(button, params);
        }
        return row;
    }

    private View exerciseFooter(int exercise) {
        LinearLayout footer = horizontal();
        footer.setGravity(Gravity.CENTER);

        TextView previous = circularNavigation("‹", false);
        previous.setOnClickListener(v -> {
            if (exercise <= 0) showWorkout(workout);
            else showExerciseByIndex(exercise - 1);
        });
        footer.addView(previous, new LinearLayout.LayoutParams(dp(34), dp(34)));

        TextView finish = label("FINALIZAR", 6, MUTED, true);
        makeClickable(finish);
        finish.setOnClickListener(v -> showSummary());
        LinearLayout.LayoutParams finishParams = new LinearLayout.LayoutParams(dp(72), dp(34));
        finishParams.setMargins(dp(5), 0, dp(5), 0);
        footer.addView(finish, finishParams);

        TextView next = circularNavigation(
                exercise >= WorkoutData.NAMES[workout].length - 1 ? "✓" : "›", true);
        next.setOnClickListener(v -> {
            if (exercise >= WorkoutData.NAMES[workout].length - 1) showSummary();
            else showExerciseByIndex(exercise + 1);
        });
        footer.addView(next, new LinearLayout.LayoutParams(dp(34), dp(34)));
        return footer;
    }

    private TextView circularNavigation(String symbol, boolean filled) {
        TextView button = label(symbol, 18, filled ? BG : WHITE, true);
        button.setBackground(panel(filled ? ORANGE : CARD, filled ? ORANGE : LINE, 17));
        makeClickable(button);
        return button;
    }

    private View singleFooter(TextView button) {
        LinearLayout footer = vertical();
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        footer.addView(button, centeredWidthHeight(62, 24));
        return footer;
    }

    private void showExerciseByIndex(int exercise) {
        for (int targetBlock = 0; targetBlock < WorkoutData.BLOCKS_PER_WORKOUT; targetBlock++) {
            int start = WorkoutData.blockStart(targetBlock);
            int size = WorkoutData.blockSize(targetBlock);
            if (exercise >= start && exercise < start + size) {
                showExercise(targetBlock, exercise - start);
                return;
            }
        }
    }

    private int firstIncompleteOffset(int selectedBlock) {
        int start = WorkoutData.blockStart(selectedBlock);
        int size = WorkoutData.blockSize(selectedBlock);
        for (int offset = 0; offset < size; offset++) {
            if (!isExerciseDone(workout, start + offset)) return offset;
        }
        return 0;
    }

    private int currentExercise() {
        return WorkoutData.blockStart(block) + exerciseOffset;
    }

    private void setExerciseDone(int selectedWorkout, int exercise, boolean done) {
        preferences.edit().putBoolean(exerciseDoneKey(selectedWorkout, exercise), done).apply();
        int selectedBlock = blockForExercise(exercise);
        boolean blockDone = isBlockDone(selectedWorkout, selectedBlock);
        preferences.edit().putBoolean(blockKey(selectedWorkout, selectedBlock), blockDone).apply();
        WatchProgressSync.publishBlock(this, selectedWorkout, selectedBlock, blockDone);
    }

    private int blockForExercise(int exercise) {
        for (int index = 0; index < WorkoutData.BLOCKS_PER_WORKOUT; index++) {
            int start = WorkoutData.blockStart(index);
            if (exercise >= start && exercise < start + WorkoutData.blockSize(index)) return index;
        }
        return WorkoutData.BLOCKS_PER_WORKOUT - 1;
    }

    private boolean isBlockDone(int selectedWorkout, int selectedBlock) {
        int start = WorkoutData.blockStart(selectedBlock);
        for (int offset = 0; offset < WorkoutData.blockSize(selectedBlock); offset++) {
            if (!isExerciseDone(selectedWorkout, start + offset)) return false;
        }
        return true;
    }

    private boolean isExerciseDone(int selectedWorkout, int exercise) {
        return preferences.getBoolean(exerciseDoneKey(selectedWorkout, exercise), false);
    }

    private int seriesMask(int selectedWorkout, int exercise) {
        return preferences.getInt(seriesKey(selectedWorkout, exercise), 0);
    }

    private int completedExercises(int selectedWorkout) {
        int done = 0;
        for (int exercise = 0; exercise < WorkoutData.NAMES[selectedWorkout].length; exercise++) {
            if (isExerciseDone(selectedWorkout, exercise)) done++;
        }
        return done;
    }

    private View exerciseImage(int selectedWorkout, int exercise) {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(panel(OBSIDIAN, ORANGE, 18));
        Bitmap bitmap = loadBitmap(WorkoutData.imagePath(selectedWorkout, exercise));
        if (bitmap != null) {
            ImageView image = new ImageView(this);
            image.setImageBitmap(bitmap);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setContentDescription("Demonstração de " + WorkoutData.NAMES[selectedWorkout][exercise]);
            frame.addView(image, new FrameLayout.LayoutParams(-1, -1));
        } else {
            ExerciseOutlineView outline = new ExerciseOutlineView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(120), -1, Gravity.CENTER);
            frame.addView(outline, params);
        }
        return frame;
    }

    private Bitmap loadBitmap(String path) {
        try (InputStream input = getAssets().open(path)) {
            return BitmapFactory.decodeStream(input);
        } catch (IOException ignored) {
            return null;
        }
    }

    private TextView workoutCard(int selectedWorkout, int done) {
        String value = "TREINO " + WorkoutData.LETTERS[selectedWorkout] + "\n"
                + WorkoutData.TYPES[selectedWorkout] + "  •  " + done + "/6";
        TextView view = label(value, 9, done == 6 ? ORANGE : WHITE, true);
        view.setLineSpacing(dp(1), 1f);
        view.setPadding(dp(9), dp(8), dp(9), dp(8));
        view.setBackground(panel(done == 6 ? CARD_DONE : CARD, done == 6 ? ORANGE : LINE, 18));
        makeClickable(view);
        return view;
    }

    private View metric(String caption, String value, int valueColor) {
        LinearLayout card = vertical();
        card.setGravity(Gravity.CENTER);
        card.setBackground(panel(CARD, LINE, 15));
        card.addView(label(caption, 6, MUTED, true), new LinearLayout.LayoutParams(-1, 0, 1f));
        card.addView(label(value, 12, valueColor, true), new LinearLayout.LayoutParams(-1, 0, 2f));
        return card;
    }

    private View header(String title, boolean showBack, boolean showDuos, Runnable backAction) {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(5), dp(20), 0);

        if (showBack) {
            TextView back = label("‹", 18, ORANGE, true);
            back.setBackground(panel(CARD, LINE, 18));
            makeClickable(back);
            back.setOnClickListener(v -> backAction.run());
            row.addView(back, new LinearLayout.LayoutParams(dp(23), dp(23)));
        } else {
            row.addView(new View(this), new LinearLayout.LayoutParams(dp(23), dp(23)));
        }

        TextView heading = label(title, 8, WHITE, true);
        heading.setMaxLines(1);
        heading.setEllipsize(TextUtils.TruncateAt.END);
        row.addView(heading, new LinearLayout.LayoutParams(0, dp(23), 1f));

        if (showDuos) {
            TextView duos = label("DUPLAS", 6, ORANGE, true);
            duos.setBackground(panel(CARD, ORANGE, 16));
            makeClickable(duos);
            duos.setOnClickListener(v -> showWorkout(workout));
            row.addView(duos, new LinearLayout.LayoutParams(dp(40), dp(23)));
        } else {
            row.addView(new View(this), new LinearLayout.LayoutParams(dp(23), dp(23)));
        }
        return row;
    }

    private TextView compactButton(String value, boolean filled, int size) {
        TextView view = label(value, size, filled ? BG : (value.startsWith("CONCLUIR") ? ORANGE : WHITE), true);
        view.setBackground(panel(filled ? ORANGE : CARD, filled ? ORANGE : LINE, 18));
        makeClickable(view);
        return view;
    }

    private TextView label(String value, int size, int color, boolean centered) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setGravity(centered ? Gravity.CENTER : Gravity.START | Gravity.CENTER_VERTICAL);
        view.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        view.setIncludeFontPadding(false);
        return view;
    }

    private void setShell(View header, View content, View footer) {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        FrameLayout.LayoutParams headerParams = new FrameLayout.LayoutParams(-1, dp(31), Gravity.TOP);
        root.addView(header, headerParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(-1, -1);
        scrollParams.topMargin = dp(33);
        scrollParams.bottomMargin = footer == null ? dp(8) : dp(51);
        root.addView(scroll, scrollParams);

        if (footer != null) {
            FrameLayout.LayoutParams footerParams = new FrameLayout.LayoutParams(-1, dp(43), Gravity.BOTTOM);
            footerParams.setMargins(dp(28), 0, dp(28), dp(4));
            root.addView(footer, footerParams);
        }
        setContentView(root);
    }

    private LinearLayout contentColumn(int sidePadding, int bottomPadding) {
        LinearLayout content = vertical();
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(sidePadding), dp(2), dp(sidePadding), dp(bottomPadding));
        return content;
    }

    private void makeClickable(View view) {
        view.setClickable(true);
        view.setFocusable(true);
    }

    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private GradientDrawable panel(int fill, int stroke, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        if (Color.alpha(stroke) > 0) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private void ensureWorkoutStarted() {
        String key = startKey(workout);
        if (!preferences.contains(key)) preferences.edit().putLong(key, System.currentTimeMillis()).apply();
    }

    private void confirmExitWorkout(Runnable confirmed) {
        new AlertDialog.Builder(this)
                .setTitle("Sair do treino?")
                .setMessage("O progresso parcial ficará salvo.")
                .setNegativeButton("CONTINUAR", null)
                .setPositiveButton("SAIR", (dialog, which) -> confirmed.run())
                .show();
    }

    private void startSummaryTimer() {
        cancelSummaryTimer();
        summaryDeadline = System.currentTimeMillis() + 10_000L;
        summaryTick = new Runnable() {
            @Override
            public void run() {
                long remaining = summaryDeadline - System.currentTimeMillis();
                if (remaining <= 0L) {
                    showHome();
                    return;
                }
                long seconds = (remaining + 999L) / 1_000L;
                if (countdownView != null) countdownView.setText("Voltando ao início em " + seconds + " s");
                handler.postDelayed(this, 250L);
            }
        };
        handler.post(summaryTick);
    }

    private void cancelSummaryTimer() {
        if (summaryTick != null) handler.removeCallbacks(summaryTick);
        summaryTick = null;
        countdownView = null;
    }

    private void refreshCurrentScreen() {
        if (screen == SCREEN_HOME) showHome();
        else if (screen == SCREEN_WORKOUT && workout >= 0) showWorkout(workout);
        else if (screen == SCREEN_EXERCISE && workout >= 0 && block >= 0) showExercise(block, exerciseOffset);
    }

    private String compactReps(String reps) {
        return reps.replace(" a ", "–").replace(" segundos", "s")
                .replace(" por perna", "/perna").replace(" por lado", "/lado");
    }

    private String formatLoad(String load) {
        return load.replace('.', ',') + " kg";
    }

    private String elapsed(long millis) {
        long totalSeconds = Math.max(1L, millis / 1_000L);
        long hours = totalSeconds / 3_600L;
        long minutes = (totalSeconds % 3_600L) / 60L;
        long seconds = totalSeconds % 60L;
        return hours > 0
                ? String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
                : String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private String blockKey(int selectedWorkout, int selectedBlock) {
        return "w" + selectedWorkout + "_b" + selectedBlock;
    }

    private String exerciseDoneKey(int selectedWorkout, int exercise) {
        return "w" + selectedWorkout + "_e" + exercise + "_done";
    }

    private String seriesKey(int selectedWorkout, int exercise) {
        return "w" + selectedWorkout + "_e" + exercise + "_mask";
    }

    private String loadKey(int selectedWorkout, int exercise) {
        return "load_w" + selectedWorkout + "_e" + exercise;
    }

    private String startKey(int selectedWorkout) {
        return "workout_start_w" + selectedWorkout;
    }

    private int workoutDoneBlocks(int selectedWorkout) {
        int done = 0;
        for (int index = 0; index < WorkoutData.BLOCKS_PER_WORKOUT; index++) {
            if (isBlockDone(selectedWorkout, index)) done++;
        }
        return done;
    }

    private int totalDoneBlocks() {
        int done = 0;
        for (int index = 0; index < WorkoutData.LETTERS.length; index++) done += workoutDoneBlocks(index);
        return done;
    }

    private LinearLayout.LayoutParams fullWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams fullWrapWithMargins(int top, int bottom) {
        LinearLayout.LayoutParams params = fullWrap();
        params.setMargins(0, dp(top), 0, dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams fullHeightWithMargins(int height, int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(height));
        params.setMargins(0, dp(top), 0, dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams weightedHeight(int height, int endMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(height), 1f);
        params.setMargins(0, 0, dp(endMargin), 0);
        return params;
    }

    private LinearLayout.LayoutParams centeredFixed(int width, int height) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(width), dp(height));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        return params;
    }

    private LinearLayout.LayoutParams centeredWidthHeight(int widthPercent, int height) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                Math.round(screenWidth * widthPercent / 100f), dp(height));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        return params;
    }

    private View spacer(int height) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(height)));
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (screen == SCREEN_HOME) {
            finishAndRemoveTask();
        } else if (screen == SCREEN_SUMMARY) {
            showHome();
        } else if (screen == SCREEN_EXERCISE) {
            showWorkout(workout);
        } else {
            confirmExitWorkout(this::showHome);
        }
    }
}
