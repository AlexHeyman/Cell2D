package cell2d.control;

import cell2d.Direction;

/**
 * @author Andrew Heyman
 */
public abstract class Control {
    
    /**
     * Returns a short, descriptive, and unique String name for this Control.
     * The name will be no more than 6 characters long and contain only ASCII
     * characters and no whitespace. Two distinct Control objects will have the
     * same name if and only if they are equal.
     * @return This Control's name
     */
    public abstract String getName();
    
    /**
     * Returns the Control whose name according to getName() is the specified
     * String, or null if no Control has that String as a name.
     * @param name The name of the Control to be returned
     * @return The Control whose name is the specified String
     */
    public static Control getControl(String name) {
        try {
            Integer keycode = KeyControl.getKeyCode(name);
            if (keycode != null) {
                return new KeyControl(keycode);
            } else if (name.equals("LMB")) {
                return new MouseButtonControl(MouseButtonControl.MOUSE_LEFT_BUTTON);
            } else if (name.equals("RMB")) {
                return new MouseButtonControl(MouseButtonControl.MOUSE_RIGHT_BUTTON);
            } else if (name.equals("MMB")) {
                return new MouseButtonControl(MouseButtonControl.MOUSE_MIDDLE_BUTTON);
            } else if (name.length() > 0 && name.charAt(0) == 'C') {
                int i = 1;
                char c;
                while (true) {
                    if (i == name.length()) {
                        return null;
                    }
                    c = name.charAt(i);
                    if (!Character.isDigit(c)) {
                        break;
                    }
                    i++;
                }
                String controllerNumStr = name.substring(1, i);
                if (controllerNumStr.charAt(0) == '0' && controllerNumStr.length() > 1) {
                    return null;
                }
                int controllerNum;
                try {
                    controllerNum = Integer.parseInt(controllerNumStr);
                } catch (NumberFormatException e) {
                    return null;
                }
                if (c == 'B') {
                    i++;
                    int j = i;
                    while (j < name.length()) {
                        c = name.charAt(j);
                        if (!Character.isDigit(c)) {
                            return null;
                        }
                        j++;
                    }
                    String buttonNumStr = name.substring(i, j);
                    if (buttonNumStr.charAt(0) == '0' && buttonNumStr.length() > 1) {
                        return null;
                    }
                    int buttonNum;
                    try {
                        buttonNum = Integer.parseInt(buttonNumStr);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    return new ControllerButtonControl(controllerNum, buttonNum);
                }
                switch (name.substring(i)) {
                    case "Up":
                        return new ControllerDirectionControl(controllerNum, Direction.UP);
                    case "Dwn":
                        return new ControllerDirectionControl(controllerNum, Direction.DOWN);
                    case "Lft":
                        return new ControllerDirectionControl(controllerNum, Direction.LEFT);
                    case "Rgt":
                        return new ControllerDirectionControl(controllerNum, Direction.RIGHT);
                }
            }
        } catch (InvalidControlException e) {
            return null;
        }
        return null;
    }
    
}
