package com.codebreaker.game;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/** Hosts the recovered v13 screen without replacing its controls or drawings. */
public class EnigmaActivity extends MainActivity {
    private SharedPreferences saved;
    private boolean first = true, restoring = false, generating = false, recorded = false, revealed = false;
    private long elapsed = 0, runningSince = 0;
    private int guesses = 0;
    private boolean foreground = false;

    @Override public void onCreate(Bundle b) {
        saved = getSharedPreferences("mindgame_v1", MODE_PRIVATE);
        super.onCreate(b);
    }
    @Override void buildUi() {
        super.buildUi();
        android.widget.LinearLayout actions = (android.widget.LinearLayout) assistBtn.getParent();
        android.widget.Button help = oldButton("RULES"); help.setTextSize(9);
        help.setOnClickListener(v -> com.ramy.mindgame.ui.RuleGuide.show(this,"enigma"));
        actions.addView(help,0,new android.widget.LinearLayout.LayoutParams(dp(46),dp(42)));
        candidateWindow.removeView(candidateBox);
        android.widget.ScrollView paper = new android.widget.ScrollView(this); paper.setFillViewport(true);
        paper.addView(candidateBox,new android.widget.ScrollView.LayoutParams(-1,-2));
        candidateWindow.addView(paper,new android.widget.FrameLayout.LayoutParams(-1,-1));
    }
    @Override protected void onResume() { super.onResume(); foreground = true; runningSince = SystemClock.elapsedRealtime(); }
    @Override protected void onPause() { elapsed = playTime(); foreground = false; persistState(); super.onPause(); }
    @Override protected void onSaveInstanceState(Bundle b) { persistState(); super.onSaveInstanceState(b); }
    private long playTime() { return elapsed + (foreground && !solved ? SystemClock.elapsedRealtime() - runningSince : 0); }

    @Override void startPuzzle() {
        if (first) { first = false; if (restoreState()) return; }
        generating = true; recorded = false; revealed = false; guesses = 0; elapsed = 0; runningSince = SystemClock.elapsedRealtime();
        super.startPuzzle();
    }
    @Override void m18lambda$startPuzzle$14$comcodebreakergameMainActivity(Puzzle p) {
        if (isFinishing() || isDestroyed()) return;
        super.m18lambda$startPuzzle$14$comcodebreakergameMainActivity(p);
        generating = false; runningSince = SystemClock.elapsedRealtime();
        try { JSONObject stat = new JSONObject(saved.getString("stat_enigma", "{}")); stat.put("played", stat.optInt("played") + 1); saved.edit().putString("stat_enigma", stat.toString()).apply(); } catch (Exception ignored) {}
        persistState();
    }
    @Override void renderWorking() { super.renderWorking(); persistState(); }
    @Override void renderCandidates() { super.renderCandidates(); persistState(); }
    @Override void m12lambda$renderCandidates$13$comcodebreakergameMainActivity(int i, ScratchTextView item, android.view.View v) { super.m12lambda$renderCandidates$13$comcodebreakergameMainActivity(i,item,v); persistState(); }
    @Override void revealSolution() { revealed = true; super.revealSolution(); persistState(); }
    @Override void checkWorking() {
        String g = currentGuess();
        if (!solved && g != null && g.chars().distinct().count() == 4) guesses++;
        super.checkWorking(); persistState();
    }
    @Override void showSolvedPopup() {
        if (!recorded && !revealed) {
            long time = elapsed + (foreground ? SystemClock.elapsedRealtime() - runningSince : 0);
            elapsed = time; recorded = true;
            try {
                JSONObject stat = new JSONObject(saved.getString("stat_enigma", "{}"));
                stat.put("wins",stat.optInt("wins")+1);
                int score = Math.max(100, 1000 - guesses*100 - (assistUsed ? 150 : 0));
                stat.put("high",Math.max(score,stat.optInt("high")));
                if (stat.optLong("best")==0 || time < stat.optLong("best")) stat.put("best",time);
                saved.edit().putString("stat_enigma",stat.toString()).apply();
            } catch (Exception ignored) {}
        }
        persistState(); super.showSolvedPopup();
    }
    private void persistState() {
        if (saved == null || puzzle == null || restoring || generating) return;
        try {
            JSONObject j = new JSONObject().put("secret",puzzle.secret).put("mode",mode).put("active",activeSlot).put("attempted",attempted).put("assist",assistUsed).put("solved",solved).put("left",attemptsLeft).put("elapsed",playTime()).put("recorded",recorded).put("revealed",revealed).put("guesses",guesses);
            JSONArray clues = new JSONArray(); for (Clue c : puzzle.clues) clues.put(new JSONObject().put("code",c.code).put("total",c.total).put("right",c.right)); j.put("clues",clues);
            JSONArray slots = new JSONArray(); for(char c:answerSlots) slots.put((int)c); j.put("slots",slots);
            JSONArray eliminatedDigits = new JSONArray(); for(char c:eliminated) eliminatedDigits.put((int)c); j.put("eliminated",eliminatedDigits);
            j.put("candidates",new JSONArray(candidates)); j.put("crossed",new JSONArray(crossedCandidates));
            saved.edit().putString("legacy_enigma_v13",j.toString()).apply();
        } catch(Exception ignored) {}
    }
    private boolean restoreState() {
        try {
            String raw = saved.getString("legacy_enigma_v13",null); if(raw==null) return false;
            JSONObject j = new JSONObject(raw); String secret = j.getString("secret"); if(secret.length()!=4 || secret.chars().distinct().count()!=4) return false;
            restoring = true;
            ArrayList<Clue> clues = new ArrayList<>(); JSONArray a = j.getJSONArray("clues"); for(int i=0;i<a.length();i++) { JSONObject c = a.getJSONObject(i); clues.add(new Clue(c.getString("code"),c.getInt("total"),c.getInt("right"))); }
            puzzle = new Puzzle(secret,clues); mode=j.getString("mode"); activeSlot=j.getInt("active"); attempted=j.getBoolean("attempted"); assistUsed=j.getBoolean("assist"); solved=j.getBoolean("solved"); attemptsLeft=j.getInt("left"); elapsed=j.optLong("elapsed"); recorded=j.optBoolean("recorded"); revealed=j.optBoolean("revealed"); guesses=j.optInt("guesses");
            a=j.getJSONArray("slots"); for(int i=0;i<4;i++) answerSlots[i]=(char)a.getInt(i);
            eliminated.clear(); a=j.getJSONArray("eliminated"); for(int i=0;i<a.length();i++) eliminated.add((char)a.getInt(i));
            candidates.clear(); a=j.getJSONArray("candidates"); for(int i=0;i<a.length();i++) candidates.add(a.getString(i));
            crossedCandidates.clear(); a=j.getJSONArray("crossed"); for(int i=0;i<a.length();i++) crossedCandidates.add(a.getInt(i));
            renderPuzzle(); renderWorking(); renderCandidates(); refreshAllMarks(); assistBtn.setEnabled(!assistUsed); decipherBtn.setEnabled(!solved && attemptsLeft>0); restoring=false; return true;
        } catch(Exception e) { restoring=false; return false; }
    }
}
