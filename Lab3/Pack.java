import javafx.scene.control.TextArea;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Pack {
    private static final byte ESC = 0x31;
    private static final byte ESC_esc = 0x7F;
    private static final byte ESC_flag = 0x2F;
    private static final byte FLAG = 0x0A;

    public static String pack(String line, byte destinationAddress, byte sourceAddress, boolean errorState, TextArea area) {
        byte[] data;
        StringBuilder str = new StringBuilder(line);
        StringBuilder result = new StringBuilder();
        while (str.length() % 5 != 0) {
            str.append("\0");
        }
        for (int i = 0; i < str.length() / 5; i++) {
            byte[] packageSend = new byte[8];
            data = str.substring(5 * i, (i + 1) * 5).getBytes();
            packageSend[0] = FLAG;
            packageSend[1] = destinationAddress;
            packageSend[2] = sourceAddress;
            System.arraycopy(data, 0, packageSend, 3, 5);
            packageSend = HammingCode.encodePacket(packageSend, errorState);              // FIXME: 19.10.2019//добавляется 1 байт
            /*for(byte a: packageSend){
                System.out.print(a + " ");
            }*/
            byte[] arr = byteStaffing(packageSend);
            area.appendText(Hex.encodeHexString(arr).toUpperCase() + "\r\n");

            result.append(new String(arr, StandardCharsets.ISO_8859_1));// FIXME: 20.10.2019 ??? 789, dest = 9, src = 0, первый символ в utf, остальные Cp1252

        }

        return result.toString();
    }

    private static byte[] byteStaffing(byte[] array) {
        int key = 0;
        for (int i = 1; i < array.length; i++) {
            if (FLAG == array[i] || ESC == array[i]) key++;
        }
        if (key == 0) return array;
        else {
            byte[] readyArray = new byte[array.length + key];
            byte[] helpArray = new byte[array.length + key];
            System.arraycopy(array, 0, helpArray, 0, array.length);

            int j = 0;
            for (int i = 1; i < array.length; i++) {
                if (FLAG == array[i]) {
                    insert(helpArray, readyArray, i + j, ESC_flag);
                    j++;
                } else if (ESC == array[i]) {
                    insert(helpArray, readyArray, i + j, ESC_esc);
                    j++;
                }
            }

            return readyArray;
        }
    }

    private static void insert(byte[] array, byte[] newElements, int i, byte symbol) {
        System.arraycopy(array, 0, newElements, 0, i);
        System.arraycopy(array, i + 1, newElements, i + 2, array.length - i - 2);
        newElements[i] = ESC;
        newElements[i + 1] = symbol;
        System.arraycopy(newElements, 0, array, 0, array.length);
    }
}
