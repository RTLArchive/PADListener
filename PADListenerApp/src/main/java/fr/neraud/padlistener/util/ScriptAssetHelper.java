package fr.neraud.padlistener.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import fr.neraud.padlistener.constant.ScriptAsset;

/**
 * Helper to copy the scripts bundled in the assets
 *
 * @author Neraud
 */
public class ScriptAssetHelper extends AbstractAssetHelper {

	public ScriptAssetHelper(Context context) {
		super(context);
	}

	public void copyScriptsFromAssets() throws IOException {
		Log.d(getClass().getName(), "copyScriptsFromAssets");
		copyScriptFromAssets(ScriptAsset.ENABLE_IPTABLES);
		copyScriptFromAssets(ScriptAsset.DISABLE_IPTABLES);
	}

	private void copyScriptFromAssets(ScriptAsset scriptAsset) throws IOException {
		copyAsset(scriptAsset.getScriptName(), getContext().getFilesDir().getPath() + "/" + scriptAsset.getScriptName());
	}

	protected void doCopyAsset(String assetFileName, String targetFilePath, File targetFile) throws IOException {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			final InputStream in = getContext().getAssets().open(assetFileName);
			reader = new BufferedReader(new InputStreamReader(in));

			final OutputStream out = new FileOutputStream(targetFilePath);
			writer = new BufferedWriter(new FileWriter(new File(targetFilePath)));

			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				// Replace new lines from the assets by writer.newLine() -> dos2unix
				writer.write(line);
				writer.newLine();
			}
			writer.flush();
			targetFile.setExecutable(true);
		} catch (final IOException e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
					Log.w(getClass().getName(), "doCopyAsset : error closing in stream");
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (final IOException e) {
					Log.w(getClass().getName(), "doCopyAsset : error closing out stream");
				}
			}
		}
	}
}
