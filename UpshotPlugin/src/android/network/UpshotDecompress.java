/*
 * //***************************************************************************************************
 * //***************************************************************************************************
 * //      Project adUnitName                    	: BrandKinesis
 * //      Class Name                              :
 * //      Author                                  : PurpleTalk
 * //***************************************************************************************************
 * //     Class Description:
 * //
 * //***************************************************************************************************
 * //***************************************************************************************************
 */

package cordova_plugin_upshotplugin;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by PurpleTalk on 19/8/15.
 */
public class UpshotDecompress {

    private final static int BUFFER_SIZE = 1024;

    /**
     * Get the Zip file from Assets folder and store that file into given path
     *
     * @param unzipLocation
     * @return
     */
    public static void unzip(ZipInputStream zipIn, String unzipLocation/*dir path*/) throws IOException {
        /*if(!getSDCardStatus()) {
			return false;
		}*/
        //validate unziplocation
        File file = new File(unzipLocation);
        if (!file.exists()) {
            file.mkdirs();
        }

        try {
            ZipEntry entry = zipIn.getNextEntry();

            // iterates over entries in the zip file
            while (entry != null) {
                final String entryName = entry.getName();
                String filePath = unzipLocation ;
                if(!entryName.startsWith("/")){
                    filePath += File.separator;
                }
                filePath += entryName;


                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        } finally {
            zipIn.close();
        }

    }

    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {

        // check directory exists for not first
        String dirPath = filePath.substring(0,filePath.lastIndexOf('/'));
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }


        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.flush();
        bos.close();
    }



}