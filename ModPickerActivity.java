package su.sovietgames.everlasting_summer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Android-only picker/installer for Everlasting Summer 1.6.
 * It uses the system DocumentsUI picker, so Android/data does not have to be
 * opened by a separate file manager.
 */
public class ModPickerActivity extends Activity {
    private static final int REQUEST_PICK = 4172;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        openPicker();
    }

    private void openPicker() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");
        startActivityForResult(i, REQUEST_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PICK) return;

        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            finish();
            return;
        }

        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(
                    uri, data.getFlags() &
                    (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
        } catch (Exception ignored) {
            // Not all DocumentsProviders grant persistable permissions.
        }

        try {
            installZip(uri);
            Toast.makeText(this, "Мод установлен. Перезапускаю игру…", Toast.LENGTH_LONG).show();
            restartGame();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка установки мода: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void installZip(Uri uri) throws Exception {
        File root = getExternalFilesDir(null);
        if (root == null) throw new Exception("Нет доступа к каталогу игры");
        if (!root.exists() && !root.mkdirs()) throw new Exception("Не удалось создать каталог игры");

        InputStream input = getContentResolver().openInputStream(uri);
        if (input == null) throw new Exception("Не удалось открыть архив");

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(input));
        byte[] buffer = new byte[32768];
        ZipEntry entry;

        try {
            while ((entry = zis.getNextEntry()) != null) {
                String name = normalizeEntry(entry.getName());
                if (name.length() == 0) continue;

                // Some PC archives contain a top-level game/ directory. The
                // Android 1.6 port expects the mod contents directly in files/.
                if (name.equals("game")) continue;
                if (name.startsWith("game/")) name = name.substring(5);
                if (name.length() == 0) continue;

                File out = new File(root, name);
                String rootPath = root.getCanonicalPath() + File.separator;
                String outPath = out.getCanonicalPath();
                if (!outPath.startsWith(rootPath)) {
                    throw new Exception("Небезопасный путь в архиве");
                }

                if (entry.isDirectory()) {
                    if (!out.exists() && !out.mkdirs()) throw new Exception("Не удалось создать папку");
                    continue;
                }

                File parent = out.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    throw new Exception("Не удалось создать папку мода");
                }

                FileOutputStream fos = new FileOutputStream(out);
                try {
                    int n;
                    while ((n = zis.read(buffer)) != -1) fos.write(buffer, 0, n);
                } finally {
                    fos.close();
                }
            }
        } finally {
            zis.close();
            input.close();
        }
    }

    private String normalizeEntry(String name) throws Exception {
        if (name == null) return "";
        name = name.replace('\\', '/');
        while (name.startsWith("./")) name = name.substring(2);
        while (name.startsWith("/")) name = name.substring(1);
        if (name.length() == 0) return "";

        String[] parts = name.split("/");
        StringBuilder safe = new StringBuilder();
        for (String p : parts) {
            if (p.length() == 0 || p.equals(".")) continue;
            if (p.equals("..")) throw new Exception("Небезопасный путь в архиве");
            if (safe.length() > 0) safe.append('/');
            safe.append(p);
        }
        return safe.toString();
    }

    private void restartGame() {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(launch);
            }
        } catch (Exception ignored) {
        }
        finishAffinity();
    }
}
