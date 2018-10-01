package cell2d.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Andrew Heyman
 * @author joverton
 * @author kevin
 */
public class KeyControl extends Control {
    
    public static final int KEY_ESCAPE          = 0x01;
    public static final int KEY_1               = 0x02;
    public static final int KEY_2               = 0x03;
    public static final int KEY_3               = 0x04;
    public static final int KEY_4               = 0x05;
    public static final int KEY_5               = 0x06;
    public static final int KEY_6               = 0x07;
    public static final int KEY_7               = 0x08;
    public static final int KEY_8               = 0x09;
    public static final int KEY_9               = 0x0A;
    public static final int KEY_0               = 0x0B;
    public static final int KEY_MINUS           = 0x0C; /* - on main keyboard */
    public static final int KEY_EQUALS          = 0x0D;
    public static final int KEY_BACK            = 0x0E; /* backspace */
    public static final int KEY_TAB             = 0x0F;
    public static final int KEY_Q               = 0x10;
    public static final int KEY_W               = 0x11;
    public static final int KEY_E               = 0x12;
    public static final int KEY_R               = 0x13;
    public static final int KEY_T               = 0x14;
    public static final int KEY_Y               = 0x15;
    public static final int KEY_U               = 0x16;
    public static final int KEY_I               = 0x17;
    public static final int KEY_O               = 0x18;
    public static final int KEY_P               = 0x19;
    public static final int KEY_LBRACKET        = 0x1A;
    public static final int KEY_RBRACKET        = 0x1B;
    public static final int KEY_RETURN          = 0x1C; /* Enter on main keyboard */
    public static final int KEY_ENTER           = 0x1C; /* Enter on main keyboard */
    public static final int KEY_LCONTROL        = 0x1D;
    public static final int KEY_A               = 0x1E;
    public static final int KEY_S               = 0x1F;
    public static final int KEY_D               = 0x20;
    public static final int KEY_F               = 0x21;
    public static final int KEY_G               = 0x22;
    public static final int KEY_H               = 0x23;
    public static final int KEY_J               = 0x24;
    public static final int KEY_K               = 0x25;
    public static final int KEY_L               = 0x26;
    public static final int KEY_SEMICOLON       = 0x27;
    public static final int KEY_APOSTROPHE      = 0x28;
    public static final int KEY_GRAVE           = 0x29; /* accent grave */
    public static final int KEY_LSHIFT          = 0x2A;
    public static final int KEY_BACKSLASH       = 0x2B;
    public static final int KEY_Z               = 0x2C;
    public static final int KEY_X               = 0x2D;
    public static final int KEY_C               = 0x2E;
    public static final int KEY_V               = 0x2F;
    public static final int KEY_B               = 0x30;
    public static final int KEY_N               = 0x31;
    public static final int KEY_M               = 0x32;
    public static final int KEY_COMMA           = 0x33;
    public static final int KEY_PERIOD          = 0x34; /* . on main keyboard */
    public static final int KEY_SLASH           = 0x35; /* / on main keyboard */
    public static final int KEY_RSHIFT          = 0x36;
    public static final int KEY_MULTIPLY        = 0x37; /* * on numeric keypad */
    public static final int KEY_LMENU           = 0x38; /* left Alt */
    public static final int KEY_SPACE           = 0x39;
    public static final int KEY_CAPITAL         = 0x3A;
    public static final int KEY_F1              = 0x3B;
    public static final int KEY_F2              = 0x3C;
    public static final int KEY_F3              = 0x3D;
    public static final int KEY_F4              = 0x3E;
    public static final int KEY_F5              = 0x3F;
    public static final int KEY_F6              = 0x40;
    public static final int KEY_F7              = 0x41;
    public static final int KEY_F8              = 0x42;
    public static final int KEY_F9              = 0x43;
    public static final int KEY_F10             = 0x44;
    public static final int KEY_NUMLOCK         = 0x45;
    public static final int KEY_SCROLL          = 0x46; /* Scroll Lock */
    public static final int KEY_NUMPAD7         = 0x47;
    public static final int KEY_NUMPAD8         = 0x48;
    public static final int KEY_NUMPAD9         = 0x49;
    public static final int KEY_SUBTRACT        = 0x4A; /* - on numeric keypad */
    public static final int KEY_NUMPAD4         = 0x4B;
    public static final int KEY_NUMPAD5         = 0x4C;
    public static final int KEY_NUMPAD6         = 0x4D;
    public static final int KEY_ADD             = 0x4E; /* + on numeric keypad */
    public static final int KEY_NUMPAD1         = 0x4F;
    public static final int KEY_NUMPAD2         = 0x50;
    public static final int KEY_NUMPAD3         = 0x51;
    public static final int KEY_NUMPAD0         = 0x52;
    public static final int KEY_DECIMAL         = 0x53; /* . on numeric keypad */
    public static final int KEY_F11             = 0x57;
    public static final int KEY_F12             = 0x58;
    public static final int KEY_F13             = 0x64; /*                     (NEC PC98) */
    public static final int KEY_F14             = 0x65; /*                     (NEC PC98) */
    public static final int KEY_F15             = 0x66; /*                     (NEC PC98) */
    public static final int KEY_KANA            = 0x70; /* (Japanese keyboard)            */
    public static final int KEY_CONVERT         = 0x79; /* (Japanese keyboard)            */
    public static final int KEY_NOCONVERT       = 0x7B; /* (Japanese keyboard)            */
    public static final int KEY_YEN             = 0x7D; /* (Japanese keyboard)            */
    public static final int KEY_NUMPADEQUALS    = 0x8D; /* = on numeric keypad (NEC PC98) */
    public static final int KEY_CIRCUMFLEX      = 0x90; /* (Japanese keyboard)            */
    public static final int KEY_AT              = 0x91; /*                     (NEC PC98) */
    public static final int KEY_COLON           = 0x92; /*                     (NEC PC98) */
    public static final int KEY_UNDERLINE       = 0x93; /*                     (NEC PC98) */
    public static final int KEY_KANJI           = 0x94; /* (Japanese keyboard)            */
    public static final int KEY_STOP            = 0x95; /*                     (NEC PC98) */
    public static final int KEY_AX              = 0x96; /*                     (Japan AX) */
    public static final int KEY_UNLABELED       = 0x97; /*                        (J3100) */
    public static final int KEY_NUMPADENTER     = 0x9C; /* Enter on numeric keypad */
    public static final int KEY_RCONTROL        = 0x9D;
    public static final int KEY_NUMPADCOMMA     = 0xB3; /* , on numeric keypad (NEC PC98) */
    public static final int KEY_DIVIDE          = 0xB5; /* / on numeric keypad */
    public static final int KEY_SYSRQ           = 0xB7;
    public static final int KEY_RMENU           = 0xB8; /* right Alt */
    public static final int KEY_PAUSE           = 0xC5; /* Pause */
    public static final int KEY_HOME            = 0xC7; /* Home on arrow keypad */
    public static final int KEY_UP              = 0xC8; /* UpArrow on arrow keypad */
    public static final int KEY_PRIOR           = 0xC9; /* PgUp on arrow keypad */
    public static final int KEY_LEFT            = 0xCB; /* LeftArrow on arrow keypad */
    public static final int KEY_RIGHT           = 0xCD; /* RightArrow on arrow keypad */
    public static final int KEY_END             = 0xCF; /* End on arrow keypad */
    public static final int KEY_DOWN            = 0xD0; /* DownArrow on arrow keypad */
    public static final int KEY_NEXT            = 0xD1; /* PgDn on arrow keypad */
    public static final int KEY_INSERT          = 0xD2; /* Insert on arrow keypad */
    public static final int KEY_DELETE          = 0xD3; /* Delete on arrow keypad */
    public static final int KEY_LWIN            = 0xDB; /* Left Windows key */
    public static final int KEY_RWIN            = 0xDC; /* Right Windows key */
    public static final int KEY_APPS            = 0xDD; /* AppMenu key */
    public static final int KEY_POWER           = 0xDE;
    public static final int KEY_SLEEP           = 0xDF;
    public static final int KEY_LALT = KEY_LMENU;
    public static final int KEY_RALT = KEY_RMENU;
    
