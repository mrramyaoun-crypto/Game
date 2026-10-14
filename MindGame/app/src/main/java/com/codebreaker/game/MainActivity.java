package com.codebreaker.game;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* loaded from: classes2.dex */
public class MainActivity extends Activity {
    FrameLayout answerStage;
    Button assistBtn;
    FlowLayout candidateBox;
    FrameLayout candidateWindow;
    LinearLayout cluesBox;
    Button decipherBtn;
    Button deleteBtn;
    Button difficultyBtn;
    Button enterBtn;
    LinearLayout machine;
    Button newCipherBtn;
    Puzzle puzzle;
    Button resetBtn;
    Button resolveBtn;
    TextView resultStamp;
    LinearLayout root;
    LinearLayout workingSlots;
    static final int PAPER = Color.rgb(214, 179, 103);
    static final int PAPER_DARK = Color.rgb(137, 93, 48);
    static final int INK = Color.rgb(50, 33, 23);
    static final int FADED = Color.rgb(108, 74, 46);
    static final int WOOD = Color.rgb(52, 32, 22);
    static final int WOOD_DARK = Color.rgb(28, 18, 14);
    static final int BRASS = Color.rgb(164, 120, 53);
    static final int BRASS_LIGHT = Color.rgb(205, 163, 83);
    static final int IVORY = Color.rgb(229, 211, 166);
    static final int RED_INK = Color.rgb(113, 42, 31);
    final Random rnd = new Random();
    final ArrayList<String> ALL = new ArrayList<>();
    final ExecutorService puzzlePool = Executors.newSingleThreadExecutor();
    final HashSet<Character> eliminated = new HashSet<>();
    final ArrayList<String> candidates = new ArrayList<>();
    final HashSet<Integer> crossedCandidates = new HashSet<>();
    final ArrayList<TextView> clueCodeViews = new ArrayList<>();
    final ArrayList<Clue> visibleClues = new ArrayList<>();
    final Button[] digitKeys = new Button[10];
    final char[] answerSlots = {0, 0, 0, 0};
    int activeSlot = 0;
    String mode = "normal";
    boolean attempted = false;
    boolean assistUsed = false;
    boolean solved = false;
    int attemptsLeft = 3;
    final Runnable hideStampRunnable = new Runnable() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda13
        @Override // java.lang.Runnable
        public final void run() {
            MainActivity.this.m9lambda$new$21$comcodebreakergameMainActivity();
        }
    };

    static class Score {
        int right;
        int total;

        Score(int total, int right) {
            this.total = total;
            this.right = right;
        }
    }

    static class Clue {
        String code;
        int right;
        int total;

        Clue(String code, int total, int right) {
            this.code = code;
            this.total = total;
            this.right = right;
        }

        String shortText() {
            return (this.total == 1 && this.right == 1) ? "1 CORRECT · RIGHT PLACE" : (this.total == 1 && this.right == 0) ? "1 CORRECT · WRONG PLACE" : (this.total == 2 && this.right == 0) ? "2 CORRECT · BOTH MISPLACED" : "2 CORRECT · 1 RIGHT + 1 WRONG";
        }
    }

    static class Puzzle {
        ArrayList<Clue> clues;
        String secret;

        Puzzle(String secret, ArrayList<Clue> clues) {
            this.secret = secret;
            this.clues = clues;
        }
    }

    class FlowLayout extends ViewGroup {
        FlowLayout(Context c) {
            super(c);
        }

        @Override // android.view.View
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            int available = Math.max(0, (width - getPaddingLeft()) - getPaddingRight());
            int x = 0;
            int y = getPaddingTop();
            int lineH = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                int cw = child.getMeasuredWidth();
                int ch = child.getMeasuredHeight();
                if (x > 0 && x + cw > available) {
                    y += lineH;
                    x = 0;
                    lineH = 0;
                }
                x += cw;
                lineH = Math.max(lineH, ch);
            }
            int i2 = getPaddingBottom();
            int desired = Math.max(getSuggestedMinimumHeight(), y + i2 + lineH);
            setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(desired, heightMeasureSpec));
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int available = Math.max(0, ((r - l) - getPaddingLeft()) - getPaddingRight());
            int x = 0;
            int y = getPaddingTop();
            int lineH = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                int cw = child.getMeasuredWidth();
                int ch = child.getMeasuredHeight();
                if (x > 0 && x + cw > available) {
                    y += lineH;
                    x = 0;
                    lineH = 0;
                }
                int left = getPaddingLeft() + x;
                child.layout(left, y, left + cw, y + ch);
                x += cw;
                lineH = Math.max(lineH, ch);
            }
        }
    }

    class ScratchTextView extends TextView {
        final Paint scratchPaint;
        boolean scratched;

        ScratchTextView(Context c) {
            super(c);
            this.scratchPaint = new Paint(1);
            this.scratched = false;
            this.scratchPaint.setColor(Color.rgb(74, 45, 28));
            this.scratchPaint.setStrokeWidth(MainActivity.this.dp(2.1f));
            this.scratchPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        void setScratched(boolean value) {
            this.scratched = value;
            invalidate();
        }

        @Override // android.widget.TextView, android.view.View
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (this.scratched) {
                float left = getPaddingLeft();
                float right = getWidth() - getPaddingRight();
                float y = getHeight() * 0.5f;
                canvas.drawLine(left, y, right, y + MainActivity.this.dp(1.0f), this.scratchPaint);
                canvas.drawLine(left, y + MainActivity.this.dp(3.0f), right, y + MainActivity.this.dp(2.0f), this.scratchPaint);
            }
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(WOOD_DARK);
        getWindow().setNavigationBarColor(WOOD_DARK);
        getWindow().getDecorView().setSystemUiVisibility(0);
        getWindow().setSoftInputMode(48);
        genCodes("", new boolean[10]);
        buildUi();
        startPuzzle();
    }

    int dp(float v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }

    int statusBarHeight() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : dp(24.0f);
    }

    Typeface oldFace(boolean z) {
        return Typeface.create("serif-monospace", z ? Typeface.BOLD : Typeface.NORMAL);
    }

    GradientDrawable rounded(int fill, int stroke, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radius));
        if (stroke != 0) {
            g.setStroke(dp(1.0f), stroke);
        }
        return g;
    }

    GradientDrawable splitShape(int fill, int stroke, boolean left) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setStroke(dp(1.0f), stroke);
        float r = dp(10.0f);
        if (left) {
            g.setCornerRadii(new float[]{r, r, 0.0f, 0.0f, 0.0f, 0.0f, r, r});
        } else {
            g.setCornerRadii(new float[]{0.0f, 0.0f, r, r, r, r, 0.0f, 0.0f});
        }
        return g;
    }

    TextView typeText(String text, int sp, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(sp);
        v.setTextColor(INK);
        v.setTypeface(oldFace(bold));
        v.setLetterSpacing(0.015f);
        return v;
    }

    Button oldButton(String text) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        b.setTextSize(11.0f);
        b.setTypeface(oldFace(true));
        b.setTextColor(Color.rgb(246, 224, 177));
        b.setMinHeight(0);
        b.setMinWidth(0);
        b.setPadding(dp(6.0f), 0, dp(6.0f), 0);
        b.setBackground(rounded(Color.rgb(91, 55, 34), BRASS, 8.0f));
        b.setElevation(dp(2.0f));
        return b;
    }

    class AgedPaperDrawable extends Drawable {
        final boolean card;
        final Paint p = new Paint(1);
        final float radius;

        AgedPaperDrawable(float radiusDp, boolean card) {
            this.radius = MainActivity.this.dp(radiusDp);
            this.card = card;
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas c) {
            int i;
            int i2;
            int i3;
            RectF r = new RectF(getBounds());
            this.p.setStyle(Paint.Style.FILL);
            this.p.setColor(this.card ? Color.rgb(228, 194, 124) : MainActivity.PAPER);
            c.drawRoundRect(r, this.radius, this.radius, this.p);
            this.p.setStyle(Paint.Style.STROKE);
            this.p.setStrokeWidth(MainActivity.this.dp(this.card ? 1.1f : 2.0f));
            Paint paint = this.p;
            if (this.card) {
                i = 99;
                i2 = 53;
                i3 = 143;
            } else {
                i = 62;
                i2 = 35;
                i3 = 97;
            }
            paint.setColor(Color.rgb(i3, i, i2));
            c.drawRoundRect(new RectF(r.left + 1.0f, r.top + 1.0f, r.right - 1.0f, r.bottom - 1.0f), this.radius, this.radius, this.p);
            int w = Math.max(1, getBounds().width());
            int h = Math.max(1, getBounds().height());
            this.p.setStyle(Paint.Style.FILL);
            this.p.setColor(Color.argb(this.card ? 32 : 38, 67, 39, 18));
            int i4 = 0;
            while (true) {
                if (i4 >= (this.card ? 24 : 78)) {
                    break;
                }
                float x = ((i4 * 71) + 31) % w;
                float y = ((i4 * 43) + 19) % h;
                float rr = i4 % 5 == 0 ? 1.5f : 0.7f;
                c.drawCircle(r.left + x, r.top + y, MainActivity.this.dp(rr), this.p);
                i4++;
            }
            this.p.setColor(Color.argb(this.card ? 29 : 42, 91, 49, 21));
            int i5 = 0;
            while (true) {
                if (i5 >= (this.card ? 6 : 13)) {
                    break;
                }
                float x1 = r.left + (((i5 * 89) + 14) % w);
                float y1 = r.top + (((i5 * 51) + 9) % h);
                c.drawRect(x1, y1, Math.min(r.right, MainActivity.this.dp((i5 % 3) + 7) + x1), Math.min(r.bottom, MainActivity.this.dp(0.8f) + y1), this.p);
                i5++;
            }
            this.p.setColor(Color.argb(this.card ? 44 : 58, 70, 34, 14));
            c.drawRect(r.left, r.top, r.right, MainActivity.this.dp(this.card ? 2.0f : 4.0f) + r.top, this.p);
            c.drawRect(r.left, r.bottom - MainActivity.this.dp(this.card ? 2.0f : 4.0f), r.right, r.bottom, this.p);
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter cf) {
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    class WoodDrawable extends Drawable {
        final Paint p = new Paint(1);

        WoodDrawable() {
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas c) {
            RectF r = new RectF(getBounds());
            this.p.setStyle(Paint.Style.FILL);
            this.p.setColor(MainActivity.WOOD);
            c.drawRoundRect(r, MainActivity.this.dp(15.0f), MainActivity.this.dp(15.0f), this.p);
            int w = Math.max(1, getBounds().width());
            int h = Math.max(1, getBounds().height());
            this.p.setColor(Color.argb(38, 223, 173, 91));
            for (int i = 0; i < 52; i++) {
                float x = r.left + (((i * 53) + 17) % w);
                float y = r.top + (((i * 37) + 13) % h);
                if (i % 4 == 0) {
                    c.drawRect(x, y, Math.min(r.right, MainActivity.this.dp((i % 7) + 8) + x), Math.min(r.bottom, MainActivity.this.dp(0.8f) + y), this.p);
                } else {
                    c.drawCircle(x, y, MainActivity.this.dp(0.55f), this.p);
                }
            }
            this.p.setStyle(Paint.Style.STROKE);
            this.p.setStrokeWidth(MainActivity.this.dp(2.0f));
            this.p.setColor(MainActivity.BRASS);
            c.drawRoundRect(new RectF(r.left + 1.0f, r.top + 1.0f, r.right - 1.0f, r.bottom - 1.0f), MainActivity.this.dp(15.0f), MainActivity.this.dp(15.0f), this.p);
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter cf) {
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    class MechanicalKeyDrawable extends Drawable {
        final boolean eliminatedKey;
        final Paint p = new Paint(1);
        final boolean pressed;

        MechanicalKeyDrawable(boolean eliminatedKey, boolean pressed) {
            this.eliminatedKey = eliminatedKey;
            this.pressed = pressed;
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas c) {
            int i;
            int i2;
            int i3;
            RectF r = new RectF(getBounds());
            float faceInset = MainActivity.this.dp(this.pressed ? 5.0f : 3.0f);
            this.p.setStyle(Paint.Style.FILL);
            Paint paint = this.p;
            if (this.pressed) {
                i = 23;
                i2 = 18;
                i3 = 31;
            } else {
                i = 28;
                i2 = 22;
                i3 = 40;
            }
            paint.setColor(Color.rgb(i3, i, i2));
            c.drawRoundRect(r, MainActivity.this.dp(6.0f), MainActivity.this.dp(6.0f), this.p);
            this.p.setStyle(Paint.Style.STROKE);
            this.p.setStrokeWidth(MainActivity.this.dp(2.0f));
            this.p.setColor(this.pressed ? Color.rgb(105, 72, 38) : MainActivity.BRASS);
            c.drawRoundRect(new RectF(r.left + 1.0f, r.top + 1.0f, r.right - 1.0f, r.bottom - 1.0f), MainActivity.this.dp(6.0f), MainActivity.this.dp(6.0f), this.p);
            RectF face = new RectF(r.left + faceInset, r.top + faceInset, r.right - faceInset, r.bottom - faceInset);
            this.p.setStyle(Paint.Style.FILL);
            this.p.setColor(this.eliminatedKey ? Color.rgb(106, 82, 62) : this.pressed ? Color.rgb(195, 169, 113) : MainActivity.IVORY);
            c.drawRoundRect(face, MainActivity.this.dp(4.0f), MainActivity.this.dp(4.0f), this.p);
            this.p.setStyle(Paint.Style.STROKE);
            this.p.setStrokeWidth(MainActivity.this.dp(1.0f));
            this.p.setColor(this.eliminatedKey ? Color.rgb(65, 43, 32) : Color.rgb(113, 80, 47));
            c.drawRoundRect(face, MainActivity.this.dp(4.0f), MainActivity.this.dp(4.0f), this.p);
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter cf) {
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    StateListDrawable keyBackground(boolean eliminatedKey) {
        StateListDrawable state = new StateListDrawable();
        state.addState(new int[]{android.R.attr.state_pressed}, new MechanicalKeyDrawable(eliminatedKey, true));
        state.addState(new int[0], new MechanicalKeyDrawable(eliminatedKey, false));
        return state;
    }

    void genCodes(String p, boolean[] used) {
        if (p.length() == 4) {
            this.ALL.add(p);
            return;
        }
        for (int i = 0; i < 10; i++) {
            if (!used[i]) {
                used[i] = true;
                genCodes(p + i, used);
                used[i] = false;
            }
        }
    }

    void buildUi() {
        this.root = new LinearLayout(this);
        this.root.setOrientation(LinearLayout.VERTICAL);
        this.root.setBackgroundColor(WOOD_DARK);
        this.root.setPadding(dp(8.0f), statusBarHeight() + dp(6.0f), dp(8.0f), dp(32.0f));
        setContentView(this.root, new ViewGroup.LayoutParams(-1, -1));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(dp(7.0f), dp(7.0f), dp(7.0f), dp(5.0f));
        linearLayout.setBackground(new AgedPaperDrawable(14.0f, false));
        this.root.addView(linearLayout, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout split = new LinearLayout(this);
        split.setGravity(17);
        this.newCipherBtn = oldButton("NEW CIPHER");
        this.assistBtn = oldButton("HINT");
        this.resetBtn = oldButton("RESET");
        this.resolveBtn = oldButton("RESOLVE");
        this.difficultyBtn = oldButton("+");
        this.newCipherBtn.setTextSize(9.0f);
        this.assistBtn.setTextSize(9.0f);
        this.resetBtn.setTextSize(9.0f);
        this.resolveBtn.setTextSize(9.0f);
        this.difficultyBtn.setTextSize(20.0f);
        Button[] topActions = {this.assistBtn, this.resetBtn, this.resolveBtn, this.newCipherBtn};
        for (Button b : topActions) {
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f);
            bp.setMargins(dp(1.0f), 0, dp(1.0f), 0);
            split.addView(b, bp);
        }
        LinearLayout.LayoutParams plusLp = new LinearLayout.LayoutParams(dp(38.0f), dp(42.0f));
        plusLp.setMargins(dp(1.0f), 0, 0, 0);
        split.addView(this.difficultyBtn, plusLp);
        linearLayout.addView(split, new LinearLayout.LayoutParams(-1, -2));
        this.newCipherBtn.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda18
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m2lambda$buildUi$0$comcodebreakergameMainActivity(view);
            }
        });
        this.assistBtn.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda19
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m3lambda$buildUi$1$comcodebreakergameMainActivity(view);
            }
        });
        this.resetBtn.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda20
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m4lambda$buildUi$2$comcodebreakergameMainActivity(view);
            }
        });
        this.resolveBtn.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda21
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m5lambda$buildUi$3$comcodebreakergameMainActivity(view);
            }
        });
        this.difficultyBtn.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m6lambda$buildUi$4$comcodebreakergameMainActivity(view);
            }
        });
        this.cluesBox = new LinearLayout(this);
        this.cluesBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, dp(11.0f), 0, 0);
        linearLayout.addView(this.cluesBox, cp);
        this.machine = new LinearLayout(this);
        this.machine.setOrientation(LinearLayout.VERTICAL);
        this.machine.setPadding(dp(8.0f), dp(6.0f), dp(8.0f), dp(8.0f));
        this.machine.setBackground(new WoodDrawable());
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(-1, 0, 1.0f);
        mp.setMargins(0, dp(6.0f), 0, 0);
        this.root.addView(this.machine, mp);
        this.answerStage = new FrameLayout(this);
        this.workingSlots = new LinearLayout(this);
        this.workingSlots.setGravity(17);
        this.answerStage.addView(this.workingSlots, new FrameLayout.LayoutParams(-1, dp(58.0f), 17));
        this.machine.addView(this.answerStage, new LinearLayout.LayoutParams(-1, dp(60.0f)));
        FrameLayout messageArea = new FrameLayout(this);
        this.resultStamp = typeText("", 13, true);
        this.resultStamp.setGravity(17);
        this.resultStamp.setTextColor(RED_INK);
        this.resultStamp.setPadding(dp(12.0f), dp(3.0f), dp(12.0f), dp(3.0f));
        this.resultStamp.setBackground(new AgedPaperDrawable(7.0f, true));
        this.resultStamp.setVisibility(View.GONE);
        messageArea.addView(this.resultStamp, new FrameLayout.LayoutParams(-2, dp(34.0f), 17));
        this.machine.addView(messageArea, new LinearLayout.LayoutParams(-1, dp(35.0f)));
        this.decipherBtn = oldButton("DECIPHER");
        this.decipherBtn.setTextSize(15.0f);
        this.decipherBtn.setTextColor(INK);
        this.decipherBtn.setBackground(new MechanicalKeyDrawable(false, false));
        LinearLayout.LayoutParams dcp = new LinearLayout.LayoutParams(-1, dp(46.0f));
        dcp.setMargins(dp(2.0f), 0, dp(2.0f), dp(5.0f));
        this.machine.addView(this.decipherBtn, dcp);
        this.decipherBtn.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m7lambda$buildUi$5$comcodebreakergameMainActivity(view);
            }
        });
        this.candidateWindow = new FrameLayout(this);
        this.candidateWindow.setBackground(new AgedPaperDrawable(8.0f, true));
        this.candidateWindow.setClipChildren(true);
        this.candidateBox = new FlowLayout(this);
        this.candidateBox.setPadding(dp(10.0f), dp(7.0f), dp(10.0f), dp(7.0f));
        this.candidateBox.setMinimumHeight(dp(150.0f));
        this.candidateWindow.addView(this.candidateBox, new FrameLayout.LayoutParams(-1, -2, 51));
        LinearLayout.LayoutParams csp = new LinearLayout.LayoutParams(-1, 0, 1.0f);
        csp.setMargins(0, 0, 0, dp(8.0f));
        this.machine.addView(this.candidateWindow, csp);
        addKeyboard();
        renderWorking();
        renderCandidates();
    }

    /* renamed from: lambda$buildUi$0$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m2lambda$buildUi$0$comcodebreakergameMainActivity(View v) {
        pressAnim(v);
        startPuzzle();
    }

    /* renamed from: lambda$buildUi$1$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m3lambda$buildUi$1$comcodebreakergameMainActivity(View v) {
        pressAnim(v);
        useAssist();
    }

    /* renamed from: lambda$buildUi$2$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m4lambda$buildUi$2$comcodebreakergameMainActivity(View v) {
        pressAnim(v);
        resetBoard();
    }

    /* renamed from: lambda$buildUi$3$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m5lambda$buildUi$3$comcodebreakergameMainActivity(View v) {
        pressAnim(v);
        revealSolution();
    }

    /* renamed from: lambda$buildUi$4$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m6lambda$buildUi$4$comcodebreakergameMainActivity(View v) {
        pressAnim(v);
        showDifficultyPopup();
    }

    /* renamed from: lambda$buildUi$5$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m7lambda$buildUi$5$comcodebreakergameMainActivity(View v) {
        pressAnim(v);
        checkWorking();
    }

    Button mechanicalKey(String text, int textSize) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        b.setTextSize(textSize);
        b.setTypeface(oldFace(true));
        b.setTextColor(INK);
        b.setMinHeight(0);
        b.setMinWidth(0);
        b.setPadding(dp(2.0f), 0, dp(2.0f), 0);
        b.setBackground(keyBackground(false));
        b.setElevation(dp(3.0f));
        return b;
    }

    void addKeyboard() {
        LinearLayout keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setGravity(80);
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(-1, dp(208.0f));
        klp.setMargins(dp(2.0f), 0, dp(2.0f), dp(6.0f));
        this.machine.addView(keyboard, klp);
        addNumberRow(keyboard, new int[]{1, 2, 3});
        addNumberRow(keyboard, new int[]{4, 5, 6});
        addNumberRow(keyboard, new int[]{7, 8, 9});
        Button zero = numberKey(0);
        this.digitKeys[0] = zero;
        this.deleteBtn = mechanicalKey("DELETE", 11);
        this.enterBtn = mechanicalKey("ENTER", 12);
        addControlRow(keyboard, new Button[]{this.deleteBtn, zero, this.enterBtn});
        this.deleteBtn.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m0lambda$addKeyboard$6$comcodebreakergameMainActivity(view);
            }
        });
        this.enterBtn.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda12
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m1lambda$addKeyboard$7$comcodebreakergameMainActivity(view);
            }
        });
    }

    /* renamed from: lambda$addKeyboard$6$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m0lambda$addKeyboard$6$comcodebreakergameMainActivity(View v) {
        pressAnim(v);
        backspace();
    }

    /* renamed from: lambda$addKeyboard$7$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m1lambda$addKeyboard$7$comcodebreakergameMainActivity(View v) {
        pressAnim(v);
        saveCandidate();
    }

    void addControlRow(LinearLayout linearLayout, Button[] buttons) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(17);
        for (Button b : buttons) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(50.0f), 1.0f);
            lp.setMargins(dp(4.0f), dp(1.0f), dp(4.0f), dp(1.0f));
            row.addView(b, lp);
        }
        linearLayout.addView(row, new LinearLayout.LayoutParams(-1, dp(52.0f)));
    }

    void addNumberRow(LinearLayout linearLayout, int[] digits) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(17);
        for (int d : digits) {
            Button key = numberKey(d);
            this.digitKeys[d] = key;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(50.0f), 1.0f);
            lp.setMargins(dp(4.0f), dp(1.0f), dp(4.0f), dp(1.0f));
            row.addView(key, lp);
        }
        linearLayout.addView(row, new LinearLayout.LayoutParams(-1, dp(52.0f)));
    }

    Button numberKey(int digit) {
        final char d = (char) (digit + 48);
        Button key = mechanicalKey(String.valueOf(d), 21);
        key.setBackground(keyBackground(this.eliminated.contains(Character.valueOf(d))));
        key.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m10lambda$numberKey$8$comcodebreakergameMainActivity(d, view);
            }
        });
        key.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda8
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return MainActivity.this.m11lambda$numberKey$9$comcodebreakergameMainActivity(d, view);
            }
        });
        return key;
    }

    /* renamed from: lambda$numberKey$8$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m10lambda$numberKey$8$comcodebreakergameMainActivity(char d, View v) {
        pressAnim(v);
        v.performHapticFeedback(3);
        this.answerSlots[this.activeSlot] = d;
        advanceActiveSlot();
        renderWorking();
    }

    /* renamed from: lambda$numberKey$9$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ boolean m11lambda$numberKey$9$comcodebreakergameMainActivity(char d, View v) {
        v.performHapticFeedback(0);
        toggleEliminated(d);
        return true;
    }

    void advanceActiveSlot() {
        int i = this.activeSlot;
        do {
            i++;
            if (i >= 4) {
                for (int i2 = 0; i2 < this.activeSlot; i2++) {
                    if (this.answerSlots[i2] == 0) {
                        this.activeSlot = i2;
                        return;
                    }
                }
                return;
            }
        } while (this.answerSlots[i] != 0);
        this.activeSlot = i;
    }

    void pressAnim(final View v) {
        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(45L).withEndAction(new Runnable() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(65L).start();
            }
        }).start();
    }

    void showDifficultyPopup() {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(dp(7.0f), dp(7.0f), dp(7.0f), dp(7.0f));
        menu.setBackground(new AgedPaperDrawable(10.0f, true));
        final PopupWindow popup = new PopupWindow((View) menu, dp(172.0f), -2, true);
        String[] choices = {"EASY", "NORMAL", "HARD"};
        for (final String choice : choices) {
            Button b = oldButton(choice);
            b.setTextColor(INK);
            b.setBackground(rounded(Color.rgb(221, 188, 122), PAPER_DARK, 7.0f));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(37.0f));
            p.setMargins(0, dp(2.0f), 0, dp(2.0f));
            menu.addView(b, p);
            b.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda16
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.m14lambda$showDifficultyPopup$11$comcodebreakergameMainActivity(popup, choice, view);
                }
            });
        }
        popup.setBackgroundDrawable(rounded(PAPER, PAPER_DARK, 10.0f));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(8.0f));
        popup.showAsDropDown(this.difficultyBtn, -dp(124.0f), dp(3.0f));
    }

    /* renamed from: lambda$showDifficultyPopup$11$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m14lambda$showDifficultyPopup$11$comcodebreakergameMainActivity(PopupWindow popup, String choice, View v) {
        popup.dismiss();
        this.mode = choice.toLowerCase();
        startPuzzle();
    }

    void revealSolution() {
        if (this.puzzle == null) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            this.answerSlots[i] = this.puzzle.secret.charAt(i);
        }
        this.activeSlot = 0;
        renderWorking();
        flashStamp("CIPHER REVEALED", false);
    }

    SpannableString styledDigits(String value, boolean wholeCross) {
        SpannableString s = new SpannableString(value);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (this.eliminated.contains(Character.valueOf(c))) {
                s.setSpan(new StrikethroughSpan(), i, i + 1, 33);
                s.setSpan(new ForegroundColorSpan(Color.rgb(126, 82, 58)), i, i + 1, 33);
            }
        }
        if (wholeCross && value.length() > 0) {
            s.setSpan(new StrikethroughSpan(), 0, value.length(), 33);
        }
        return s;
    }

    void renderWorking() {
        this.workingSlots.removeAllViews();
        int i = 0;
        while (i < 4) {
            final int slotIndex = i;
            TextView slot = typeText("·", 36, true);
            slot.setGravity(17);
            slot.setTextColor(i == this.activeSlot ? BRASS_LIGHT : Color.rgb(230, 205, 151));
            slot.setBackgroundColor(0);
            if (this.answerSlots[i] != 0) {
                slot.setText(styledDigits(String.valueOf(this.answerSlots[i]), false));
                if (i == this.activeSlot) {
                    slot.setTextColor(BRASS_LIGHT);
                }
            }
            slot.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.m13lambda$renderWorking$12$comcodebreakergameMainActivity(slotIndex, view);
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(68.0f), dp(56.0f));
            lp.setMargins(dp(5.0f), 0, dp(5.0f), 0);
            this.workingSlots.addView(slot, lp);
            i++;
        }
    }

    /* renamed from: lambda$renderWorking$12$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m13lambda$renderWorking$12$comcodebreakergameMainActivity(int slotIndex, View v) {
        this.activeSlot = slotIndex;
        renderWorking();
    }

    void renderCandidates() {
        this.candidateBox.removeAllViews();
        if (this.candidates.isEmpty()) {
            TextView empty = typeText("·  ·  ·  ·", 18, false);
            empty.setTextColor(Color.rgb(143, 102, 64));
            empty.setGravity(19);
            empty.setPadding(dp(4.0f), 0, dp(4.0f), 0);
            this.candidateBox.addView(empty, new ViewGroup.LayoutParams(-2, dp(40.0f)));
            return;
        }
        for (int idx = 0; idx < this.candidates.size(); idx++) {
            final int candidateIndex = idx;
            String candidate = this.candidates.get(idx);
            final ScratchTextView item = new ScratchTextView(this);
            item.setTextSize(20.0f);
            item.setTypeface(oldFace(true));
            item.setTextColor(INK);
            item.setGravity(16);
            item.setPadding(dp(3.0f), 0, dp(10.0f), 0);
            item.setText(styledDigits(candidate + ",", false));
            item.setScratched(this.crossedCandidates.contains(Integer.valueOf(candidateIndex)));
            item.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda11
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.m12lambda$renderCandidates$13$comcodebreakergameMainActivity(candidateIndex, item, view);
                }
            });
            this.candidateBox.addView(item, new ViewGroup.LayoutParams(-2, dp(42.0f)));
        }
    }

    /* renamed from: lambda$renderCandidates$13$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m12lambda$renderCandidates$13$comcodebreakergameMainActivity(int candidateIndex, ScratchTextView item, View v) {
        if (this.crossedCandidates.contains(Integer.valueOf(candidateIndex))) {
            this.crossedCandidates.remove(Integer.valueOf(candidateIndex));
        } else {
            this.crossedCandidates.add(Integer.valueOf(candidateIndex));
        }
        item.setScratched(this.crossedCandidates.contains(Integer.valueOf(candidateIndex)));
    }

    void saveCandidate() {
        String value = currentPartial();
        if (value.length() == 0) {
            flashStamp("NO CIPHER", false);
            return;
        }
        this.candidates.add(value);
        clearAnswerSlots();
        renderWorking();
        renderCandidates();
    }

    String currentPartial() {
        StringBuilder b = new StringBuilder();
        for (char c : this.answerSlots) {
            if (c != 0) {
                b.append(c);
            }
        }
        return b.toString();
    }

    String currentGuess() {
        for (char c : this.answerSlots) {
            if (c == 0) {
                return null;
            }
        }
        return new String(this.answerSlots);
    }

    void clearAnswerSlots() {
        Arrays.fill(this.answerSlots, (char) 0);
        this.activeSlot = 0;
    }

    void backspace() {
        if (this.answerSlots[this.activeSlot] != 0) {
            this.answerSlots[this.activeSlot] = 0;
        } else {
            int i = this.activeSlot - 1;
            while (true) {
                if (i < 0) {
                    break;
                }
                if (this.answerSlots[i] == 0) {
                    i--;
                } else {
                    this.answerSlots[i] = 0;
                    this.activeSlot = i;
                    break;
                }
            }
        }
        renderWorking();
    }

    void toggleEliminated(char d) {
        if (this.eliminated.contains(Character.valueOf(d))) {
            this.eliminated.remove(Character.valueOf(d));
        } else {
            this.eliminated.add(Character.valueOf(d));
        }
        refreshAllMarks();
    }

    void refreshAllMarks() {
        for (int d = 0; d <= 9; d++) {
            Button k = this.digitKeys[d];
            if (k != null) {
                char c = (char) (d + 48);
                boolean off = this.eliminated.contains(Character.valueOf(c));
                k.setBackground(keyBackground(off));
                k.setTextColor(off ? Color.rgb(183, 149, 112) : INK);
                k.setText(styledDigits(String.valueOf(c), false));
            }
        }
        for (int i = 0; i < this.clueCodeViews.size() && i < this.visibleClues.size(); i++) {
            this.clueCodeViews.get(i).setText(styledDigits(this.visibleClues.get(i).code, false));
        }
        renderWorking();
        renderCandidates();
    }

    LinearLayout makeClueCard(Clue c, boolean wide) {
        this.visibleClues.add(c);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(17);
        card.setPadding(dp(5.0f), dp(6.0f), dp(5.0f), dp(4.0f));
        card.setBackground(new AgedPaperDrawable(8.0f, true));
        TextView code = typeText("", wide ? 30 : 34, true);
        code.setGravity(17);
        code.setIncludeFontPadding(false);
        code.setText(styledDigits(c.code, false));
        code.setLetterSpacing(0.1f);
        code.setTextScaleX(wide ? 1.0f : 1.18f);
        if (wide) code.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        this.clueCodeViews.add(code);
        card.addView(code, new LinearLayout.LayoutParams(-1, 0, 1.35f));
        int clueLen = c.shortText().length();
        int ruleSize = wide ? clueLen > 26 ? 9 : 11 : clueLen > 26 ? 7 : 8;
        TextView rule = typeText(c.shortText(), ruleSize, true);
        rule.setTextColor(FADED);
        rule.setGravity(17);
        rule.setSingleLine(true);
        card.addView(rule, new LinearLayout.LayoutParams(-1, 0, 0.65f));
        return card;
    }

    void renderPuzzle() {
        this.cluesBox.removeAllViews();
        this.clueCodeViews.clear();
        this.visibleClues.clear();
        for (int rowIndex = 0; rowIndex < 2; rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(17);
            int col = 0;
            while (col < 2) {
                int clueIndex = (rowIndex * 2) + col;
                Clue c = this.puzzle.clues.get(clueIndex);
                LinearLayout card = makeClueCard(c, false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(74.0f), 1.0f);
                lp.setMargins(col == 0 ? 0 : dp(2.0f), 0, col == 0 ? dp(2.0f) : 0, dp(4.0f));
                row.addView(card, lp);
                col++;
            }
            this.cluesBox.addView(row, new LinearLayout.LayoutParams(-1, dp(78.0f)));
        }
        LinearLayout finalCard = makeClueCard(this.puzzle.clues.get(4), true);
        LinearLayout.LayoutParams finalLp = new LinearLayout.LayoutParams(-1, dp(70.0f));
        finalLp.setMargins(0, 0, 0, dp(2.0f));
        this.cluesBox.addView(finalCard, finalLp);
    }

    void resetBoard() {
        clearAnswerSlots();
        this.eliminated.clear();
        this.candidates.clear();
        this.crossedCandidates.clear();
        this.solved = false;
        this.attempted = false;
        hideStamp();
        refreshAllMarks();
    }

    void startPuzzle() {
        this.newCipherBtn.setEnabled(false);
        this.newCipherBtn.setText("...");
        this.difficultyBtn.setEnabled(false);
        if (this.assistBtn != null) {
            this.assistBtn.setEnabled(false);
        }
        if (this.resetBtn != null) {
            this.resetBtn.setEnabled(false);
        }
        if (this.resolveBtn != null) {
            this.resolveBtn.setEnabled(false);
        }
        if (this.decipherBtn != null) {
            this.decipherBtn.setEnabled(false);
        }
        clearAnswerSlots();
        this.eliminated.clear();
        this.candidates.clear();
        this.crossedCandidates.clear();
        this.attempted = false;
        this.assistUsed = false;
        this.solved = false;
        this.attemptsLeft = 3;
        hideStamp();
        refreshAllMarks();
        final String requestedMode = this.mode;
        this.puzzlePool.submit(new Runnable() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda15
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m19lambda$startPuzzle$15$comcodebreakergameMainActivity(requestedMode);
            }
        });
    }

    /* renamed from: lambda$startPuzzle$15$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m19lambda$startPuzzle$15$comcodebreakergameMainActivity(String requestedMode) {
        final Puzzle p = generate(requestedMode);
        runOnUiThread(new Runnable() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m18lambda$startPuzzle$14$comcodebreakergameMainActivity(p);
            }
        });
    }

    /* renamed from: lambda$startPuzzle$14$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m18lambda$startPuzzle$14$comcodebreakergameMainActivity(Puzzle p) {
        this.puzzle = p;
        renderPuzzle();
        renderWorking();
        renderCandidates();
        refreshAllMarks();
        this.newCipherBtn.setText("NEW CIPHER");
        this.newCipherBtn.setEnabled(true);
        this.difficultyBtn.setEnabled(true);
        this.assistBtn.setEnabled(true);
        this.resetBtn.setEnabled(true);
        this.resolveBtn.setEnabled(true);
        this.assistBtn.setText("HINT");
        this.decipherBtn.setEnabled(true);
    }

    void useAssist() {
        if (this.puzzle == null || this.assistUsed) {
            return;
        }
        if (this.mode.equals("hard") && !this.attempted) {
            flashStamp("DECIPHER ONCE FIRST", false);
            return;
        }
        ArrayList<Character> falseDigits = new ArrayList<>();
        for (char d = '0'; d <= '9'; d = (char) (d + 1)) {
            if (this.puzzle.secret.indexOf(d) < 0 && !this.eliminated.contains(Character.valueOf(d))) {
                falseDigits.add(Character.valueOf(d));
            }
        }
        Collections.shuffle(falseDigits, this.rnd);
        int n = this.mode.equals("easy") ? Math.min(2, falseDigits.size()) : Math.min(1, falseDigits.size());
        for (int i = 0; i < n; i++) {
            this.eliminated.add(falseDigits.get(i));
        }
        this.assistUsed = true;
        this.assistBtn.setText("HINT");
        this.assistBtn.setEnabled(false);
        refreshAllMarks();
    }

    void checkWorking() {
        if (this.puzzle == null || this.solved) {
            return;
        }
        String guess = currentGuess();
        if (guess == null) {
            flashStamp("4 DIGITS REQUIRED", false);
            return;
        }
        HashSet<Character> unique = new HashSet<>();
        for (char c : this.answerSlots) {
            unique.add(Character.valueOf(c));
        }
        if (unique.size() != 4) {
            flashStamp("NO REPEATS", false);
            return;
        }
        this.attempted = true;
        if (!guess.equals(this.puzzle.secret)) {
            this.attemptsLeft = Math.max(0, this.attemptsLeft - 1);
            if (this.attemptsLeft == 0) {
                this.decipherBtn.setEnabled(false);
            }
            showRejectedPopup();
            return;
        }
        this.solved = true;
        this.decipherBtn.setEnabled(false);
        showSolvedPopup();
    }

    void showRejectedPopup() {
        String remaining;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(17);
        linearLayout.setPadding(dp(22.0f), dp(20.0f), dp(22.0f), dp(18.0f));
        linearLayout.setBackground(new AgedPaperDrawable(12.0f, true));
        TextView title = typeText("CIPHER REJECTED", 27, true);
        title.setGravity(17);
        title.setTextColor(RED_INK);
        linearLayout.addView(title, new LinearLayout.LayoutParams(-1, dp(50.0f)));
        if (this.attemptsLeft == 0) {
            remaining = "NO ATTEMPTS REMAINING";
        } else {
            remaining = this.attemptsLeft == 1 ? "1 ATTEMPT REMAINING" : this.attemptsLeft + " ATTEMPTS REMAINING";
        }
        TextView info = typeText(remaining, 15, true);
        info.setGravity(17);
        info.setTextColor(INK);
        linearLayout.addView(info, new LinearLayout.LayoutParams(-1, dp(42.0f)));
        final PopupWindow popup = new PopupWindow((View) linearLayout, dp(310.0f), dp(218.0f), true);
        popup.setBackgroundDrawable(rounded(PAPER, PAPER_DARK, 12.0f));
        popup.setOutsideTouchable(false);
        popup.setElevation(dp(12.0f));
        if (this.attemptsLeft > 0) {
            Button again = oldButton("TRY AGAIN");
            again.setTextSize(12.0f);
            again.setTextColor(INK);
            again.setBackground(new MechanicalKeyDrawable(false, false));
            LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(-1, dp(46.0f));
            alp.setMargins(0, dp(12.0f), 0, 0);
            linearLayout.addView(again, alp);
            again.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda4
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    popup.dismiss();
                }
            });
        } else {
            LinearLayout actions = new LinearLayout(this);
            actions.setGravity(17);
            Button next = oldButton("NEW CIPHER");
            next.setTextSize(11.0f);
            next.setTextColor(INK);
            next.setBackground(new MechanicalKeyDrawable(false, false));
            Button reveal = oldButton("RESOLVE");
            reveal.setTextSize(11.0f);
            reveal.setTextColor(INK);
            reveal.setBackground(new MechanicalKeyDrawable(false, false));
            LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, dp(46.0f), 1.0f);
            left.setMargins(0, dp(12.0f), dp(4.0f), 0);
            LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, dp(46.0f), 1.0f);
            right.setMargins(dp(4.0f), dp(12.0f), 0, 0);
            actions.addView(next, left);
            actions.addView(reveal, right);
            linearLayout.addView(actions, new LinearLayout.LayoutParams(-1, -2));
            next.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda5
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.m15lambda$showRejectedPopup$17$comcodebreakergameMainActivity(popup, view);
                }
            });
            reveal.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda6
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.m16lambda$showRejectedPopup$18$comcodebreakergameMainActivity(popup, view);
                }
            });
        }
        popup.showAtLocation(this.root, 17, 0, 0);
    }

    /* renamed from: lambda$showRejectedPopup$17$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m15lambda$showRejectedPopup$17$comcodebreakergameMainActivity(PopupWindow popup, View v) {
        popup.dismiss();
        startPuzzle();
    }

    /* renamed from: lambda$showRejectedPopup$18$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m16lambda$showRejectedPopup$18$comcodebreakergameMainActivity(PopupWindow popup, View v) {
        popup.dismiss();
        revealSolution();
    }

    void showSolvedPopup() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(17);
        panel.setPadding(dp(24.0f), dp(22.0f), dp(24.0f), dp(20.0f));
        panel.setBackground(new AgedPaperDrawable(12.0f, true));
        TextView title = typeText("CIPHER BROKEN", 27, true);
        title.setGravity(17);
        title.setTextColor(RED_INK);
        panel.addView(title, new LinearLayout.LayoutParams(-1, dp(48.0f)));
        TextView code = typeText(new String(this.answerSlots), 29, true);
        code.setGravity(17);
        code.setTextColor(INK);
        panel.addView(code, new LinearLayout.LayoutParams(-1, dp(44.0f)));
        Button next = oldButton("NEW CIPHER");
        next.setTextSize(12.0f);
        next.setTextColor(INK);
        next.setBackground(new MechanicalKeyDrawable(false, false));
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(-1, dp(44.0f));
        nlp.setMargins(0, dp(12.0f), 0, 0);
        panel.addView(next, nlp);
        final PopupWindow popup = new PopupWindow((View) panel, dp(310.0f), dp(205.0f), true);
        popup.setBackgroundDrawable(rounded(PAPER, PAPER_DARK, 12.0f));
        popup.setOutsideTouchable(false);
        popup.setElevation(dp(12.0f));
        next.setOnClickListener(new View.OnClickListener() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m17lambda$showSolvedPopup$19$comcodebreakergameMainActivity(popup, view);
            }
        });
        popup.showAtLocation(this.root, 17, 0, 0);
    }

    /* renamed from: lambda$showSolvedPopup$19$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m17lambda$showSolvedPopup$19$comcodebreakergameMainActivity(PopupWindow popup, View v) {
        popup.dismiss();
        startPuzzle();
    }

    void flashStamp(String text, boolean stay) {
        this.resultStamp.removeCallbacks(this.hideStampRunnable);
        this.resultStamp.animate().cancel();
        this.resultStamp.setText(text);
        this.resultStamp.setVisibility(View.VISIBLE);
        this.resultStamp.setAlpha(0.0f);
        this.resultStamp.animate().alpha(1.0f).setDuration(120L).start();
        if (!stay) {
            this.resultStamp.postDelayed(this.hideStampRunnable, 1100L);
        }
    }

    /* renamed from: lambda$new$21$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m9lambda$new$21$comcodebreakergameMainActivity() {
        if (this.resultStamp == null) {
            return;
        }
        this.resultStamp.animate().cancel();
        this.resultStamp.animate().alpha(0.0f).setDuration(400L).withEndAction(new Runnable() { // from class: com.codebreaker.game.MainActivity$$ExternalSyntheticLambda14
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m8lambda$new$20$comcodebreakergameMainActivity();
            }
        }).start();
    }

    /* renamed from: lambda$new$20$com-codebreaker-game-MainActivity, reason: not valid java name */
    /* synthetic */ void m8lambda$new$20$comcodebreakergameMainActivity() {
        this.resultStamp.setVisibility(View.GONE);
        this.resultStamp.setText("");
        this.resultStamp.setAlpha(1.0f);
    }

    void hideStamp() {
        if (this.resultStamp != null) {
            this.resultStamp.removeCallbacks(this.hideStampRunnable);
            this.resultStamp.animate().cancel();
            this.resultStamp.setVisibility(View.GONE);
            this.resultStamp.setText("");
            this.resultStamp.setAlpha(1.0f);
        }
    }

    Score score(String secret, String clue) {
        int total = 0;
        int right = 0;
        for (int i = 0; i < 4; i++) {
            if (secret.charAt(i) == clue.charAt(i)) {
                right++;
            }
            if (clue.indexOf(secret.charAt(i)) >= 0) {
                total++;
            }
        }
        return new Score(total, right);
    }

    boolean matches(String candidate, Clue c) {
        Score s = score(candidate, c.code);
        return s.total == c.total && s.right == c.right;
    }

    ArrayList<String> filter(List<String> in, List<Clue> clues) {
        ArrayList<String> out = new ArrayList<>();
        for (String s : in) {
            Iterator<Clue> it = clues.iterator();
            while (true) {
                if (!it.hasNext()) {
                    out.add(s);
                    break;
                }
                Clue c = it.next();
                if (!matches(s, c)) {
                    break;
                }
            }
        }
        return out;
    }

    boolean disjoint(String a, String b) {
        for (int i = 0; i < 4; i++) {
            if (b.indexOf(a.charAt(i)) >= 0) {
                return false;
            }
        }
        return true;
    }

    char fixedDigit(String secret, Clue c) {
        for (int i = 0; i < 4; i++) {
            if (secret.charAt(i) == c.code.charAt(i)) {
                return secret.charAt(i);
            }
        }
        return '?';
    }

    boolean containsExactlyOne(String code, char a, char b) {
        int n = code.indexOf(a) >= 0 ? 0 + 1 : 0;
        if (code.indexOf(b) >= 0) {
            n++;
        }
        return n == 1;
    }

    ArrayList<Clue> poolFor(String secret, int total, int right) {
        ArrayList<Clue> p = new ArrayList<>();
        Iterator<String> it = this.ALL.iterator();
        while (it.hasNext()) {
            String code = it.next();
            Score s = score(secret, code);
            if (s.total == total && s.right == right) {
                p.add(new Clue(code, total, right));
            }
        }
        Collections.shuffle(p, this.rnd);
        return p;
    }

    Clue chooseByCount(ArrayList<Clue> pool, List<String> candidates, int min, int max, Set<String> used) {
        Iterator<Clue> it = pool.iterator();
        while (it.hasNext()) {
            Clue c = it.next();
            if (!used.contains(c.code)) {
                int n = 0;
                for (String s : candidates) {
                    if (matches(s, c)) {
                        n++;
                    }
                }
                if (n >= min && n <= max) {
                    return c;
                }
            }
        }
        return null;
    }

    Puzzle generate(String str) {
        Clue clue;
        int i;
        int i2;
        int i3;
        char c;
        int i4;
        boolean z;
        int i5;
        Clue clue2;
        String str2 = str;
        while (true) {
            String str3 = this.ALL.get(this.rnd.nextInt(this.ALL.size()));
            ArrayList<Clue> arrayListPoolFor = poolFor(str3, 1, 1);
            if (arrayListPoolFor.size() >= 2) {
                Clue clue3 = arrayListPoolFor.get(this.rnd.nextInt(arrayListPoolFor.size()));
                Iterator<Clue> it = arrayListPoolFor.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        clue = null;
                        break;
                    }
                    Clue next = it.next();
                    if (disjoint(clue3.code, next.code)) {
                        clue = next;
                        break;
                    }
                }
                if (clue == null) {
                    continue;
                } else {
                    char cFixedDigit = fixedDigit(str3, clue3);
                    char cFixedDigit2 = fixedDigit(str3, clue);
                    ArrayList arrayList = new ArrayList(Arrays.asList(clue3, clue));
                    ArrayList<String> arrayListFilter = filter(this.ALL, arrayList);
                    Set<String> hashSet = new HashSet<>();
                    hashSet.add(clue3.code);
                    hashSet.add(clue.code);
                    int i6 = str2.equals("easy") ? 2 : 1;
                    boolean zEquals = str2.equals("easy");
                    boolean zEquals2 = str2.equals("hard");
                    if (str2.equals("easy")) {
                        i = 2;
                    } else {
                        i = str2.equals("hard") ? 10 : 6;
                    }
                    int i7 = i;
                    if (str2.equals("easy")) {
                        i2 = 6;
                    } else {
                        i2 = str2.equals("hard") ? 18 : 12;
                    }
                    int i8 = i2;
                    int i9 = i6;
                    int i10 = str2.equals("hard") ? 3 : 2;
                    if (str2.equals("easy")) {
                        i3 = 3;
                    } else {
                        i3 = str2.equals("hard") ? 6 : 4;
                    }
                    ArrayList<Clue> arrayListPoolFor2 = poolFor(str3, i9, zEquals ? 1 : 0);
                    boolean z2 = zEquals2;
                    ArrayList<Clue> arrayListPoolFor3 = poolFor(str3, 2, z2 ? 1 : 0);
                    ArrayList<Clue> arrayListPoolFor4 = poolFor(str3, 2, 0);
                    Iterator<Clue> it2 = arrayListPoolFor2.iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            Clue next2 = it2.next();
                            if (containsExactlyOne(next2.code, cFixedDigit, cFixedDigit2)) {
                                int i11 = 0;
                                Iterator<String> it3 = arrayListFilter.iterator();
                                while (it3.hasNext()) {
                                    char c2 = cFixedDigit2;
                                    if (matches(it3.next(), next2)) {
                                        i11++;
                                    }
                                    cFixedDigit2 = c2;
                                }
                                c = cFixedDigit2;
                                i4 = i7;
                                if (i11 >= i4) {
                                    z = z2;
                                    i5 = i8;
                                    if (i11 <= i5) {
                                        clue2 = next2;
                                        break;
                                    }
                                } else {
                                    z = z2;
                                    i5 = i8;
                                }
                                i8 = i5;
                                z2 = z;
                                i7 = i4;
                                cFixedDigit2 = c;
                            }
                        } else {
                            c = cFixedDigit2;
                            i4 = i7;
                            z = z2;
                            i5 = i8;
                            clue2 = null;
                            break;
                        }
                    }
                    if (clue2 == null) {
                        continue;
                    } else {
                        arrayList.add(clue2);
                        hashSet.add(clue2.code);
                        ArrayList<String> arrayListFilter2 = filter(arrayListFilter, Collections.singletonList(clue2));
                        Clue clueChooseByCount = chooseByCount(arrayListPoolFor3, arrayListFilter2, i10, i3, hashSet);
                        if (clueChooseByCount == null) {
                            continue;
                        } else {
                            arrayList.add(clueChooseByCount);
                            hashSet.add(clueChooseByCount.code);
                            ArrayList<String> arrayListFilter3 = filter(arrayListFilter2, Collections.singletonList(clueChooseByCount));
                            char c3 = c;
                            Clue clue4 = clue;
                            boolean z3 = z;
                            Clue clue5 = clue2;
                            Clue clueChooseByCount2 = chooseByCount(arrayListPoolFor4, arrayListFilter3, 1, 1, hashSet);
                            if (clueChooseByCount2 == null) {
                                str2 = str;
                            } else {
                                arrayList.add(clueChooseByCount2);
                                ArrayList<String> arrayListFilter4 = filter(arrayListFilter3, Collections.singletonList(clueChooseByCount2));
                                if (arrayListFilter4.size() != 1) {
                                    str2 = str;
                                } else if (arrayListFilter4.get(0).equals(str3)) {
                                    Score score = score(str3, clue3.code);
                                    Score score2 = score(str3, clue4.code);
                                    Score score3 = score(str3, clue5.code);
                                    Score score4 = score(str3, clueChooseByCount.code);
                                    Score score5 = score(str3, clueChooseByCount2.code);
                                    if (score.total == 1 && score.right == 1 && score2.total == 1 && score2.right == 1 && disjoint(clue3.code, clue4.code) && score3.total == i9 && score3.right == (zEquals ? 1 : 0) && containsExactlyOne(clue5.code, cFixedDigit, c3) && score4.total == 2 && score4.right == (z3 ? 1 : 0) && score5.total == 2 && score5.right == 0) {
                                        return new Puzzle(str3, arrayList);
                                    }
                                    str2 = str;
                                } else {
                                    str2 = str;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.puzzlePool.shutdownNow();
        super.onDestroy();
    }
}
