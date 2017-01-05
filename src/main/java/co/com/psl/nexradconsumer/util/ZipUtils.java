package co.com.psl.nexradconsumer.util;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Created by acastanedav on 28/12/16.
 */
public class ZipUtils {

    public String unZip(String gzFile, String unCompressFile, Logger logger) {
        try (
                GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gzFile));
                FileOutputStream out = new FileOutputStream(unCompressFile, true)
        ) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            return unCompressFile;
        } catch (Exception e) {
            logger.error("Problem with file " + gzFile + ": " + e.getMessage(), e);
        }
        return "";
    }
}
