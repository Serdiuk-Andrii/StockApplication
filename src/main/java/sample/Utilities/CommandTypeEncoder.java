package Utilities;

public class CommandTypeEncoder {

    boolean isProduct;

    int commandTypeCode;

    String commandTypeString;

    public boolean isProduct() {
        return isProduct;
    }

    public int getCommandTypeCode() {
        return commandTypeCode;
    }

    public String getCommandTypeString() {
        return commandTypeString;
    }

    public CommandTypeEncoder(int INCOMING_COMMAND_TYPE) throws Exception {
        isProduct = isProduct(INCOMING_COMMAND_TYPE);
        commandTypeCode = getTypeCommandCode(INCOMING_COMMAND_TYPE);
        commandTypeString = getTypeCommand(INCOMING_COMMAND_TYPE);
    }

    public static final int ERROR = -1;

    static public final int
            PRODUCT = 1,
            GROUP = 2;

    static public final int
            CREATE = 4,
            READ = 8,
            UPDATE = 16,
            DELETE = 32,
            LIST_ALL = 64,
            COST = 128,
            LIST_SPECIFIC = 256;


    static public final int
            PRODUCT_CREATE = PRODUCT ^ CREATE,
            PRODUCT_READ = PRODUCT ^ READ,
            PRODUCT_UPDATE = PRODUCT ^ UPDATE,
            PRODUCT_DELETE = PRODUCT ^ DELETE,
            PRODUCT_LIST_ALL = PRODUCT ^ LIST_ALL;

    static public final int
            GROUP_CREATE = GROUP ^ CREATE,
            GROUP_READ = GROUP ^ READ,
            GROUP_UPDATE = GROUP ^ UPDATE,
            GROUP_DELETE = GROUP ^ DELETE,
            GROUP_LIST_ALL = GROUP ^ LIST_ALL,
            GROUP_COST = GROUP ^ COST,
            GROUP_LIST_SPECIFIC = GROUP ^ LIST_SPECIFIC;

    public static boolean isProduct(int INCOMING_COMMAND_TYPE) {
        return (INCOMING_COMMAND_TYPE & PRODUCT) == 1;
    }

    public static int getTypeCommandCode(int INCOMING_COMMAND_TYPE) {
        boolean IS_PRODUCT = isProduct(INCOMING_COMMAND_TYPE);
        return INCOMING_COMMAND_TYPE ^ (IS_PRODUCT ? PRODUCT : GROUP);
    }

    public static String getTypeCommand(int INCOMING_COMMAND_TYPE) throws Exception {
        int COMMAND = getTypeCommandCode(INCOMING_COMMAND_TYPE);
        switch (COMMAND) {
            case CREATE:
                return "CREATE";

            case READ:
                return "READ";

            case UPDATE:
                return "UPDATE";

            case DELETE:
                return "DELETE";

            case LIST_ALL:
                return "LIST_ALL";

            default:
                throw new Exception("Undefined INCOMING_COMMAND_TYPE");
        }
    }
}
