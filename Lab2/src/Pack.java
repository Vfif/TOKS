import javafx.scene.control.TextArea;
import org.apache.commons.codec.binary.Hex;

public class Pack {
    private static final byte ESC = 0x31;
    private static final byte ESC_esc = 0x7F;
    private static final byte ESC_flag = 0x2F;
    private static final byte FLAG = 0x0A;

    public static String pack(String line , byte destinationAddress, byte sourceAddress, byte FCS, TextArea area) {
        byte[] data;
        StringBuilder str = new StringBuilder(line);
        StringBuilder result = new StringBuilder();
        while (str.length() % 5 != 0) {
            str.append("\0");
        }
        for (int i = 0; i < str.length() / 5; i++) {
            byte[] packageSend = new byte[9];
            data = str.substring(5 * i, (i + 1) * 5).getBytes();
            packageSend[0] = FLAG;
            packageSend[1] = destinationAddress;
            packageSend[2] = sourceAddress;
            System.arraycopy(data, 0, packageSend, 3, 5);
            packageSend[8] = FCS;
            byte[] arr = byteStaffing(packageSend);
            area.appendText(Hex.encodeHexString(arr).toUpperCase() + "\r\n");
            result.append(new String(arr));
        }
        return result.toString();
    }

    private static byte[] byteStaffing(byte[] array) {
        int key = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[0] == array[i] || ESC == array[i]) key++;
        }
        if (key == 0) return array;
        else {
            byte[] readyArray = new byte[array.length + key];
            byte[] helpArray = new byte[array.length + key];
            System.arraycopy(array, 0, helpArray, 0, array.length);

            int j = 0;
            for (int i = 1; i < array.length; i++) {
                if (array[0] == array[i]) {
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
