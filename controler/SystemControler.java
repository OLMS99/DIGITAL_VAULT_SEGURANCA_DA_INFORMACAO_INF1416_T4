package controler;

import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;

import model.Sistema;

public class SystemControler {
    private static final Sistema sistema = Sistema.getInstance();
    public SystemControler(){}
    public static void TurnOn(){sistema.turnOn();}
    public static void TurnOff(){sistema.shutDown();}
    public static Logger getSistemaLogger(){return sistema.log;}


}