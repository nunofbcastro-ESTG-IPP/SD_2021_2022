package estg.ipp.pt.utlis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations {
    public static Boolean emailValidation(String email) {
        String regex = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
                + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.\\p{L}{2,})$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        return  matcher.matches();
    }

    public static Boolean passwordValidation(
            String password,
            int minCharacters,
            int numberLowercases,
            int numberUppercases,
            int numberNumbers,
            int numberSpecialCharacters
    ){
        String regex =  "(?=.*[a-z]{"+numberLowercases+",})"+
                "(?=.*[A-Z]{"+numberUppercases+",})"+
                "(?=.*\\d{"+numberNumbers+",})"+
                "(?=.*[-._!\"`'#%&,:;<>=@{}~\\$\\(\\)\\*\\+\\/\\\\\\?\\[\\]\\^\\|]{"+numberSpecialCharacters+",})"+
                ".{"+minCharacters+",}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);

        return  matcher.matches();
    }

    public static Boolean passwordValidation(
            String password
    ){
        return passwordValidation(
                password,8,1,1,1,1
        );
    }

    public static Boolean nameValidation(
            String name
    ){
        String regex =  "^([A-Z\\u00C0-\\u017F][a-z\\u00C0-\\u017F]{2,}+\\s?)+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);

        return  matcher.matches();
    }

    public static Boolean lineNameValidation(
            String lineName
    ){
        return lineName.length() > 3;
    }
}

