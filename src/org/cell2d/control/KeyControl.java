package org.cell2d.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>A KeyControl is a Control that represents a key on the keyboard. The key
 * is specified by an integer <i>key code</i> that is equal to one of the KEY_
 * constants of the KeyControl class. All other integers are invalid as key
 * codes.</p>
 * @author Alex Heyman
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
    public static final int KEY_LALT            = 0x38; /* left Alt */
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
    public static final int KEY_F16             = 0x67; /* Extended Function keys - (Mac) */
    public static final int KEY_F17             = 0x68;
    public static final int KEY_F18             = 0x69;
    public static final int KEY_KANA            = 0x70; /* (Japanese keyboard)            */
    public static final int KEY_F19             = 0x71; /* Extended Function keys - (Mac) */
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
    public static final int KEY_SECTION         = 0xA7; /* Section symbol (Mac) */
    public static final int KEY_NUMPADCOMMA     = 0xB3; /* , on numeric keypad (NEC PC98) */
    public static final int KEY_DIVIDE          = 0xB5; /* / on numeric keypad */
    public static final int KEY_SYSRQ           = 0xB7;
    public static final int KEY_RALT            = 0xB8; /* right Alt */
    public static final int KEY_FUNCTION        = 0xC4; /* Function (Mac) */
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
    public static final int KEY_CLEAR           = 0xDA; /* Clear key (Mac) */
    public static final int KEY_LMETA           = 0xDB; /* Left Windows/Option key */
    public static final int KEY_RMETA           = 0xDC; /* Right Windows/Option key */
    public static final int KEY_APPS            = 0xDD; /* AppMenu key */
    public static final int KEY_POWER           = 0xDE;
    public static final int KEY_SLEEP           = 0xDF;
    
    private static Map<Integer,String> KEYCODE_NAMES = null;
    private static Map<String,Integer> NAME_KEYCODES = null;
    
    private static void putKeyCodeName(int keyCode, String name) {
        KEYCODE_NAMES.put(keyCode, name);
        NAME_KEYCODES.put(name, keyCode);
    }
    
    private static void initKeyCodeData() {
        KEYCODE_NAMES = new HashMap<>();
        NAME_KEYCODES = new HashMap<>();
        putKeyCodeName(KEY_ESCAPE, "Escape");
	putKeyCodeName(KEY_1, "1");
	putKeyCodeName(KEY_2, "2");
        putKeyCodeName(KEY_3, "3");
	putKeyCodeName(KEY_4, "4");
        putKeyCodeName(KEY_5, "5");
	putKeyCodeName(KEY_6, "6");
        putKeyCodeName(KEY_7, "7");
	putKeyCodeName(KEY_8, "8");
        putKeyCodeName(KEY_9, "9");
	putKeyCodeName(KEY_0, "0");
	putKeyCodeName(KEY_MINUS, "-");
	putKeyCodeName(KEY_EQUALS, "=");
	putKeyCodeName(KEY_BACK, "Back");
	putKeyCodeName(KEY_TAB, "Tab");
	putKeyCodeName(KEY_Q, "Q");
	putKeyCodeName(KEY_W, "W");
	putKeyCodeName(KEY_E, "E");
	putKeyCodeName(KEY_R, "R");
	putKeyCodeName(KEY_T, "T");
	putKeyCodeName(KEY_Y, "Y");
	putKeyCodeName(KEY_U, "U");
	putKeyCodeName(KEY_I, "I");
	putKeyCodeName(KEY_O, "O");
	putKeyCodeName(KEY_P, "P");
	putKeyCodeName(KEY_LBRACKET, "[");
	putKeyCodeName(KEY_RBRACKET, "]");
	putKeyCodeName(KEY_ENTER, "Enter");
	putKeyCodeName(KEY_LCONTROL, "LCtrl");
	putKeyCodeName(KEY_A, "A");
	putKeyCodeName(KEY_S, "S");
	putKeyCodeName(KEY_D, "D");
	putKeyCodeName(KEY_F, "F");
	putKeyCodeName(KEY_G, "G");
	putKeyCodeName(KEY_H, "H");
	putKeyCodeName(KEY_J, "J");
	putKeyCodeName(KEY_K, "K");
	putKeyCodeName(KEY_L, "L");
	putKeyCodeName(KEY_SEMICOLON, ";");
	putKeyCodeName(KEY_APOSTROPHE, "'");
	putKeyCodeName(KEY_GRAVE, "`");
	putKeyCodeName(KEY_LSHIFT, "LShift");
	putKeyCodeName(KEY_BACKSLASH, "\\");
	putKeyCodeName(KEY_Z, "Z");
	putKeyCodeName(KEY_X, "X");
	putKeyCodeName(KEY_C, "C");
	putKeyCodeName(KEY_V, "V");
	putKeyCodeName(KEY_B, "B");
	putKeyCodeName(KEY_N, "N");
	putKeyCodeName(KEY_M, "M");
	putKeyCodeName(KEY_COMMA, ",");
	putKeyCodeName(KEY_PERIOD, ".");
	putKeyCodeName(KEY_SLASH, "/");
	putKeyCodeName(KEY_RSHIFT, "RShift");
	putKeyCodeName(KEY_MULTIPLY, "Numpd*");
	putKeyCodeName(KEY_LALT, "LAlt");
	putKeyCodeName(KEY_SPACE, "Space");
	putKeyCodeName(KEY_CAPITAL, "CapsLk");
	putKeyCodeName(KEY_F1, "F1");
        putKeyCodeName(KEY_F2, "F2");
        putKeyCodeName(KEY_F3, "F3");
        putKeyCodeName(KEY_F4, "F4");
        putKeyCodeName(KEY_F5, "F5");
        putKeyCodeName(KEY_F6, "F6");
        putKeyCodeName(KEY_F7, "F7");
        putKeyCodeName(KEY_F8, "F8");
        putKeyCodeName(KEY_F9, "F9");
        putKeyCodeName(KEY_F10, "F10");
	putKeyCodeName(KEY_NUMLOCK, "NumLk");
	putKeyCodeName(KEY_SCROLL, "ScrlLk");
	putKeyCodeName(KEY_NUMPAD7, "Numpd7");
	putKeyCodeName(KEY_NUMPAD8, "Numpd8");
	putKeyCodeName(KEY_NUMPAD9, "Numpd9");
	putKeyCodeName(KEY_SUBTRACT, "Numpd-");
	putKeyCodeName(KEY_NUMPAD4, "Numpd4");
	putKeyCodeName(KEY_NUMPAD5, "Numpd5");
	putKeyCodeName(KEY_NUMPAD6, "Numpd6");
	putKeyCodeName(KEY_ADD, "Numpd+");
	putKeyCodeName(KEY_NUMPAD1, "Numpd1");
	putKeyCodeName(KEY_NUMPAD2, "Numpd2");
	putKeyCodeName(KEY_NUMPAD3, "Numpd3");
	putKeyCodeName(KEY_NUMPAD0, "Numpd0");
	putKeyCodeName(KEY_DECIMAL, "Numpd.");
	putKeyCodeName(KEY_F11, "F11");
	putKeyCodeName(KEY_F12, "F12");
	putKeyCodeName(KEY_F13, "F13");
	putKeyCodeName(KEY_F14, "F14");
	putKeyCodeName(KEY_F15, "F15");
        putKeyCodeName(KEY_F16, "F16");
	putKeyCodeName(KEY_F17, "F17");
	putKeyCodeName(KEY_F18, "F18");
	putKeyCodeName(KEY_KANA, "Kana");
        putKeyCodeName(KEY_F19, "F19");
	putKeyCodeName(KEY_CONVERT, "Conv");
	putKeyCodeName(KEY_NOCONVERT, "NoConv");
	putKeyCodeName(KEY_YEN, "Yen");
	putKeyCodeName(KEY_NUMPADEQUALS, "Numpd=");
	putKeyCodeName(KEY_CIRCUMFLEX, "^");
	putKeyCodeName(KEY_AT, "@");
	putKeyCodeName(KEY_COLON, ":");
	putKeyCodeName(KEY_UNDERLINE, "_");
	putKeyCodeName(KEY_KANJI, "Kanji");
	putKeyCodeName(KEY_STOP, "Stop");
	putKeyCodeName(KEY_AX, "AX");
	putKeyCodeName(KEY_UNLABELED, "Unlabl");
	putKeyCodeName(KEY_NUMPADENTER, "NumpdE");
	putKeyCodeName(KEY_RCONTROL, "RCtrl");
        putKeyCodeName(KEY_SECTION, "Sectn");
	putKeyCodeName(KEY_NUMPADCOMMA, "Numpd,");
	putKeyCodeName(KEY_DIVIDE, "Numpd%");
	putKeyCodeName(KEY_SYSRQ, "SysRq");
	putKeyCodeName(KEY_RALT, "RAlt");
        putKeyCodeName(KEY_FUNCTION, "Functn");
	putKeyCodeName(KEY_PAUSE, "Pause");
	putKeyCodeName(KEY_HOME, "Home");
	putKeyCodeName(KEY_UP, "Up");
	putKeyCodeName(KEY_PRIOR, "PageUp");
	putKeyCodeName(KEY_LEFT, "Left");
	putKeyCodeName(KEY_RIGHT, "Right");
	putKeyCodeName(KEY_END, "End");
	putKeyCodeName(KEY_DOWN, "Down");
	putKeyCodeName(KEY_NEXT, "PageDn");
	putKeyCodeName(KEY_INSERT, "Insert");
	putKeyCodeName(KEY_DELETE, "Delete");
        putKeyCodeName(KEY_CLEAR, "Clear");
	putKeyCodeName(KEY_LMETA, "LMeta");
	putKeyCodeName(KEY_RMETA, "RMeta");
	putKeyCodeName(KEY_APPS, "Apps");
	putKeyCodeName(KEY_POWER, "Power");
	putKeyCodeName(KEY_SLEEP, "Sleep");
    }
    
    /**
     * Returns a short, descriptive, and unique String name for the specified
     * key code, identical to the return value of the getName() method of a
     * KeyControl with the key code. If the key code is invalid, this method
     * will return null.
     * @param keyCode The key code to return the name of
     * @return The name of the specified key code
     */
    public static String getKeyCodeName(int keyCode) {
        if (KEYCODE_NAMES == null) {
            initKeyCodeData();
        }
        return KEYCODE_NAMES.get(keyCode);
    }
    
    /**
     * Returns the key code whose name according to getKeyCodeName() is the
     * specified String. If no key code has the String as a name, this method
     * will return null.
     * @param name The name of the key code to be returned
     * @return The key code whose name is the specified String
     */
    public static Integer getKeyCode(String name) {
        if (NAME_KEYCODES == null) {
            initKeyCodeData();
        }
        return NAME_KEYCODES.get(name);
    }
    
    private final int keyCode;
    
    /**
     * Constructs a KeyControl that represents the key with the specified key
     * code.
     * @param keyCode This KeyControl's key code
     * @throws InvalidControlException If the key code is invalid
     */
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
    
    /**
     * Returns this KeyControl's key code.
     * @return This KeyControl's key code
     */
    public final int getKeyCode() {
        return keyCode;
    }
    
}
