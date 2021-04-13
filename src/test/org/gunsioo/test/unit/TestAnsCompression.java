/*
 * Copyright 2004-2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Gunsioo Group
 */
package org.gunsioo.test.unit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.gunsioo.dev.util.AnsCompression;
import org.gunsioo.dev.util.BinaryArithmeticStream;
import org.gunsioo.dev.util.BitStream;
import org.gunsioo.test.TestBase;

/**
 * Tests the ANS (Asymmetric Numeral Systems) compression tool.
 */
public class TestAnsCompression extends TestBase {

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String... a) throws Exception {
        TestBase.createCaller().init().testFromMain();
    }

    @Override
    public void test() throws Exception {
        testScaleFrequencies();
        testRandomized();
        testCompressionRate();
    }

    private void testCompressionRate() throws IOException {
        byte[] data = new byte[1024 * 1024];
        Random r = new Random(1);
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (r.nextInt(4) * r.nextInt(4));
        }
        int[] freq = new int[256];
        AnsCompression.countFrequencies(freq, data);
        int lenAns = AnsCompression.encode(freq, data).length;
        BitStream.Huffman huff = new BitStream.Huffman(freq);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BitStream.Out o = new BitStream.Out(out);
        for (byte x : data) {
            huff.write(o, x & 255);
        }
        o.flush();
        int lenHuff = out.toByteArray().length;
        BinaryArithmeticStream.Huffman aHuff = new BinaryArithmeticStream.Huffman(
                freq);
        out = new ByteArrayOutputStream();
        BinaryArithmeticStream.Out o2 = new BinaryArithmeticStream.Out(out);
        for (byte x : data) {
            aHuff.write(o2, x & 255);
        }
        o2.flush();
        int lenArithmetic = out.toByteArray().length;

        assertTrue(lenAns < lenArithmetic);
        assertTrue(lenArithmetic < lenHuff);
        assertTrue(lenHuff < data.length);
    }

    private void testScaleFrequencies() {
        Random r = new Random(1);
        for (int j = 0; j < 100; j++) {
            int symbolCount = r.nextInt(200) + 1;
            int[] freq = new int[symbolCount];
            for (int total = symbolCount * 2; total < 10000; total *= 2) {
                for (int i = 0; i < freq.length; i++) {
                    freq[i] = r.nextInt(1000) + 1;
                }
                AnsCompression.scaleFrequencies(freq, total);
            }
        }
        int[] freq = new int[]{0, 1, 1, 1000};
        AnsCompression.scaleFrequencies(freq, 100);
        assertEquals("[0, 1, 1, 98]", Arrays.toString(freq));
    }

    private void testRandomized() {
        Random r = new Random(1);
        int symbolCount = r.nextInt(200) + 1;
        int[] freq = new int[symbolCount];
        for (int i = 0; i < freq.length; i++) {
            freq[i] = r.nextInt(1000) + 1;
        }
        int seed = r.nextInt();
        r.setSeed(seed);
        int len = 10000;
        byte[] data = new byte[len];
        r.nextBytes(data);
        freq = new int[256];
        AnsCompression.countFrequencies(freq, data);
        byte[] encoded = AnsCompression.encode(freq, data);
        byte[] decoded = AnsCompression.decode(freq, encoded, data.length);
        for (int i = 0; i < len; i++) {
            int expected = data[i];
            assertEquals(expected, decoded[i]);
        }
    }

}
