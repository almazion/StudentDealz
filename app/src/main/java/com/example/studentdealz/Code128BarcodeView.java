package com.example.studentdealz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class Code128BarcodeView extends View {

    private static final String[] CODE_128_PATTERNS = {
            "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312",
            "132212", "221213", "221312", "231212", "112232", "122132", "122231", "113222",
            "123122", "123221", "223211", "221132", "221231", "213212", "223112", "312131",
            "311222", "321122", "321221", "312212", "322112", "322211", "212123", "212321",
            "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313",
            "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121",
            "313121", "211331", "231131", "213113", "213311", "213131", "311123", "311321",
            "331121", "312113", "312311", "332111", "314111", "221411", "431111", "111224",
            "111422", "121124", "121421", "141122", "141221", "112214", "112412", "122114",
            "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111",
            "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112",
            "421211", "212141", "214121", "412121", "111143", "111341", "131141", "114113",
            "114311", "411113", "411311", "113141", "114131", "311141", "411131", "211412",
            "211214", "211232", "2331112"
    };

    private static final int START_CODE_B = 104;
    private static final int STOP_CODE = 106;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String barcodeValue = "";

    public Code128BarcodeView(Context context) {
        super(context);
        init();
    }

    public Code128BarcodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Code128BarcodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        setBackgroundColor(Color.WHITE);
    }

    public void setBarcodeValue(String barcodeValue) {
        this.barcodeValue = sanitize(barcodeValue);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean[] modules = encodeCode128B(barcodeValue);
        if (modules.length == 0) {
            return;
        }

        float availableWidth = getWidth() - getPaddingStart() - getPaddingEnd();
        float availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        float moduleWidth = availableWidth / modules.length;
        float left = getPaddingStart();
        float top = getPaddingTop();
        float bottom = top + availableHeight;

        for (int i = 0; i < modules.length; i++) {
            if (modules[i]) {
                float barLeft = left + i * moduleWidth;
                float barRight = left + (i + 1) * moduleWidth;
                canvas.drawRect(barLeft, top, barRight, bottom, paint);
            }
        }
    }

    private static boolean[] encodeCode128B(String value) {
        if (value == null || value.isEmpty()) {
            return new boolean[0];
        }

        // Code 128-B starts with a fixed start pattern, then adds a weighted checksum.
        int checksum = START_CODE_B;
        StringBuilder patternBuilder = new StringBuilder(CODE_128_PATTERNS[START_CODE_B]);

        for (int i = 0; i < value.length(); i++) {
            int code = value.charAt(i) - 32;
            checksum += code * (i + 1);
            patternBuilder.append(CODE_128_PATTERNS[code]);
        }

        checksum = checksum % 103;
        patternBuilder.append(CODE_128_PATTERNS[checksum]);
        patternBuilder.append(CODE_128_PATTERNS[STOP_CODE]);

        return patternToModules(patternBuilder.toString());
    }

    private static boolean[] patternToModules(String pattern) {
        int moduleCount = 0;
        for (int i = 0; i < pattern.length(); i++) {
            moduleCount += Character.digit(pattern.charAt(i), 10);
        }

        boolean[] modules = new boolean[moduleCount];
        int index = 0;
        boolean drawBar = true;
        for (int i = 0; i < pattern.length(); i++) {
            int width = Character.digit(pattern.charAt(i), 10);
            for (int j = 0; j < width; j++) {
                modules[index++] = drawBar;
            }
            drawBar = !drawBar;
        }
        return modules;
    }

    private static String sanitize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "STUDENTDEALZ-DEAL";
        }

        StringBuilder sanitized = new StringBuilder();
        String trimmed = value.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c >= 32 && c <= 126) {
                sanitized.append(c);
            }
        }

        return sanitized.length() == 0 ? "STUDENTDEALZ-DEAL" : sanitized.toString();
    }
}