    private static Map<Integer,String> KEYCODE_NAMES = null;
    private static Map<String,Integer> NAME_KEYCODES = null;
    
    private static void putKeycodeName(int keycode, String name) {
        KEYCODE_NAMES.put(keycode, name);
        NAME_KEYCODES.put(name, keycode);
    }
    
    private static void initKeycodeData() {
        KEYCODE_NAMES = new HashMap<>();
        NAME_KEYCODES = new HashMap<>();
        putKeycodeName(KEY_ESCAPE, "Escape");
	putKeycodeName(KEY_1, "1");
	putKeycodeName(KEY_2, "2");
        putKeycodeName(KEY_3, "3");
	putKeycodeName(KEY_4, "4");
        putKeycodeName(KEY_5, "5");
	putKeycodeName(KEY_6, "6");
        putKeycodeName(KEY_7, "7");
	putKeycodeName(KEY_8, "8");
        putKeycodeName(KEY_9, "9");
	putKeycodeName(KEY_0, "0");
	putKeycodeName(KEY_MINUS, "-");
	putKeycodeName(KEY_EQUALS, "=");
	putKeycodeName(KEY_BACK, "Back");
	putKeycodeName(KEY_TAB, "Tab");
	putKeycodeName(KEY_Q, "Q");
	putKeycodeName(KEY_W, "W");
	putKeycodeName(KEY_E, "E");
	putKeycodeName(KEY_R, "R");
	putKeycodeName(KEY_T, "T");
	putKeycodeName(KEY_Y, "Y");
	putKeycodeName(KEY_U, "U");
	putKeycodeName(KEY_I, "I");
	putKeycodeName(KEY_O, "O");
	putKeycodeName(KEY_P, "P");
	putKeycodeName(KEY_LBRACKET, "[");
	putKeycodeName(KEY_RBRACKET, "]");
	putKeycodeName(KEY_ENTER, "Enter");
	putKeycodeName(KEY_LCONTROL, "LCtrl");
	putKeycodeName(KEY_A, "A");
	putKeycodeName(KEY_S, "S");
	putKeycodeName(KEY_D, "D");
	putKeycodeName(KEY_F, "F");
	putKeycodeName(KEY_G, "G");
	putKeycodeName(KEY_H, "H");
	putKeycodeName(KEY_J, "J");
	putKeycodeName(KEY_K, "K");
	putKeycodeName(KEY_L, "L");
	putKeycodeName(KEY_SEMICOLON, ";");
	putKeycodeName(KEY_APOSTROPHE, "'");
	putKeycodeName(KEY_GRAVE, "`");
	putKeycodeName(KEY_LSHIFT, "LShift");
	putKeycodeName(KEY_BACKSLASH, "\\");
	putKeycodeName(KEY_Z, "Z");
	putKeycodeName(KEY_X, "X");
	putKeycodeName(KEY_C, "C");
	putKeycodeName(KEY_V, "V");
	putKeycodeName(KEY_B, "B");
	putKeycodeName(KEY_N, "N");
	putKeycodeName(KEY_M, "M");
	putKeycodeName(KEY_COMMA, ",");
	putKeycodeName(KEY_PERIOD, ".");
	putKeycodeName(KEY_SLASH, "/");
	putKeycodeName(KEY_RSHIFT, "RShift");
	putKeycodeName(KEY_MULTIPLY, "Numpd*");
	putKeycodeName(KEY_LMENU, "LAlt");
	putKeycodeName(KEY_SPACE, "Space");
	putKeycodeName(KEY_CAPITAL, "CapsLk");
	putKeycodeName(KEY_F1, "F1");
        putKeycodeName(KEY_F2, "F2");
        putKeycodeName(KEY_F3, "F3");
        putKeycodeName(KEY_F4, "F4");
        putKeycodeName(KEY_F5, "F5");
        putKeycodeName(KEY_F6, "F6");
        putKeycodeName(KEY_F7, "F7");
        putKeycodeName(KEY_F8, "F8");
        putKeycodeName(KEY_F9, "F9");
        putKeycodeName(KEY_F10, "F10");
	putKeycodeName(KEY_NUMLOCK, "NumLk");
	putKeycodeName(KEY_SCROLL, "ScrlLk");
	putKeycodeName(KEY_NUMPAD7, "Numpd7");
	putKeycodeName(KEY_NUMPAD8, "Numpd8");
	putKeycodeName(KEY_NUMPAD9, "Numpd9");
	putKeycodeName(KEY_SUBTRACT, "Numpd-");
	putKeycodeName(KEY_NUMPAD4, "Numpd4");
	putKeycodeName(KEY_NUMPAD5, "Numpd5");
	putKeycodeName(KEY_NUMPAD6, "Numpd6");
	putKeycodeName(KEY_ADD, "Numpd+");
	putKeycodeName(KEY_NUMPAD1, "Numpd1");
	putKeycodeName(KEY_NUMPAD2, "Numpd2");
	putKeycodeName(KEY_NUMPAD3, "Numpd3");
	putKeycodeName(KEY_NUMPAD0, "Numpd0");
	putKeycodeName(KEY_DECIMAL, "Numpd.");
	putKeycodeName(KEY_F11, "F11");
	putKeycodeName(KEY_F12, "F12");
	putKeycodeName(KEY_F13, "F13");
	putKeycodeName(KEY_F14, "F14");
	putKeycodeName(KEY_F15, "F15");
	putKeycodeName(KEY_KANA, "Kana");
	putKeycodeName(KEY_CONVERT, "Conv");
	putKeycodeName(KEY_NOCONVERT, "NoConv");
	putKeycodeName(KEY_YEN, "Yen");
	putKeycodeName(KEY_NUMPADEQUALS, "Numpd=");
	putKeycodeName(KEY_CIRCUMFLEX, "^");
	putKeycodeName(KEY_AT, "@");
	putKeycodeName(KEY_COLON, ":");
	putKeycodeName(KEY_UNDERLINE, "_");
	putKeycodeName(KEY_KANJI, "Kanji");
	putKeycodeName(KEY_STOP, "Stop");
	putKeycodeName(KEY_AX, "AX");
	putKeycodeName(KEY_UNLABELED, "Unlabl");
	putKeycodeName(KEY_NUMPADENTER, "NumpdE");
	putKeycodeName(KEY_RCONTROL, "RCtrl");
	putKeycodeName(KEY_NUMPADCOMMA, "Numpd,");
	putKeycodeName(KEY_DIVIDE, "Numpd%");
	putKeycodeName(KEY_SYSRQ, "SysRq");
	putKeycodeName(KEY_RMENU, "RAlt");
	putKeycodeName(KEY_PAUSE, "Pause");
	putKeycodeName(KEY_HOME, "Home");
	putKeycodeName(KEY_UP, "Up");
	putKeycodeName(KEY_PRIOR, "PageUp");
	putKeycodeName(KEY_LEFT, "Left");
	putKeycodeName(KEY_RIGHT, "Right");
	putKeycodeName(KEY_END, "End");
	putKeycodeName(KEY_DOWN, "Down");
	putKeycodeName(KEY_NEXT, "PageDn");
	putKeycodeName(KEY_INSERT, "Insert");
	putKeycodeName(KEY_DELETE, "Delete");
	putKeycodeName(KEY_LWIN, "LWndws");
	putKeycodeName(KEY_RWIN, "RWndws");
	putKeycodeName(KEY_APPS, "Apps");
	putKeycodeName(KEY_POWER, "Power");
	putKeycodeName(KEY_SLEEP, "Sleep");
    }
    
    public static String getKeyCodeName(int keyCode) {
        if (KEYCODE_NAMES == null) {
            initKeycodeData();
        }
        return KEYCODE_NAMES.get(keyCode);
    }
    
    public static Integer getKeyCode(String name) {
        if (NAME_KEYCODES == null) {
            initKeycodeData();
        }
        return NAME_KEYCODES.get(name);
    }
    
    private final int keyCode;
    
    public KeyControl(int keyCode) throws InvalidControlException {
        if (getKeyCodeName(keyCode) == null) {
            throw new InvalidControlException("Attempted to construct a KeyControl with invalid key code "
                    + keyCode);
        }
        this.keyCode = keyCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KeyControl) {
            return ((KeyControl)obj).keyCode == keyCode;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash("Key", keyCode);
    }
    
    @Override
    public String getName() {
        return KEYCODE_NAMES.get(keyCode);
    }
    
    public final int getKeyCode() {
        return keyCode;
    }
    
}
