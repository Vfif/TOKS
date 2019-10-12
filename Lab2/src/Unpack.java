public class Unpack {
    private boolean isGetEnter;
    private boolean isMark;
    private int index;
    private static int PACKAGE_SIZE = 9;
    private byte[] newPackage = new byte[PACKAGE_SIZE];
    private  byte[] data = new byte[5];
    private static final byte ESC = 0x31;
    private static final byte ESC_esc = 0x7F;
    private static final byte ESC_flag = 0x2F;
    private static final byte FLAG = 0x0A;
    byte FCS;
    byte destAddress;

    public boolean unByteStaffing(StringBuilder builder, byte symbol) {
        if (index == PACKAGE_SIZE) {
            index = 0;
        }
        if (symbol == ESC) {
            isMark = true;
        } else if (symbol == ESC_flag || symbol == ESC_esc) {
            if (isMark) {
                if (symbol == ESC_flag) {
                    newPackage[index] = FLAG;
                    if (index != 1 && index != 2) isGetEnter = true;
                } else {
                    newPackage[index] = ESC;
                }
                isMark = false;
            } else {
                newPackage[index] = symbol;
            }
            index++;
        } else {
            newPackage[index] = symbol;
            index++;
        }

        if (index == PACKAGE_SIZE) {
            System.arraycopy(newPackage, 3, data, 0, 5);
            builder.append(new String(data));
            FCS = newPackage[8];
            destAddress = newPackage[1];
            newPackage = new byte[PACKAGE_SIZE];
            data = new byte[5];
        }
        return isGetEnter && index == PACKAGE_SIZE;
    }
}