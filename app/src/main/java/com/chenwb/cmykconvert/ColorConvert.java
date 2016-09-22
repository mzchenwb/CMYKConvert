package com.chenwb.cmykconvert;


public class ColorConvert {

    private static final int CMP_MAX = 0xFF;

    public static int[] rgb2cmyk(int r, int g, int b) {
        int iC, iM, iY, iK, C, M, Y, K;

        iC = CMP_MAX - r;
        iM = CMP_MAX - g;
        iY = CMP_MAX - b;

        K = Math.min(Math.min(iC, iM), iY);

        if (K != CMP_MAX) {
            iK = CMP_MAX - K;      // or minimum(R,G,B)

            C = (CMP_MAX * (iC - K)) / iK;
            M = (CMP_MAX * (iM - K)) / iK;
            Y = (CMP_MAX * (iY - K)) / iK;

        } else { // would have divided by 0
            C = M = Y = 0;
        }

        return new int[]{C, M, Y, K};
    }

    public static float[] rgb2cmyk2(int r, int g, int b) {
        float c, m, y, k;
        float black;

        float factor = 255f;

        k = Math.min(1 - r / factor, Math.min(1 - g / factor, 1 - b / factor));
        black = k;

        c = ((1 - (r / factor) - black) / (1 - black));
        m = ((1 - (g / factor) - black) / (1 - black));
        y = ((1 - (b / factor) - black) / (1 - black));

        return new float[]{c, m, y, k};
    }

    byte[] RGBToCMY(byte red, byte green, byte blue)//RGB转CMY
    {
        byte cyan = (byte) (255 - red);
        byte magenta = (byte) (255 - green);
        byte yellow = (byte) (255 - blue);
        return CorrectCMYK(cyan, magenta, yellow, (byte) 20);
        //修正值
    }

    byte[] CorrectCMYK(byte cyan, byte magenta, byte yellow, byte rep_v)//色彩修正
    {
        byte temp = (byte) Math.min(Math.min(cyan, magenta), yellow);
        byte rep_k, rep_c, rep_m, rep_y;
        if (temp != 0) {
            byte temp2 = (byte) ((rep_v / 100.0) * temp + 0.9);
            rep_k = (byte) (temp2 / 255.0 * 100 + 0.9);
            rep_c = (byte) ((cyan - temp2) / 255.0 * 100 + 0.9);
            rep_m = (byte) ((magenta - temp2) / 255.0 * 100 + 0.9);
            rep_y = (byte) ((yellow - temp2) / 255.0 * 100 + 0.9);
        } else {
            rep_c = (byte) (cyan / 255.0 * 100 + 0.9);
            rep_m = (byte) (magenta / 255.0 * 100 + 0.9);
            rep_y = (byte) (yellow / 255.0 * 100 + 0.9);
            rep_k = 0;
        }
        return new byte[]{rep_c, rep_m, rep_y, rep_k};
    }

    public static int cmyk2rgb(float c, float m, float y, float k) {

        int r = (int) ((1 - Math.min((float)1, (c) * (1 - (k)) + (k))) * 255);
        int g = (int) ((1 - Math.min((float)1, (m) * (1 - (k)) + (k))) * 255);
        int b = (int) ((1 - Math.min((float)1, (y) * (1 - (k)) + (k))) * 255);

        return r << 16 | g << 8 | b;
    }
}
