package br.com.uemg.prog3.radarurbano;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

public class ImagemUtil {

    /** Carrega a foto reduzida (evita estouro de memória) e corrige a rotação EXIF. */
    public static Bitmap carregar(String caminho, int maxDim) {
        if (caminho == null) return null;
        try {
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(caminho, op);

            int amostra = 1;
            while (Math.max(op.outWidth, op.outHeight) / amostra > maxDim) amostra *= 2;

            op.inJustDecodeBounds = false;
            op.inSampleSize = amostra;
            Bitmap bmp = BitmapFactory.decodeFile(caminho, op);
            if (bmp == null) return null;

            int graus = 0;
            int o = new ExifInterface(caminho).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (o == ExifInterface.ORIENTATION_ROTATE_90) graus = 90;
            else if (o == ExifInterface.ORIENTATION_ROTATE_180) graus = 180;
            else if (o == ExifInterface.ORIENTATION_ROTATE_270) graus = 270;

            if (graus != 0) {
                Matrix m = new Matrix();
                m.postRotate(graus);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
            }
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }
}
