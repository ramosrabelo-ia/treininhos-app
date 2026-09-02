package com.luanarabelo.treinodaluana.v12;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity implements DataClient.OnDataChangedListener {
    private static final String SAMSUNG_HEALTH_PACKAGE = "com.sec.android.app.shealth";
    private static final String HEALTH_AVAILABLE = "health_connect_available";
    private static final String HEALTH_GRANTED = "health_connect_granted";

    private static final int BLACK = Color.rgb(7, 7, 7);
    private static final int OBSIDIAN = Color.rgb(16, 16, 16);
    private static final int CARD = Color.rgb(24, 24, 24);
    private static final int CARD_LIGHT = Color.rgb(31, 31, 31);
    private static final int WHITE = Color.rgb(244, 239, 233);
    private static final int MUTED = Color.rgb(166, 157, 149);
    private static final int ORANGE = Color.rgb(255, 138, 61);
    private static final int ORANGE_DARK = Color.rgb(111, 58, 27);
    private static final int CYAN = Color.rgb(92, 200, 215);
    private static final int GREEN = Color.rgb(117, 205, 139);
    private static final int LINE = Color.rgb(58, 48, 42);

    private static final Locale PT_BR = new Locale("pt", "BR");

    private SharedPreferences preferences;
    private CountDownTimer restTimer;
    private TextView restTimerText;
    private int currentScreen = 0;
    private int currentWorkout = -1;
    private int currentBlock = 0;
    private boolean healthConnectAvailable;
    private boolean healthPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BLACK);
        getWindow().setNavigationBarColor(BLACK);
        preferences = getSharedPreferences("treino_v12", MODE_PRIVATE);
        healthConnectAvailable = preferences.getBoolean(HEALTH_AVAILABLE, false);
        healthPermissionGranted = preferences.getBoolean(HEALTH_GRANTED, false);
        openFromIntent(getIntent());
        PhoneProgressSync.migrateAndPublish(this);
        refreshHealthStatus(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        openFromIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
        PhoneProgressSync.pullRemote(this, changed -> {
            if (changed) runOnUiThread(this::refreshProgressScreen);
        });
        if (preferences != null) refreshHealthStatus(true);
    }

    @Override
    protected void onPause() {
        Wearable.getDataClient(this).removeListener(this);
        super.onPause();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        boolean changed = false;
        for (DataEvent event : WearBufferCompat.<DataEvent>iterable(dataEvents)) {
            changed |= PhoneProgressSync.applyEvent(this, event);
        }
        if (changed) runOnUiThread(this::refreshProgressScreen);
    }

    private void refreshProgressScreen() {
        if (currentScreen == 0) showHome();
        else if (currentScreen == 1 && currentWorkout >= 0) showBlock(currentWorkout, currentBlock);
        else if (currentScreen == 2) showProgress();
        else if (currentScreen == 3) showSyncSetup();
    }

    private void openFromIntent(Intent intent) {
        if (isPrivacyIntent(intent)) {
            showPrivacyPolicy();
        } else {
            showHome();
        }
    }

    private boolean isPrivacyIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) return false;
        String action = intent.getAction();
        return "androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE".equals(action)
                || Intent.ACTION_VIEW_PERMISSION_USAGE.equals(action);
    }

    @Override
    public void onBackPressed() {
        if (currentScreen == 1) {
            navigateBackFromBlock();
        } else if (currentScreen != 0) {
            showHome();
        } else {
            super.onBackPressed();
        }
    }

    private void refreshHealthStatus(boolean refreshVisibleScreen) {
        HealthConnectBridge.checkStatus(this, (available, granted) -> {
            boolean changed = available != healthConnectAvailable || granted != healthPermissionGranted;
            healthConnectAvailable = available;
            healthPermissionGranted = granted;
            preferences.edit()
                    .putBoolean(HEALTH_AVAILABLE, available)
                    .putBoolean(HEALTH_GRANTED, granted)
                    .apply();
            if (!refreshVisibleScreen || !changed) return;
            if (currentScreen == 0) showHome();
            if (currentScreen == 3) showSyncSetup();
        });
    }

    private void showHome() {
        cancelRestTimer();
        currentScreen = 0;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BLACK);
        scroll.setFillViewport(true);

        LinearLayout root = vertical();
        root.setPadding(dp(16), dp(16), dp(16), dp(34));
        scroll.addView(root, full());

        root.addView(createOfficialHero(), full(dp(230)));
        root.addView(space(14));
        root.addView(createWeeklySummary(), full());
        root.addView(space(24));

        root.addView(micro("PLANO CONJUGADO", ORANGE), full());
        TextView heading = heading("Sua semana em 4 treinos", 25);
        heading.setPadding(0, dp(5), 0, dp(14));
        root.addView(heading, full());

        for (int workout = 0; workout < WorkoutData.LETTERS.length; workout++) {
            root.addView(createWorkoutCard(workout), fullWithBottom(12));
        }

        Button progress = actionButton("VER PROGRESSO DA SEMANA", false);
        progress.setOnClickListener(view -> showProgress());
        root.addView(progress, full(dp(54)));

        TextView note = body(
                "Faça o exercício A, siga para o B e descanse somente depois dos dois. Ajuste cargas ou movimentos com um profissional quando precisar.",
                12,
                MUTED
        );
        note.setGravity(Gravity.CENTER);
        note.setPadding(dp(14), dp(18), dp(14), 0);
        root.addView(note, full());

        root.addView(space(24));
        root.addView(createSyncHomeCard(), full());

        setContentView(scroll);
    }

    private View createOfficialHero() {
        FrameLayout hero = new FrameLayout(this);
        hero.setBackground(round(OBSIDIAN, 24, 0, 0));
        hero.setClipToOutline(true);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setContentDescription("Foto oficial da Luana");
        image.setImageBitmap(loadAsset("heroes/hero_official.jpg"));
        hero.addView(image, frameMatch());

        View shade = new View(this);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.argb(238, 7, 7, 7), Color.argb(145, 7, 7, 7), Color.argb(18, 7, 7, 7)}
        );
        shade.setBackground(gradient);
        hero.addView(shade, frameMatch());

        TextView chip = micro("CAPA OFICIAL", WHITE);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(round(Color.argb(180, 16, 16, 16), 14, ORANGE, 1));
        FrameLayout.LayoutParams chipParams = new FrameLayout.LayoutParams(dp(126), dp(30));
        chipParams.gravity = Gravity.TOP | Gravity.START;
        chipParams.setMargins(dp(18), dp(18), 0, 0);
        hero.addView(chip, chipParams);

        LinearLayout copy = vertical();
        TextView title = heading("TREINO DA\nLUANA", 31);
        title.setLineSpacing(0, 0.92f);
        copy.addView(title, full());
        TextView subtitle = micro("V13  •  SAMSUNG SYNC", CYAN);
        subtitle.setPadding(0, dp(7), 0, 0);
        copy.addView(subtitle, full());

        FrameLayout.LayoutParams copyParams = new FrameLayout.LayoutParams(
                dp(285),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        copyParams.gravity = Gravity.BOTTOM | Gravity.START;
        copyParams.setMargins(dp(18), 0, 0, dp(18));
        hero.addView(copy, copyParams);
        return hero;
    }

    private View createWeeklySummary() {
        LinearLayout card = card(LINE);
        card.setPadding(dp(18), dp(17), dp(18), dp(17));

        LinearLayout top = horizontal();
        LinearLayout copy = vertical();
        copy.addView(micro("SEMANA ATUAL", ORANGE), full());
        TextView period = body(weekLabel(), 13, MUTED);
        period.setPadding(0, dp(4), 0, 0);
        copy.addView(period, full());
        top.addView(copy, weighted());

        int workouts = completedWorkoutCount();
        TextView score = heading(workouts + " de 4", 25);
        score.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        top.addView(score, wrap());
        card.addView(top, full());

        ProgressBar bar = progressBar((workouts * 100) / 4);
        LinearLayout.LayoutParams barParams = full(dp(7));
        barParams.setMargins(0, dp(15), 0, dp(12));
        card.addView(bar, barParams);

        int blocks = completedBlockCount();
        card.addView(body(blocks + " de " + WorkoutData.TOTAL_BLOCKS + " blocos concluídos", 14, WHITE), full());
        TextView helper = body(workouts == 4
                ? "Semana completa. Todos os check-ins estão feitos."
                : "Toque em um treino para começar ou continuar de onde parou.", 12, MUTED);
        helper.setPadding(0, dp(5), 0, 0);
        card.addView(helper, full());
        return card;
    }

    private View createSyncHomeCard() {
        int stroke = healthPermissionGranted ? GREEN : healthConnectAvailable ? ORANGE_DARK : LINE;
        LinearLayout card = card(stroke);
        card.setPadding(dp(16), dp(15), dp(16), dp(15));

        LinearLayout top = horizontal();
        LinearLayout copy = vertical();
        copy.addView(micro("GALAXY WATCH8 + SAMSUNG HEALTH", CYAN), full());
        TextView title = heading("Checks e atividade em sintonia", 19);
        title.setPadding(0, dp(3), 0, 0);
        copy.addView(title, full());
        top.addView(copy, weighted());

        String statusText = healthPermissionGranted ? "ATIVO" : healthConnectAvailable ? "CONFIGURAR" : "VERIFICAR";
        int statusColor = healthPermissionGranted ? GREEN : ORANGE;
        TextView status = micro(statusText, statusColor);
        status.setGravity(Gravity.CENTER);
        status.setBackground(round(CARD_LIGHT, 13, statusColor, 1));
        top.addView(status, new LinearLayout.LayoutParams(dp(102), dp(30)));
        card.addView(top, full());

        TextView detail = body(healthPermissionGranted
                ? "Os checks feitos no relógio voltam para o celular. Ao finalizar, a atividade segue ao Samsung Health pelo Health Connect."
                : "A sincronização de checks com o Galaxy Watch já está pronta. Autorize também o registro da atividade no Samsung Health.", 13, MUTED);
        detail.setPadding(0, dp(10), 0, dp(12));
        card.addView(detail, full());

        Button setup = actionButton(healthPermissionGranted ? "REVISAR CONEXÕES" : "CONFIGURAR SAMSUNG", false);
        setup.setOnClickListener(view -> showSyncSetup());
        card.addView(setup, full(dp(48)));
        return card;
    }

    private View createWorkoutCard(int workout) {
        int completed = completedBlockCount(workout);
        boolean done = completed == WorkoutData.BLOCKS_PER_WORKOUT;
        boolean started = completed > 0;

        LinearLayout card = card(done ? GREEN : LINE);
        card.setPadding(dp(16), dp(16), dp(16), dp(15));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(view -> showBlock(workout, firstIncompleteBlock(workout)));

        LinearLayout top = horizontal();
        TextView badge = heading(WorkoutData.LETTERS[workout], 22);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(done ? GREEN : ORANGE, 18, 0, 0));
        badge.setTextColor(BLACK);
        top.addView(badge, fixed(46, 46));

        LinearLayout titleCopy = vertical();
        titleCopy.setPadding(dp(13), 0, dp(8), 0);
        titleCopy.addView(heading("TREINO " + WorkoutData.LETTERS[workout], 19), full());
        TextView type = micro(WorkoutData.TYPES[workout], CYAN);
        type.setPadding(0, dp(3), 0, 0);
        titleCopy.addView(type, full());
        top.addView(titleCopy, weighted());

        String statusText = done ? "CONCLUÍDO" : started ? "CONTINUAR" : "COMEÇAR";
        TextView status = micro(statusText, done ? GREEN : ORANGE);
        status.setGravity(Gravity.CENTER);
        status.setBackground(round(CARD_LIGHT, 13, done ? GREEN : ORANGE, 1));
        top.addView(status, new LinearLayout.LayoutParams(dp(92), dp(30)));
        card.addView(top, full());

        TextView focus = body(WorkoutData.FOCUSES[workout], 16, WHITE);
        focus.setPadding(0, dp(15), 0, dp(4));
        card.addView(focus, full());
        TextView structure = body("5 duplas conjugadas  •  1 abdominal", 12, MUTED);
        structure.setPadding(0, 0, 0, dp(11));
        card.addView(structure, full());

        LinearLayout progressCopy = horizontal();
        progressCopy.addView(micro(completed + "/6 BLOCOS", MUTED), weighted());
        TextView percent = micro(((completed * 100) / WorkoutData.BLOCKS_PER_WORKOUT) + "%", done ? GREEN : ORANGE);
        percent.setGravity(Gravity.END);
        progressCopy.addView(percent, wrap());
        card.addView(progressCopy, full());

        ProgressBar bar = progressBar((completed * 100) / WorkoutData.BLOCKS_PER_WORKOUT);
        if (done) bar.setProgressTintList(ColorStateList.valueOf(GREEN));
        LinearLayout.LayoutParams barParams = full(dp(6));
        barParams.setMargins(0, dp(8), 0, 0);
        card.addView(bar, barParams);
        return card;
    }

    private void showBlock(int workout, int block) {
        cancelRestTimer();
        currentScreen = 1;
        currentWorkout = workout;
        currentBlock = block;
        ensureWorkoutStarted(workout);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BLACK);
        scroll.setFillViewport(true);
        LinearLayout root = vertical();
        applySafeTopPadding(scroll, root, 16, 20, 16, 34);
        scroll.addView(root, full());

        LinearLayout toolbar = horizontal();
        TextView back = micro(block > 0 ? "‹  BLOCO ANTERIOR" : "‹  TREINOS", ORANGE);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setMinHeight(dp(48));
        back.setPadding(dp(10), 0, dp(10), 0);
        back.setBackground(round(CARD, 14, LINE, 1));
        back.setContentDescription(block > 0 ? "Voltar ao bloco anterior" : "Voltar aos treinos");
        back.setClickable(true);
        back.setFocusable(true);
        back.setOnClickListener(view -> navigateBackFromBlock());
        toolbar.addView(back, weighted());
        TextView route = micro(
                "TREINO " + WorkoutData.LETTERS[workout] + "  •  BLOCO " + (block + 1) + "/6",
                MUTED
        );
        route.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        toolbar.addView(route, wrap());
        LinearLayout.LayoutParams toolbarParams = full(dp(48));
        toolbarParams.setMargins(0, 0, 0, dp(8));
        root.addView(toolbar, toolbarParams);

        boolean finalizer = block == WorkoutData.BLOCKS_PER_WORKOUT - 1;
        LinearLayout intro = card(finalizer ? CYAN : ORANGE_DARK);
        intro.setPadding(dp(15), dp(13), dp(15), dp(13));
        intro.addView(micro(WorkoutData.blockLabel(block), finalizer ? CYAN : ORANGE), full());
        TextView instruction = body(finalizer
                ? "Feche o treino com o abdômen e marque as séries."
                : "Faça o A, vá direto para o B e descanse somente depois dos dois.", 14, WHITE);
        instruction.setPadding(0, dp(6), 0, 0);
        intro.addView(instruction, full());
        root.addView(intro, fullWithBottom(12));

        int start = WorkoutData.blockStart(block);
        int size = WorkoutData.blockSize(block);
        if (size == 2) {
            LinearLayout photos = horizontal();
            View photoA = createExercisePhoto(workout, start, WorkoutData.exerciseLabel(block, 0));
            photos.addView(photoA, weightedPhoto(true));
            View photoB = createExercisePhoto(workout, start + 1, WorkoutData.exerciseLabel(block, 1));
            photos.addView(photoB, weightedPhoto(false));
            root.addView(photos, fullWithBottom(18, 250));
        } else {
            root.addView(createExercisePhoto(workout, start, "ABS"), fullWithBottom(18, 345));
        }

        for (int offset = 0; offset < size; offset++) {
            int exercise = start + offset;
            root.addView(createExerciseLog(workout, block, exercise, offset), fullWithBottom(12));
            if (offset == 0 && size == 2) {
                TextView arrow = micro("DEPOIS  →  EXERCÍCIO " + WorkoutData.exerciseLabel(block, 1), CYAN);
                arrow.setGravity(Gravity.CENTER);
                arrow.setBackground(round(CARD_LIGHT, 14, CYAN, 1));
                root.addView(arrow, fullWithBottom(12, 38));
            }
        }

        restTimerText = micro(finalizer ? "PAUSA 45s" : "DESCANSO APÓS A DUPLA 60s", CYAN);
        restTimerText.setGravity(Gravity.CENTER);
        restTimerText.setBackground(round(CARD_LIGHT, 16, CYAN, 1));
        restTimerText.setOnClickListener(view -> startRestTimer(finalizer ? 45 : 60));
        LinearLayout.LayoutParams restParams = full(dp(46));
        restParams.setMargins(0, dp(3), 0, dp(18));
        root.addView(restTimerText, restParams);

        String finishText = finalizer ? "FINALIZAR TREINO" : "CONCLUIR DUPLA E AVANÇAR";
        Button finish = actionButton(finishText, true);
        finish.setOnClickListener(view -> {
            markBlockComplete(workout, block);
            updateWorkoutCheckin(workout);
            syncBlock(workout, block);
            if (block < WorkoutData.BLOCKS_PER_WORKOUT - 1) {
                showBlock(workout, block + 1);
            } else {
                finishWorkout(workout);
            }
        });
        root.addView(finish, full(dp(58)));

        LinearLayout navigation = horizontal();
        Button previous = smallButton("ANTERIOR", block > 0);
        previous.setOnClickListener(view -> showBlock(workout, block - 1));
        navigation.addView(previous, weightedWithRight(6));
        Button next = smallButton("PRÓXIMO", block < WorkoutData.BLOCKS_PER_WORKOUT - 1);
        next.setOnClickListener(view -> showBlock(workout, block + 1));
        navigation.addView(next, weightedWithLeft(6));
        LinearLayout.LayoutParams navParams = full(dp(50));
        navParams.setMargins(0, dp(10), 0, 0);
        root.addView(navigation, navParams);

        setContentView(scroll);
        scroll.requestApplyInsets();
    }

    private void navigateBackFromBlock() {
        if (currentWorkout >= 0 && currentBlock > 0) {
            showBlock(currentWorkout, currentBlock - 1);
        } else {
            showHome();
        }
    }

    private View createExercisePhoto(int workout, int exercise, String label) {
        FrameLayout photo = new FrameLayout(this);
        photo.setBackground(round(OBSIDIAN, 22, 0, 0));
        photo.setClipToOutline(true);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setContentDescription("Demonstração de " + WorkoutData.NAMES[workout][exercise]);
        image.setImageBitmap(loadAsset(WorkoutData.imagePath(workout, exercise)));
        photo.addView(image, frameMatch());

        View shade = new View(this);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{Color.argb(210, 7, 7, 7), Color.argb(0, 7, 7, 7)}
        );
        shade.setBackground(gradient);
        FrameLayout.LayoutParams shadeParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(95),
                Gravity.BOTTOM
        );
        photo.addView(shade, shadeParams);

        TextView chip = heading(label, 18);
        chip.setTextColor(BLACK);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(round(ORANGE, 15, 0, 0));
        FrameLayout.LayoutParams chipParams = new FrameLayout.LayoutParams(dp(54), dp(34));
        chipParams.gravity = Gravity.TOP | Gravity.START;
        chipParams.setMargins(dp(12), dp(12), 0, 0);
        photo.addView(chip, chipParams);
        return photo;
    }

    private View createExerciseLog(int workout, int block, int exercise, int offset) {
        LinearLayout card = card(LINE);
        card.setPadding(dp(15), dp(15), dp(15), dp(15));

        String label = WorkoutData.exerciseLabel(block, offset);
        card.addView(micro(label + "  •  " + WorkoutData.TYPES[workout], CYAN), full());
        TextView name = heading(WorkoutData.NAMES[workout][exercise], 23);
        name.setPadding(0, dp(5), 0, dp(5));
        card.addView(name, full());

        String dose = WorkoutData.SETS[workout][exercise] + " SÉRIES  •  "
                + WorkoutData.REPS[workout][exercise].toUpperCase(PT_BR);
        TextView prescription = micro(dose, ORANGE);
        prescription.setPadding(0, 0, 0, dp(10));
        card.addView(prescription, full());

        TextView tip = body(WorkoutData.TIPS[workout][exercise], 13, MUTED);
        tip.setPadding(0, 0, 0, dp(12));
        card.addView(tip, full());

        card.addView(createLoadControl(workout, exercise), fullWithBottom(12));

        final int[] liveMask = {getMask(workout, exercise)};
        int totalSets = WorkoutData.SETS[workout][exercise];
        TextView setCounter = micro(setCount(liveMask[0], totalSets) + " DE " + totalSets + " CONCLUÍDAS", ORANGE);
        setCounter.setPadding(0, 0, 0, dp(8));
        card.addView(setCounter, full());

        for (int set = 0; set < totalSets; set++) {
            final int setIndex = set;
            Button setButton = new Button(this);
            setButton.setAllCaps(false);
            setButton.setStateListAnimator(null);
            styleSetButton(setButton, (liveMask[0] & (1 << setIndex)) != 0, setIndex, workout, exercise);
            setButton.setOnClickListener(view -> {
                ensureWorkoutStarted(workout);
                liveMask[0] ^= (1 << setIndex);
                saveMask(workout, exercise, liveMask[0]);
                boolean checked = (liveMask[0] & (1 << setIndex)) != 0;
                styleSetButton(setButton, checked, setIndex, workout, exercise);
                setCounter.setText(setCount(liveMask[0], totalSets) + " DE " + totalSets + " CONCLUÍDAS");
                updateWorkoutCheckin(workout);
                syncBlock(workout, block);
            });
            card.addView(setButton, fullWithBottom(8, 47));
        }
        return card;
    }

    private View createLoadControl(int workout, int exercise) {
        LinearLayout control = horizontal();
        control.setPadding(dp(12), dp(10), dp(10), dp(10));
        control.setBackground(round(OBSIDIAN, 16, LINE, 1));

        LinearLayout copy = vertical();
        copy.addView(micro("PROGRESSÃO DE CARGA", CYAN), full());
        String savedLoad = getLoad(workout, exercise);
        TextView helper = body(
                savedLoad.isEmpty() ? "Registre o peso usado neste exercício." : "A carga fica salva até você editar.",
                11,
                MUTED
        );
        helper.setPadding(0, dp(3), dp(8), 0);
        copy.addView(helper, full());
        control.addView(copy, weighted());

        Button loadButton = smallButton(loadButtonText(savedLoad), true);
        loadButton.setTextColor(savedLoad.isEmpty() ? ORANGE : WHITE);
        loadButton.setOnClickListener(view -> showLoadDialog(workout, exercise, loadButton, helper));
        control.addView(loadButton, fixed(118, 46));
        return control;
    }

    private void showLoadDialog(
            int workout,
            int exercise,
            Button loadButton,
            TextView helper
    ) {
        String savedLoad = getLoad(workout, exercise);
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(savedLoad.replace('.', ','));
        input.setHint("Ex.: 12,5");
        input.setSelectAllOnFocus(true);
        input.setTextColor(WHITE);
        input.setHintTextColor(MUTED);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(round(CARD_LIGHT, 14, ORANGE, 1));

        FrameLayout field = new FrameLayout(this);
        field.setPadding(dp(20), dp(6), dp(20), 0);
        field.addView(input, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Carga de " + WorkoutData.NAMES[workout][exercise])
                .setMessage("Digite o peso em kg. Ele continuará salvo nos próximos treinos.")
                .setView(field)
                .setNegativeButton("CANCELAR", null)
                .setNeutralButton("LIMPAR", (ignored, which) -> {
                    preferences.edit().remove(loadKey(workout, exercise)).apply();
                    PhoneProgressSync.publishLoad(this, workout, exercise, "");
                    refreshLoadControl(loadButton, helper, "");
                })
                .setPositiveButton("SALVAR", (ignored, which) -> {
                    String normalized = normalizeLoad(input.getText().toString());
                    if (normalized == null) {
                        Toast.makeText(this, "Digite uma carga válida em kg.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    preferences.edit().putString(loadKey(workout, exercise), normalized).apply();
                    PhoneProgressSync.publishLoad(this, workout, exercise, normalized);
                    refreshLoadControl(loadButton, helper, normalized);
                })
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ORANGE);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(CYAN);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(MUTED);
            input.requestFocus();
        });
        dialog.show();
    }

    private void refreshLoadControl(Button loadButton, TextView helper, String load) {
        loadButton.setText(loadButtonText(load));
        loadButton.setTextColor(load.isEmpty() ? ORANGE : WHITE);
        helper.setText(load.isEmpty()
                ? "Registre o peso usado neste exercício."
                : "A carga fica salva até você editar.");
    }

    private String normalizeLoad(String value) {
        String cleaned = value.trim().replace(',', '.');
        if (cleaned.isEmpty()) return "";
        try {
            BigDecimal load = new BigDecimal(cleaned);
            if (load.signum() < 0 || load.compareTo(new BigDecimal("1000")) > 0) return null;
            return load.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private String loadButtonText(String load) {
        if (load.isEmpty()) return "+ CARGA";
        return load.replace('.', ',') + " KG";
    }

    private void finishWorkout(int workout) {
        updateWorkoutCheckin(workout);
        long now = System.currentTimeMillis();
        long start = preferences.getLong(workoutStartKey(workout), now - 60L * 60L * 1000L);
        if (start > now || now - start > 6L * 60L * 60L * 1000L) {
            start = now - 60L * 60L * 1000L;
        }
        preferences.edit().remove(workoutStartKey(workout)).apply();

        String syncKey = workoutSyncKey(workout);
        if (preferences.getBoolean(syncKey, false)) {
            Toast.makeText(this, "Treino " + WorkoutData.LETTERS[workout] + " concluído", Toast.LENGTH_LONG).show();
            showHome();
            return;
        }

        if (!healthPermissionGranted) {
            Toast.makeText(
                    this,
                    "Treino concluído. Autorize o Health Connect para registrar a atividade no Samsung Health.",
                    Toast.LENGTH_LONG
            ).show();
            showHome();
            return;
        }

        String title = "Treino da Luana " + WorkoutData.LETTERS[workout];
        String notes = WorkoutData.FOCUSES[workout] + ". Cinco duplas conjugadas e um abdominal final.";
        HealthConnectBridge.writeStrengthWorkout(this, title, notes, start, now, (success, message) -> {
            if (success) {
                preferences.edit().putBoolean(syncKey, true).apply();
                Toast.makeText(
                        this,
                        "Treino salvo. O Samsung Health poderá sincronizar a atividade.",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(this, "Treino concluído, mas não sincronizado: " + message, Toast.LENGTH_LONG).show();
                refreshHealthStatus(false);
            }
        });
        showHome();
    }

    private void showSyncSetup() {
        cancelRestTimer();
        currentScreen = 3;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BLACK);
        scroll.setFillViewport(true);
        LinearLayout root = vertical();
        applySafeTopPadding(scroll, root, 16, 20, 16, 34);
        scroll.addView(root, full());

        TextView back = micro("‹  VOLTAR PARA O INÍCIO", ORANGE);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setOnClickListener(view -> showHome());
        root.addView(back, full(dp(42)));

        TextView eyebrow = micro("CELULAR + GALAXY WATCH8", CYAN);
        eyebrow.setPadding(0, dp(8), 0, dp(5));
        root.addView(eyebrow, full());
        root.addView(heading("Sincronização Samsung", 31), full());

        LinearLayout explanation = card(ORANGE_DARK);
        explanation.setPadding(dp(16), dp(15), dp(16), dp(15));
        explanation.addView(micro("COMO FUNCIONA", ORANGE), full());
        TextView explanationText = body(
                "A V13 usa duas pontes: o Wear OS sincroniza os checks dos 24 blocos nos dois sentidos; o Health Connect registra a sessão concluída para o Samsung Health.",
                14,
                WHITE
        );
        explanationText.setPadding(0, dp(7), 0, 0);
        explanation.addView(explanationText, full());
        LinearLayout.LayoutParams explanationParams = full();
        explanationParams.setMargins(0, dp(18), 0, dp(18));
        root.addView(explanation, explanationParams);

        root.addView(setupStep(
                "1",
                "Galaxy Watch8",
                "Marque ou desmarque um bloco no relógio. A mudança chega ao celular automaticamente quando os dois estiverem conectados.",
                "SYNC ATIVA",
                GREEN
        ), fullWithBottom(8));

        Button progressSync = actionButton("SINCRONIZAR CHECKS AGORA", true);
        progressSync.setOnClickListener(view -> {
            PhoneProgressSync.pullRemote(this, changed -> runOnUiThread(() -> {
                PhoneProgressSync.publishAll(this);
                Toast.makeText(this, changed ? "Checks atualizados pelo relógio" : "Sincronização solicitada", Toast.LENGTH_SHORT).show();
                showSyncSetup();
            }));
        });
        root.addView(progressSync, fullWithBottom(16, 54));

        root.addView(setupStep(
                "2",
                "Health Connect",
                "Permite somente escrever uma sessão de treino de força concluída. O app não lê seus dados de saúde.",
                healthPermissionGranted ? "AUTORIZADO" : healthConnectAvailable ? "PENDENTE" : "VERIFICAR",
                healthPermissionGranted ? GREEN : ORANGE
        ), fullWithBottom(8));

        Button permission = actionButton(
                healthPermissionGranted ? "PERMISSÃO DE TREINOS LIBERADA" : "AUTORIZAR REGISTRO DE TREINOS",
                !healthPermissionGranted
        );
        permission.setOnClickListener(view -> {
            if (healthPermissionGranted) {
                HealthConnectBridge.openSettings(this);
            } else {
                HealthConnectBridge.requestWritePermission(this);
            }
        });
        root.addView(permission, fullWithBottom(16, 54));

        Button healthSettings = actionButton("ABRIR HEALTH CONNECT", false);
        healthSettings.setOnClickListener(view -> HealthConnectBridge.openSettings(this));
        root.addView(healthSettings, fullWithBottom(16, 52));

        boolean samsungHealthInstalled = isSamsungHealthInstalled();
        root.addView(setupStep(
                "3",
                "Samsung Health",
                "No Samsung Health, conecte o Health Connect e permita a leitura de Exercícios para receber a atividade concluída.",
                samsungHealthInstalled ? "ABRIR APP" : "INSTALAR APP",
                samsungHealthInstalled ? CYAN : ORANGE
        ), fullWithBottom(8));
        Button samsung = actionButton(samsungHealthInstalled ? "ABRIR SAMSUNG HEALTH" : "INSTALAR SAMSUNG HEALTH", false);
        samsung.setOnClickListener(view -> openSamsungHealth());
        root.addView(samsung, fullWithBottom(12, 52));

        TextView footer = body(
                "O check do relógio atualiza o mesmo bloco no celular e a carga registrada no celular aparece no relógio. Ao concluir o último abdominal, a sessão “Treino da Luana A, B, C ou D” é registrada no Health Connect.",
                12,
                MUTED
        );
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(dp(12), dp(16), dp(12), 0);
        root.addView(footer, full());

        setContentView(scroll);
        scroll.requestApplyInsets();
    }

    private View setupStep(String number, String title, String detail, String status, int statusColor) {
        LinearLayout card = card(statusColor == MUTED ? LINE : statusColor);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout top = horizontal();
        TextView badge = heading(number, 18);
        badge.setTextColor(BLACK);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(statusColor == MUTED ? MUTED : statusColor, 15, 0, 0));
        top.addView(badge, fixed(40, 40));

        TextView titleView = heading(title, 17);
        titleView.setPadding(dp(12), 0, dp(8), 0);
        top.addView(titleView, weighted());

        TextView statusView = micro(status, statusColor);
        statusView.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        top.addView(statusView, wrap());
        card.addView(top, full());

        TextView detailView = body(detail, 13, MUTED);
        detailView.setPadding(dp(52), dp(8), 0, 0);
        card.addView(detailView, full());
        return card;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != HealthConnectBridge.PERMISSION_REQUEST_CODE) return;
        HealthConnectBridge.checkStatus(this, (available, granted) -> {
            healthConnectAvailable = available;
            healthPermissionGranted = granted;
            preferences.edit()
                    .putBoolean(HEALTH_AVAILABLE, available)
                    .putBoolean(HEALTH_GRANTED, granted)
                    .apply();
            Toast.makeText(
                    this,
                    granted ? "Atividades conectadas ao Health Connect" : "Permissão não liberada. O treino continuará normalmente.",
                    Toast.LENGTH_LONG
            ).show();
            showSyncSetup();
        });
    }

    private boolean isSamsungHealthInstalled() {
        return getPackageManager().getLaunchIntentForPackage(SAMSUNG_HEALTH_PACKAGE) != null;
    }

    private void openSamsungHealth() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(SAMSUNG_HEALTH_PACKAGE);
        if (launch != null) {
            startActivity(launch);
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + SAMSUNG_HEALTH_PACKAGE)));
        } catch (Throwable error) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + SAMSUNG_HEALTH_PACKAGE)
            ));
        }
    }

    private void showPrivacyPolicy() {
        cancelRestTimer();
        currentScreen = 4;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BLACK);
        LinearLayout root = vertical();
        root.setPadding(dp(20), dp(28), dp(20), dp(34));
        scroll.addView(root, full());

        root.addView(micro("PRIVACIDADE", ORANGE), full());
        TextView title = heading("Como a V14 usa seus dados", 30);
        title.setPadding(0, dp(7), 0, dp(20));
        root.addView(title, full());

        LinearLayout policy = card(LINE);
        policy.setPadding(dp(17), dp(17), dp(17), dp(17));
        policy.addView(heading("Somente escrita de atividade", 20), full());
        TextView text = body(
                "A V14 solicita apenas permissão para gravar no Health Connect uma sessão de treino de força concluída. O registro contém horário de início, horário de fim, letra do treino e grupo muscular.\n\n"
                        + "Os checks dos 24 blocos e as cargas são sincronizados diretamente entre o aplicativo Android e o Galaxy Watch8 pelo Wear OS Data Layer. Eles ficam nos seus aparelhos e não passam por um servidor do Treino da Luana.\n\n"
                        + "O aplicativo não lê peso, frequência cardíaca, sono ou localização. Você pode revogar a permissão a qualquer momento. O compartilhamento com o Samsung Health depende das permissões escolhidas por você no Health Connect e no Samsung Health.",
                14,
                WHITE
        );
        text.setPadding(0, dp(10), 0, 0);
        policy.addView(text, full());
        root.addView(policy, fullWithBottom(18));

        Button home = actionButton("VOLTAR AO APP", true);
        home.setOnClickListener(view -> showHome());
        root.addView(home, full(dp(56)));
        setContentView(scroll);
    }

    private void showProgress() {
        cancelRestTimer();
        currentScreen = 2;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BLACK);
        LinearLayout root = vertical();
        applySafeTopPadding(scroll, root, 16, 20, 16, 34);
        scroll.addView(root, full());

        TextView back = micro("‹  VOLTAR PARA O INÍCIO", ORANGE);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setOnClickListener(view -> showHome());
        root.addView(back, full(dp(42)));

        TextView eyebrow = micro("CHECK-IN SEMANAL", CYAN);
        eyebrow.setPadding(0, dp(8), 0, dp(5));
        root.addView(eyebrow, full());
        root.addView(heading("Seu progresso", 31), full());

        int workouts = completedWorkoutCount();
        int blocks = completedBlockCount();
        int percent = (blocks * 100) / WorkoutData.TOTAL_BLOCKS;

        LinearLayout hero = card(ORANGE_DARK);
        hero.setPadding(dp(18), dp(19), dp(18), dp(19));
        LinearLayout stats = horizontal();
        LinearLayout percentBox = vertical();
        percentBox.addView(micro("SEMANA ATUAL", ORANGE), full());
        TextView percentText = heading(percent + "%", 42);
        percentText.setPadding(0, dp(5), 0, 0);
        percentBox.addView(percentText, full());
        stats.addView(percentBox, weighted());

        LinearLayout countBox = vertical();
        TextView workoutCount = heading(workouts + "/4", 24);
        workoutCount.setGravity(Gravity.END);
        countBox.addView(workoutCount, full());
        TextView workoutLabel = body("treinos", 12, MUTED);
        workoutLabel.setGravity(Gravity.END);
        countBox.addView(workoutLabel, full());
        TextView blockCount = body(blocks + "/24 blocos", 13, WHITE);
        blockCount.setGravity(Gravity.END);
        blockCount.setPadding(0, dp(7), 0, 0);
        countBox.addView(blockCount, full());
        stats.addView(countBox, weighted());
        hero.addView(stats, full());

        ProgressBar totalProgress = progressBar(percent);
        LinearLayout.LayoutParams progressParams = full(dp(8));
        progressParams.setMargins(0, dp(17), 0, 0);
        hero.addView(totalProgress, progressParams);
        LinearLayout.LayoutParams heroParams = full();
        heroParams.setMargins(0, dp(18), 0, dp(24));
        root.addView(hero, heroParams);

        TextView week = heading("Treinos da semana", 22);
        week.setPadding(0, 0, 0, dp(12));
        root.addView(week, full());
        for (int workout = 0; workout < 4; workout++) {
            root.addView(createProgressRow(workout), fullWithBottom(10));
        }

        TextView message = body(workouts == 4
                ? "Semana fechada. Quatro treinos, quatro check-ins."
                : "Cada série marcada fica salva no celular. Você pode sair e continuar depois.", 14, MUTED);
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(18), dp(12), dp(18), dp(18));
        root.addView(message, full());

        Button home = actionButton("VOLTAR AOS TREINOS", true);
        home.setOnClickListener(view -> showHome());
        root.addView(home, full(dp(56)));
        setContentView(scroll);
        scroll.requestApplyInsets();
    }

    private View createProgressRow(int workout) {
        int completed = completedBlockCount(workout);
        boolean done = completed == WorkoutData.BLOCKS_PER_WORKOUT;
        LinearLayout row = horizontal();
        row.setBackground(round(CARD, 22, done ? GREEN : LINE, 1));
        row.setPadding(dp(14), dp(14), dp(14), dp(14));
        row.setOnClickListener(view -> showBlock(workout, firstIncompleteBlock(workout)));

        TextView badge = heading(WorkoutData.LETTERS[workout], 19);
        badge.setTextColor(BLACK);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(done ? GREEN : ORANGE, 16, 0, 0));
        row.addView(badge, fixed(42, 42));

        LinearLayout copy = vertical();
        copy.setPadding(dp(12), 0, dp(8), 0);
        copy.addView(heading(WorkoutData.TYPES[workout], 16), full());
        TextView focus = body(WorkoutData.FOCUSES[workout], 12, MUTED);
        focus.setPadding(0, dp(3), 0, 0);
        copy.addView(focus, full());
        row.addView(copy, weighted());

        TextView status = micro(done ? "CHECK-IN" : completed + "/6", done ? GREEN : ORANGE);
        status.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        row.addView(status, wrap());
        return row;
    }

    private void styleSetButton(Button button, boolean checked, int set, int workout, int exercise) {
        String prefix = checked ? "✓  " : "○  ";
        button.setText(prefix + "SÉRIE " + (set + 1) + "     " + WorkoutData.REPS[workout][exercise]);
        button.setTextSize(13);
        button.setTextColor(checked ? BLACK : WHITE);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setPadding(dp(14), 0, dp(14), 0);
        button.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        button.setBackground(round(checked ? ORANGE : CARD_LIGHT, 16, checked ? ORANGE : LINE, 1));
    }

    private void startRestTimer(int totalSeconds) {
        cancelRestTimerOnly();
        if (restTimerText == null) return;
        restTimerText.setText(String.format(PT_BR, "DESCANSO 00:%02d", totalSeconds));
        restTimer = new CountDownTimer(totalSeconds * 1_000L, 1_000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished + 999) / 1_000;
                restTimerText.setText(String.format(PT_BR, "DESCANSO 00:%02d", seconds));
            }

            @Override
            public void onFinish() {
                restTimerText.setText("DESCANSO CONCLUÍDO");
                restTimerText.setTextColor(ORANGE);
            }
        }.start();
    }

    private void cancelRestTimerOnly() {
        if (restTimer != null) {
            restTimer.cancel();
            restTimer = null;
        }
    }

    private void cancelRestTimer() {
        cancelRestTimerOnly();
        restTimerText = null;
    }

    private Bitmap loadAsset(String path) {
        try (InputStream stream = getAssets().open(path)) {
            return BitmapFactory.decodeStream(stream);
        } catch (IOException error) {
            Toast.makeText(this, "Não foi possível abrir a foto deste exercício.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String weekKey() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private String weekLabel() {
        Calendar calendar = Calendar.getInstance();
        String day = new SimpleDateFormat("d 'de' MMMM", PT_BR).format(calendar.getTime());
        return "Semana " + calendar.get(Calendar.WEEK_OF_YEAR) + "  •  " + day;
    }

    private String maskKey(int workout, int exercise) {
        return weekKey() + "_w" + workout + "_e" + exercise;
    }

    private String loadKey(int workout, int exercise) {
        return "load_w" + workout + "_e" + exercise;
    }

    private String getLoad(int workout, int exercise) {
        return preferences.getString(loadKey(workout, exercise), "");
    }

    private String workoutStartKey(int workout) {
        return weekKey() + "_start_" + workout;
    }

    private String workoutSyncKey(int workout) {
        return weekKey() + "_health_synced_" + workout;
    }

    private int getMask(int workout, int exercise) {
        return preferences.getInt(maskKey(workout, exercise), 0);
    }

    private void saveMask(int workout, int exercise, int mask) {
        preferences.edit().putInt(maskKey(workout, exercise), mask).apply();
    }

    private void syncBlock(int workout, int block) {
        int start = WorkoutData.blockStart(block);
        int size = WorkoutData.blockSize(block);
        for (int offset = 0; offset < size; offset++) {
            int exercise = start + offset;
            PhoneProgressSync.publishExerciseMask(this, workout, exercise, getMask(workout, exercise));
        }
        PhoneProgressSync.publishBlock(this, workout, block, isBlockComplete(workout, block));
    }

    private void ensureWorkoutStarted(int workout) {
        String key = workoutStartKey(workout);
        if (!preferences.contains(key)) {
            preferences.edit().putLong(key, System.currentTimeMillis()).apply();
        }
    }

    private boolean isExerciseComplete(int workout, int exercise) {
        int sets = WorkoutData.SETS[workout][exercise];
        int all = (1 << sets) - 1;
        return (getMask(workout, exercise) & all) == all;
    }

    private boolean isBlockComplete(int workout, int block) {
        int start = WorkoutData.blockStart(block);
        int size = WorkoutData.blockSize(block);
        for (int offset = 0; offset < size; offset++) {
            if (!isExerciseComplete(workout, start + offset)) return false;
        }
        return true;
    }

    private boolean isWorkoutComplete(int workout) {
        return completedBlockCount(workout) == WorkoutData.BLOCKS_PER_WORKOUT;
    }

    private void markExerciseComplete(int workout, int exercise) {
        int sets = WorkoutData.SETS[workout][exercise];
        saveMask(workout, exercise, (1 << sets) - 1);
    }

    private void markBlockComplete(int workout, int block) {
        int start = WorkoutData.blockStart(block);
        int size = WorkoutData.blockSize(block);
        for (int offset = 0; offset < size; offset++) markExerciseComplete(workout, start + offset);
    }

    private void updateWorkoutCheckin(int workout) {
        String key = weekKey() + "_checkin_" + workout;
        SharedPreferences.Editor editor = preferences.edit();
        if (isWorkoutComplete(workout)) {
            if (!preferences.contains(key)) editor.putLong(key, System.currentTimeMillis());
        } else {
            editor.remove(key);
            editor.remove(workoutSyncKey(workout));
        }
        editor.apply();
    }

    private int completedBlockCount(int workout) {
        int count = 0;
        for (int block = 0; block < WorkoutData.BLOCKS_PER_WORKOUT; block++) {
            if (isBlockComplete(workout, block)) count++;
        }
        return count;
    }

    private int completedBlockCount() {
        int count = 0;
        for (int workout = 0; workout < 4; workout++) count += completedBlockCount(workout);
        return count;
    }

    private int completedWorkoutCount() {
        int count = 0;
        for (int workout = 0; workout < 4; workout++) if (isWorkoutComplete(workout)) count++;
        return count;
    }

    private int firstIncompleteBlock(int workout) {
        for (int block = 0; block < WorkoutData.BLOCKS_PER_WORKOUT; block++) {
            if (!isBlockComplete(workout, block)) return block;
        }
        return 0;
    }

    private int setCount(int mask, int totalSets) {
        int count = 0;
        for (int set = 0; set < totalSets; set++) if ((mask & (1 << set)) != 0) count++;
        return count;
    }

    private void applySafeTopPadding(
            View host,
            LinearLayout content,
            int left,
            int topSpacing,
            int right,
            int bottom
    ) {
        content.setPadding(dp(left), dp(topSpacing), dp(right), dp(bottom));
        host.setOnApplyWindowInsetsListener((view, insets) -> {
            int statusBarHeight = insets.getSystemWindowInsetTop();
            content.setPadding(
                    dp(left),
                    statusBarHeight + dp(topSpacing),
                    dp(right),
                    dp(bottom)
            );
            return insets;
        });
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    private LinearLayout card(int strokeColor) {
        LinearLayout layout = vertical();
        layout.setBackground(round(CARD, 22, strokeColor, 1));
        return layout;
    }

    private TextView heading(String value, int size) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(WHITE);
        view.setTextSize(size);
        view.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        view.setLetterSpacing(0.02f);
        return view;
    }

    private TextView body(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        view.setLineSpacing(dp(2), 1.0f);
        return view;
    }

    private TextView micro(String value, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(11);
        view.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        view.setLetterSpacing(0.08f);
        return view;
    }

    private Button actionButton(String value, boolean primary) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setStateListAnimator(null);
        button.setText(value);
        button.setTextSize(14);
        button.setTextColor(primary ? BLACK : ORANGE);
        button.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        button.setBackground(round(primary ? ORANGE : CARD, 18, ORANGE, 1));
        return button;
    }

    private Button smallButton(String value, boolean enabled) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setStateListAnimator(null);
        button.setText(value);
        button.setTextSize(12);
        button.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        button.setTextColor(enabled ? WHITE : Color.rgb(78, 75, 72));
        button.setBackground(round(CARD, 16, enabled ? LINE : Color.rgb(35, 35, 35), 1));
        button.setEnabled(enabled);
        return button;
    }

    private ProgressBar progressBar(int progress) {
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(progress);
        bar.setProgressTintList(ColorStateList.valueOf(ORANGE));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(52, 46, 42)));
        return bar;
    }

    private GradientDrawable round(int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeColor != 0 && strokeDp > 0) drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private View space(int height) {
        View view = new View(this);
        view.setLayoutParams(full(dp(height)));
        return view;
    }

    private LinearLayout.LayoutParams full() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams full(int height) {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
    }

    private LinearLayout.LayoutParams fullWithBottom(int bottom) {
        LinearLayout.LayoutParams params = full();
        params.setMargins(0, 0, 0, dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams fullWithBottom(int bottom, int height) {
        LinearLayout.LayoutParams params = full(dp(height));
        params.setMargins(0, 0, 0, dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    }

    private LinearLayout.LayoutParams weightedPhoto(boolean left) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        if (left) params.setMargins(0, 0, dp(5), 0);
        else params.setMargins(dp(5), 0, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams weightedWithRight(int margin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        params.setMargins(0, 0, dp(margin), 0);
        return params;
    }

    private LinearLayout.LayoutParams weightedWithLeft(int margin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        params.setMargins(dp(margin), 0, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams fixed(int width, int height) {
        return new LinearLayout.LayoutParams(dp(width), dp(height));
    }

    private FrameLayout.LayoutParams frameMatch() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
