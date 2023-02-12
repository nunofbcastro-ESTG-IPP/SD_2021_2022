package estg.ipp.pt.sd_grupo6_client.Utils;

public class Convert {
    public static Integer StringToInt(String number, Integer defaultValue){
        try{
            return Integer.parseInt(number);
        }catch (Exception e){
            return defaultValue;
        }
    }
    public static double StringToDouble(String number, double defaultValue){
        try{
            return Double.parseDouble(number);
        }catch (Exception e){
            return defaultValue;
        }
    }
}

