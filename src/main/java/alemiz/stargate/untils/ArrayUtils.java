package alemiz.stargate.untils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ArrayUtils {

    public static String implode(String glue, String[] array, int startIndex){
        String implode = "";
        for (int i = startIndex; i < array.length; i++){
            implode += array[i]+glue;
        }
        implode.substring(0, implode.length()-glue.length());

        return  implode;
    }

    public static String implode(String glue, String[] array){
        return implode(glue, array, 0);
    }

    public static String implode(String glue, List<String> array, int startIndex){
        String implode = "";
        for (int i = startIndex; i < array.size(); i++){
            if (i == array.size()-1){
                implode += array.get(i);
                continue;
            }

            implode += array.get(i)+glue;
        }
        //implode.substring(0, implode.length()-glue.length());

        return implode;
    }

    public static String implode(String glue, List<String> array){
        return implode(glue, array, 0);
    }


    public static String[] explode(String delimiter, String string){
        return string.split(delimiter);
    }

    public static List<String> explodeList(String delimiter, String string){
        return new LinkedList<String>(Arrays.asList(string.split(delimiter)));
    }

    public static List<Object> insertList(List<Object> originalList, List<Object> insert){
        for (Object element : insert){
            if (!originalList.contains(element)) originalList.add(element);
        }

        return originalList;
    }
}
