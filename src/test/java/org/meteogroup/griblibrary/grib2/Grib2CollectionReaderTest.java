package org.meteogroup.griblibrary.grib2;

import org.meteogroup.griblibrary.exception.BinaryNumberConversionException;
import org.meteogroup.griblibrary.exception.GribReaderException;
import org.meteogroup.griblibrary.grib2.model.Grib2Record;
import org.meteogroup.griblibrary.util.FileChannelPartReader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by roijen on 28-Oct-15.
 */
public class Grib2CollectionReaderTest {

    Grib2CollectionReader collectionReader;

    @DataProvider(name = "simpleFileLocation")
    public static Object[][] simpleFileLocation(){
        return new Object[][]{
                new Object[]{VERY_SIMPLE_TEXT_FILE_LOCATION}
        };
    }

    @DataProvider(name = "notExistingFileLocation")
    public static Object[][] notExistingFileLocation(){
        return new Object[][]{
                new Object[]{NOT_EXISTING_FILE_LOCATION},
        };
    }

    @BeforeMethod
    public void setUp(){
        collectionReader = new Grib2CollectionReader();
    }

    @Test(dataProvider = "simpleFileLocation")
    public void getAFileChannelFromAFileName(String fileLocation) throws IOException {
        String fileName = getClass().getResource(fileLocation).getPath();
        FileChannel channel = collectionReader.getFileChannelFromURL(fileName);
        assertThat(channel).isNotNull();
        assertThat(collectionReader.getGribRecordOffset()).isEqualTo(0l);
        assertThat(collectionReader.getFileLength()).isGreaterThan(0);
    }

    @Test(dataProvider = "notExistingFileLocation", expectedExceptions = FileNotFoundException.class)
    public void getAFileChannelFromANotExistingFileName(String fileLocation) throws IOException {
        collectionReader.getFileChannelFromURL(fileLocation);
    }

    @Test
    public void testReadRecords() throws GribReaderException, IOException, BinaryNumberConversionException {

        collectionReader.partReader = mock(FileChannelPartReader.class);
        collectionReader.recordReader = mock(Grib2RecordReader.class);
        collectionReader.fileLength = 32;
        collectionReader.gribRecordOffset = 0;

        when(collectionReader.partReader.readPartOfFileChannel(any(FileChannel.class), anyInt(), anyInt())).thenReturn(SIMULATED_BYTE_ARRAY);

        when(collectionReader.recordReader.checkIfGribFileIsValidGrib2(any(byte[].class))).thenReturn(true);
        when(collectionReader.recordReader.readRecordLength(any(byte[].class))).thenReturn(16l);

        List<Grib2Record> records = collectionReader.readAllRecords(SIMULATED_FILE_CHANNEL());
        assertThat(records.size()).isEqualTo(2);
    }

    private static final String VERY_SIMPLE_TEXT_FILE_LOCATION = "VerySimpleSampleFile.txt";

    private static final String NOT_EXISTING_FILE_LOCATION = "/dev/null/doesnotexist.txt";

    private static final byte[] SIMULATED_BYTE_ARRAY = new byte[]{'G','R','I','B',19,84,-26,1};
    private static FileChannel SIMULATED_FILE_CHANNEL() throws FileNotFoundException {
        String fileName = Grib2CollectionReaderTest.class.getResource("VerySimpleSampleFile.txt").getPath();
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        return raf.getChannel();
    }
}
