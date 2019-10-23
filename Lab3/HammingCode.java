import java.util.Random;

public class HammingCode {
    private static final int CHECKSUM_COUNT = 7;

    private static StringBuilder toStringOfBits(byte[] array) {
        StringBuilder builder = new StringBuilder();
        //to String
        for (byte b : array) {
            for (int i = 7; i >= 0; --i) {
                builder.append(b >>> i & 1);
            }
        }
        return builder;
    }

    private static int[] calculateChecksum(StringBuilder builder) {
        //mark checksum position by 0
        for (int i = 0; i < CHECKSUM_COUNT; i++) {
            builder.insert((int) Math.pow(2, i) - 1, '0');
        }

        //checksum calculation
        int[] checksum = new int[CHECKSUM_COUNT];
        //1, 2, 4, 8, 16, 32, 64, итого +7 бит, надо будет еще один бит добавить
        for (int i = 0; i < builder.length(); i++) {
            for (int j = 0; j < CHECKSUM_COUNT; j++) {
                if (((i + 1 >>> j) & 1) != 0) {
                    if (builder.charAt(i) == '1')
                        checksum[j]++;
                }
            }
        }

        for (int i = 0; i < CHECKSUM_COUNT; i++) {
            checksum[i] %= 2;
        }

        return checksum;
    }

    public static byte[] encodePacket(byte[] array, boolean errorState) {//принимаем array[8], возвращаем array[9]

        StringBuilder builder = toStringOfBits(array);
        StringBuilder resultBuilder = new StringBuilder(builder);


        int[] checksum = calculateChecksum(builder);

        for (int i = 0; i < CHECKSUM_COUNT; i++) {
            resultBuilder.append(checksum[i]);
        }
        resultBuilder.append("0");//добиваем до байта: 71 бит -> 72 бита

        //create error if necessary
        if (errorState) {
            int position = new Random().nextInt(resultBuilder.length());
            //System.out.println(position);
            if (resultBuilder.charAt(position) == '0') resultBuilder.replace(position, position + 1, "1");
            else resultBuilder.replace(position, position + 1, "0");
        }

        byte[] newArray = new byte[9];
        //return to bytes[9]
        for (int i = 0; i < resultBuilder.length() / 8; i++) {
            newArray[i] = (byte) Integer.parseInt(resultBuilder.substring(8 * i, (i + 1) * 8), 2);
        }
        return newArray;
    }

    public static byte[] decodePacket(byte[] array) {//принимаем array[9], возвращаем array[8]
        //to String
        StringBuilder builder = toStringOfBits(array);

        //take expected checksum
        int[] expectedChecksum = new int[CHECKSUM_COUNT];
        for (int i = 0; i < CHECKSUM_COUNT; i++) {
            expectedChecksum[i] = builder.charAt(64 + i) == '1' ? 1 : 0;
        }
        builder.delete(64, builder.length());

        int[] actualChecksum = calculateChecksum(builder);

        //compare actual and expected
        boolean result = false;
        for (int i = 0; i < CHECKSUM_COUNT; i++) {
            if (!(result = actualChecksum[i] == expectedChecksum[i])) break;
        }

        //return result;
        if (result) {
            return array(builder);
        } else {
            int position = 0;
            for (int i = 0; i < CHECKSUM_COUNT; i++) {
                if (actualChecksum[i] != expectedChecksum[i]) {
                    position += (int) Math.pow(2, i);
                }
            }

            if (position == 1 || position == 2 || position == 4 || position == 8 || position == 16 || position == 32 || position == 64) {
                return array(builder);
            }
            builder.replace(position - 1, position, builder.charAt(position - 1) == '0' ? "1" : "0");
            return array(builder);
        }
    }

    private static byte[] array(StringBuilder builder) {

        for (int i = CHECKSUM_COUNT - 1; i >= 0; i--) {
            int position = (int) Math.pow(2, i) - 1;
            builder.deleteCharAt(position);
        }
        byte[] newArray = new byte[8];
        for (int i = 0; i < builder.length() / 8; i++) {
            newArray[i] = (byte) Integer.parseInt(builder.substring(8 * i, (i + 1) * 8), 2);
        }
        return newArray;
    }
}