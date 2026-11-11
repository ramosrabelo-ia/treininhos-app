package br.com.musculacao.watch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private static final int ORANGE = 0xffff7900, WHITE = Color.WHITE, MUTED = 0xffc8c1ba;
    private SharedPreferences prefs;
    private int workout = -1, exercise = -1;
    private long session = -1;
    private String screen = "home";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        prefs = getSharedPreferences("musculacao_v1", MODE_PRIVATE);
        showHome();
    }
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextColor(color); t.setTextSize(size);
        t.setGravity(Gravity.CENTER); t.setIncludeFontPadding(false);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private GradientDrawable bg(int color, int border) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(24));
        if (border != 0) d.setStroke(dp(1), border); return d;
    }
    private TextView button(String label, boolean highlight, View.OnClickListener click) {
        TextView b = text(label, 12, highlight ? Color.BLACK : WHITE, true);
        b.setBackground(bg(highlight ? ORANGE : 0xff242424, 0));
        b.setMinHeight(dp(44)); b.setPadding(dp(10), dp(8), dp(10), dp(8));
        b.setOnClickListener(click); return b;
    }
    private LinearLayout column() {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER_HORIZONTAL); c.setPadding(dp(28), dp(28), dp(28), dp(28)); return c;
    }
    private void add(LinearLayout c, View v, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(bottom); c.addView(v, p);
    }
    private void list(LinearLayout c) {
        ScrollView sc = new ScrollView(this); sc.setBackgroundColor(Color.BLACK); sc.setFillViewport(true);
        sc.setVerticalScrollBarEnabled(false); sc.addView(c); setContentView(sc);
    }
    private void showHome() {
        screen = "home"; workout = -1; exercise = -1;
        LinearLayout c = column(); add(c, text("MUSCULAÇÃO", 16, WHITE, true), 4);
        add(c, text("ESCOLHA SEU TREINO", 9, ORANGE, true), 12);
        for (int i = 0; i < 4; i++) {
            final int w = i;
            add(c, button("TREINO " + WorkoutData.LETTERS[i] + "\n" + WorkoutData.FOCUSES[i], false, v -> chooseWorkout(w)), 8);
        }
        list(c);
    }
    private void chooseWorkout(int w) {
        workout = w;
        long active = prefs.getLong("active_" + w, 0);
        if (active == 0) { newSession(); return; }
        new AlertDialog.Builder(this).setTitle("Treino " + WorkoutData.LETTERS[w])
            .setItems(new String[]{"Continuar treino", "Começar novo treino"}, (d, which) -> {
                if (which == 0) { session = active; showWorkout(); } else newSession();
            }).setNegativeButton("Voltar", (d, which) -> showHome()).show();
    }
    private void newSession() {
        session = System.currentTimeMillis();
        prefs.edit().putLong("active_" + workout, session).putLong("start_" + session, session).apply();
        showWorkout();
    }
    private String setKey(int e) { return "s_" + session + "_" + workout + "_" + e; }
    private String valueKey(int e, String kind) { return kind + "_" + workout + "_" + e; }
    private int done(int e) { return prefs.getInt(setKey(e), 0); }
    private void showWorkout() {
        screen = "workout"; exercise = -1;
        LinearLayout c = column(); add(c, text("TREINO " + WorkoutData.LETTERS[workout], 16, WHITE, true), 4);
        add(c, text(WorkoutData.FOCUSES[workout], 9, MUTED, false), 12);
        for (int b = 0; b < 5; b++) {
            final int first = b * 2;
            String names = WorkoutData.NAMES[workout][first] + "\n" + WorkoutData.NAMES[workout][first + 1];
            String completed = (done(first) == 3 && done(first+1) == 3) ? "  ✓" : "";
            add(c, button("DUPLA " + (b+1) + completed + "\n" + names, false, v -> openDuo(first)), 8);
        }
        add(c, button("FINALIZAR", false, v -> confirmFinish()), 0); list(c);
    }
    private void openDuo(int first) {
        int offset = DuoSequence.nextOffset(done(first), done(first + 1));
        if (offset < 0) {
            new AlertDialog.Builder(this).setMessage("Dupla concluída. As séries continuam salvas.")
                .setPositiveButton("OK", null).show();
        } else showExercise(first + offset);
    }
    private void showExercise(int e) {
        screen = "exercise"; exercise = e;
        FrameLayout root = new FrameLayout(this); root.setBackgroundColor(Color.BLACK);
        ImageView image = new ImageView(this); image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        try (java.io.InputStream in = getAssets().open(WorkoutData.imagePath(workout, e))) { image.setImageBitmap(BitmapFactory.decodeStream(in)); }
        catch (java.io.IOException ignored) { image.setBackgroundColor(0xff242424); }
        image.setContentDescription("Imagem: " + WorkoutData.NAMES[workout][e]); root.addView(image, new FrameLayout.LayoutParams(-1,-1));
        View shade = new View(this); shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xcc000000,0x33000000,0x99000000,0xff000000})); root.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout overlay = new LinearLayout(this); overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.CENTER_HORIZONTAL); overlay.setPadding(dp(26),dp(18),dp(26),dp(15));
        int marked = Integer.bitCount(done(e));
        add(overlay, text(marked < 2 ? "SÉRIE " + (marked+1) + " DE 2" : "SÉRIES CONCLUÍDAS", 12, WHITE, true), 0);
        overlay.addView(new View(this), new LinearLayout.LayoutParams(1,0,1));
        TextView title = text(WorkoutData.NAMES[workout][e], 14, WHITE, true); title.setMaxLines(2); add(overlay,title,3);
        String load = prefs.getString(valueKey(e,"load"), ""), reps = prefs.getString(valueKey(e,"reps"), "");
        String details = (load.isEmpty() ? "" : load + " kg") + (load.isEmpty() || reps.isEmpty() ? "" : "  •  ") + (reps.isEmpty()?"":reps + " rep.");
        if (!details.isEmpty()) add(overlay,text(details,9,WHITE,true),3);
        TextView edit = text("EDITAR CARGA / REPS",8,WHITE,true); edit.setMinHeight(dp(30)); edit.setOnClickListener(v -> edit(e)); add(overlay,edit,2);
        add(overlay,button("SÉRIE FEITA",true,v -> {
            int first = (e / 2) * 2;
            int updated = DuoSequence.markNext(done(e));
            prefs.edit().putInt(setKey(e), updated).apply();
            int offset = DuoSequence.nextOffset(done(first), done(first + 1));
            if (offset < 0) showWorkout(); else showExercise(first + offset);
        }),0);
        root.addView(overlay,new FrameLayout.LayoutParams(-1,-1)); setContentView(root);
    }
    private void edit(int e) {
        LinearLayout fields = new LinearLayout(this); fields.setOrientation(LinearLayout.VERTICAL); fields.setPadding(dp(24),0,dp(24),0);
        EditText load = new EditText(this); load.setHint("Carga (kg)"); load.setSingleLine(); load.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); load.setText(prefs.getString(valueKey(e,"load"),"")); fields.addView(load);
        EditText reps = new EditText(this); reps.setHint("Repetições"); reps.setSingleLine(); reps.setInputType(InputType.TYPE_CLASS_NUMBER); reps.setText(prefs.getString(valueKey(e,"reps"),"")); fields.addView(reps);
        new AlertDialog.Builder(this).setTitle("Dados do exercício").setView(fields).setPositiveButton("Salvar", (d,w) -> {
            prefs.edit().putString(valueKey(e,"load"),load.getText().toString().trim()).putString(valueKey(e,"reps"),reps.getText().toString().trim()).apply(); showExercise(e);
        }).setNegativeButton("Cancelar",null).show();
    }
    private void confirmFinish() {
        new AlertDialog.Builder(this).setMessage("Finalizar este treino? As séries registradas ficarão salvas.")
            .setPositiveButton("Finalizar",(d,w)->showSummary()).setNegativeButton("Voltar",null).show();
    }
    private void showSummary() {
        screen = "summary"; int completed = 0; for(int e=0;e<10;e++) if(done(e)==3) completed++;
        long end=System.currentTimeMillis(); prefs.edit().putLong("end_"+session,end).remove("active_"+workout).apply();
        LinearLayout c=column(); add(c,text("TREINO FINALIZADO",15,WHITE,true),10);
        add(c,text(completed+" de 10 exercícios",12,ORANGE,true),8);
        add(c,text("Salvo no relógio",10,MUTED,false),12);
        add(c,button("INÍCIO",true,v->showHome()),0); list(c);
    }
    @Override public void onBackPressed() {
        if ("exercise".equals(screen)) showWorkout(); else if ("workout".equals(screen) || "summary".equals(screen)) showHome(); else super.onBackPressed();
    }
}
