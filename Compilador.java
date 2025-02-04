import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

public class Compilador {
    private static int auxIndex = -1;
    private static int auxTag = -1;
    private static int contextActual = 0;
    private static HashMap<Integer, ArrayList<Tag>> variables = new HashMap<>();
    public static boolean c = false;
    public static String x = "-1";

    public static String print(String v, boolean c) {
        if (variables.containsKey(0)) {
            for (Tag t : variables.get(0)) {
                if (t.getB().equals(v) && t.getLength() > 0) {
                    printMatriz(v, t.getLength(), t.getA());
                    return v;
                }
            }
        }
        if (isType(v, "char")) c = true;
        System.out.println("print" + (c ? "c " : " ") + v + " ;");
        return v;
    }

    public static void printString(String k) {
        String aux = k.substring(1, k.length() - 1);
        int x = 0;
        do {
            int ascii = 0;
            if ((aux.charAt(x) == '\\') && (aux.charAt(x + 1) == '\\')) {
                ascii = 92;
                x++;
            } else if ((aux.charAt(x) == '\\') && (aux.charAt(x + 1) == '"')) {
                ascii = 34;
                x++;
            } else if ((aux.charAt(x) == '\\') && (aux.charAt(x + 1) == 'b')) {
                ascii = 8;
                x++;
            } else if ((aux.charAt(x) == '\\') && (aux.charAt(x + 1) == 'n')) {
                ascii = 10;
                x++;
            } else if ((aux.charAt(x) == '\\') && (aux.charAt(x + 1) == 'r')) {
                ascii = 13;
                x++;
            } else if (aux.substring(x).contains("\\u")) {
                ascii = Integer.parseInt(aux.substring(x, aux.length()), 16);
            } else {
                ascii = (int) aux.charAt(x);
            }
            System.out.println("writec " + ascii + ";");
            x++;
        } while (x < aux.length());
        System.out.println("writec 10;");
    }

    private static void printMatriz(String k, int length, String type) {
        String aux = newVariableAuxiliar();
        for (int x = 0; x < length; x++) {
            System.out.println(aux + " = " + k + "[" + x + "] ;");
            System.out.println("print" + (type.equalsIgnoreCase("char") ? "c " : " ") + aux + " ;");
        }
    }

    public static void printList(ArrayList<String> values, boolean c) {
        Collections.reverse(values);
        for (int x = 0; x < values.size(); x++) {
            System.out.println("print" + (c ? "c " : " ") + values.get(x) + " ;");
        }
    }

    public static String asig(String k, String v, String range, boolean cx) {
        if (!k.contains("*"))
            k = checkDeclaracion(k);
        else
            checkDeclaracion(k.substring(1, k.length()));

        if (isType(k, "char") && range != "char" && !cx) {
            error("error de tipos");
            return k;
        }
        
        String clave = k;
        if (!range.equals("-1") && !range.equals("char")) {
            clave = k + "[" + range + "]";
        }

        boolean isVar = false;
        isVar = v.matches("[a-zA-Z][a-zA-Z0-9]*");

        if (isVar) {
            if (isArray(k).getLength() != -1 && isArray(v).getLength() != -1) {
                int length_1 = isArray(k).getLength();
                int length_2 = isArray(v).getLength();
                if (length_1 < length_2) {
                    error("Las matrices no son compatibles");
                }
                if (!isArray(k).getA().equals(isArray(v).getA())) {
                    error("error de tipos");
                }
            } else if (isType(v, "FLOAT") && isType(k, "INT")) {
                System.out.println(clave + " = (int) " + v + " ;");
                return clave;
            } else if (isType(v, "INT") && isType(k, "FLOAT")) {
                System.out.println(clave + " = (float) " + v + " ;");
                return clave;
            }
        } else if (v.matches("[0-9]*[.][0-9]+([eE][-+]?[0-9]+)?")) {
            if (isType(k, "int")) {
                error("ha intentado asignar un numero real a una variable entera");
                return null;
            }
        }
        System.out.println(clave + " = " + v + " ;");
        return clave;
    }

    private static String newVariableAuxiliar() {
        auxIndex++;
        return "t" + auxIndex;
    }

    public static String operacion(String var1, String var2, String op) {
        String aux = newVariableAuxiliar();
        if (isType(var1, "*int") && isType(var2, "*int")) {
            error("aritmetica de punteros no permitida");
            return aux;
        }
        if (var2.isEmpty()) {
            System.out.println(aux + " = " + op + var1 + " ;");
        } else {
            System.out.println(aux + " = " + var1 + " " + op + " " + var2 + " ;");
        }
        return aux;
    }

    public static String prepost(String op, String var, String number) {
        if (op.equals("PRE")) {
            System.out.println(var + " = " + number + " ;");
            return var;
        } else {
            String aux = newVariableAuxiliar();
            System.out.println(aux + " = " + var + " ;");
            System.out.println(var + " = " + number + " ;");
            return aux;
        }
    }

    public static void etiqueta(String label) {
        System.out.println("label " + label + " ;");
    }
    
    public static void goToLabel(String label) {
        System.out.println("goto " + label + " ;");
    }
    
    public static String generarTag() {
        auxTag++;
        return "L" + auxTag;
    }
    
    public static Tag condicion(String cond, String arg1, String arg2) {
        Tag result = new Tag(generarTag(), generarTag());
        
        switch (cond) {
            case "IGUAL":
                System.out.println("if(" + arg1 + " == " + arg2 + ") goto " + result.getA() + " ;");
                System.out.println("goto " + result.getB() + " ;");
                break;
                
            case "DIST":
                System.out.println("if(" + arg1 + " == " + arg2 + ") goto " + result.getB() + " ;");
                System.out.println("goto " + result.getA() + " ;");
                break;
                
            case "MENOR":
                System.out.println("if(" + arg1 + " < " + arg2 + ") goto " + result.getA() + " ;");
                System.out.println("goto " + result.getB() + " ;");
                break;
                
            case "MAYOR":
                System.out.println("if(" + arg2 + " < " + arg1 + ") goto " + result.getA() + " ;");
                System.out.println("goto " + result.getB() + " ;");
                break;
                
            case "MAYOREQ":
                System.out.println("if(" + arg1 + " < " + arg2 + ") goto " + result.getB() + " ;");
                System.out.println("goto " + result.getA() + " ;");
                break;
                
            case "MENOREQ":
                System.out.println("if(" + arg2 + " < " + arg1 + ") goto " + result.getB() + " ;");
                System.out.println("goto " + result.getA() + " ;");
                break;
        }
        
        return result;
    }

    public static Tag operador(String op, Tag t1, Tag t2) {
        Tag result = t2;
        
        switch (op) {
            case "NOT":
                result = new Tag(t1.getB(), t1.getA());
                break;
                
            case "AND":
                etiqueta(t1.getB());
                goToLabel(t2.getA());
                break;
                
            case "OR":
                etiqueta(t1.getA());
                goToLabel(t2.getB());
                break;
        }
        
        return result;
    }
    // NUEVO EN PLXC

    public static String declaracion(String type, String k, String v) {
        boolean found = false;
        int aux = contextActual;
        
        while (aux >= 0 && !found) {
            if (variables.containsKey(aux)) {
                for (Tag t : variables.get(aux)) {
                    if (t.getB().equals(k))
                        found = true;
                }
            }
            aux--;
        }
        
        if (!k.contains("$") && found && (aux + 1) == contextActual) {  // encontrada en el mismo contexto
            error("variable \"" + k + "\" ya declarada.");
            return null;
        } else {  // no encontrada o encontrada en otro contexto -> la declaramos
            if (variables.containsKey(contextActual)) {
                variables.get(contextActual).add(new Tag(type, k));
            } else {
                ArrayList<Tag> al = new ArrayList<>();
                al.add(new Tag(type, k));
                variables.put(contextActual, al);
            }
            
            if (!v.equals("0")) {
                if (!k.contains("$")) {
                    if ((aux + 1) < contextActual) {
                        System.out.println(k + "_" + (contextActual - 1) + " = " + v + " ;");
                    } else {
                        System.out.println(k + " = " + v + " ;");
                    }
                }
            }
        }
        return k;
    }
    
    public static String declaracionArray(String type, String k, String length, String fila, String columna, String profundidad) {
        boolean found = false;
        int aux = contextActual;
        
        while (aux >= 0 && !found) {
            if (variables.containsKey(aux)) {
                for (Tag t : variables.get(aux)) {
                    if (t.getB().equals(k))
                        found = true;
                }
            }
            aux--;
        }
        
        if (found && (aux + 1) == contextActual) {  // encontrada en el mismo contexto
            error("variable \"" + k + "\" ya declarada.");
            return null;
        } else {  // no encontrada o encontrada en otro contexto -> la declaramos
            if (variables.containsKey(contextActual)) {
                variables.get(contextActual).add(new Tag(type, k, Integer.parseInt(length)));
            } else {
                ArrayList<Tag> al = new ArrayList<>();
                al.add(new Tag(type, k, Integer.parseInt(length)));
                variables.put(contextActual, al);
            }
            
            System.out.println("$" + k + "_length = " + length + " ;");
            
            if (!fila.equals("")) {
                System.out.println("$" + k + "_fil_size = " + fila + " ;");
                System.out.println("$" + k + "_col_size = " + columna + " ;");
            }
            
            if (!profundidad.equals("")) {
                System.out.println("$" + k + "_prof_size = " + profundidad + " ;");
            }
        }
        return k;
    }

    public static String checkDeclaracion(String k) {
        boolean found = false;
        int aux = contextActual;
        String clave = k;
    
        if (k.contains("[")) {
            k = k.substring(0, k.indexOf("["));
        }
    
        while (aux >= 0 && !found) {
            if (variables.containsKey(aux)) {
                for (Tag t : variables.get(aux)) {
                    if (t.getB().equals(k))
                        found = true;
                }
            }
            aux--;
        }
    
        if (found) {
            boolean antes = false;
            int aux2 = contextActual - 1;
    
            while (aux2 >= 0 && !antes) {
                if (variables.containsKey(aux2)) {
                    for (Tag t : variables.get(aux2)) {
                        if (t.getB().equals(k))
                            antes = true;
                    }
                }
                aux2--;
            }
    
            if (antes && variables.containsKey(contextActual) &&
                variables.get(contextActual).contains(k)) { // encontrada antes y ahora -> nuevo nombre
                boolean check = false;
                for (Tag t : variables.get(contextActual)) {
                    if (t.getB().equals(k))
                        check = true;
                }
                if (check)
                    return k + "_" + (contextActual - 1);
            }
            return clave;  // mismo nombre si no...
        } else {
            error("variable \"" + k + "\" no declarada.");
            return null;
        }
    }
    
    public static void checkDeclaracionArray(String i1, String k) {
        checkDeclaracion(k);
    
        if (isArray(k).getLength() == -1) {
            error("error de tipos, no ha introducido un array");
        }
    
        if (isArray(k).getA().equals("int") && isType(i1, "float") ||
            isArray(k).getA().equals("float") && isType(i1, "int")) {
            error("tipos incompatibles");
        }
    }
    
    public static void aumentarContexto() {
        contextActual++;
    }
    
    public static void disminuirContexto() {
        variables.remove(contextActual);
        contextActual--;
    }
    
    public static boolean isType(String var, String type) {
        boolean found = false;
        for (int context : variables.keySet()) {
            for (Tag t : variables.get(context)) {
                if (t.getA().equalsIgnoreCase(type) && t.getB().equals(var)) {
                    found = true;
                }
            }
        }
        return found;
    }
    
    public static Tag isArray(String var) {
        Tag tag = new Tag("", "");
        for (int context : variables.keySet()) {
            for (Tag t : variables.get(context)) {
                if (t.getB().equals(var) && t.getLength() != -1) {
                    tag = t;
                }
            }
        }
        return tag;
    }
    
    public static void error(String error) {
        System.out.println("error ;");
        System.out.println("# " + error);
        System.out.println("halt ;");
    }
    
    public static void imprimeResultados() {
        System.out.println("\n\n# ********VARIABLES********");
        for (int c : variables.keySet()) {
            for (Tag t : variables.get(0)) {
                System.out.println("#\t" + t);
            }
        }
    }
    
    public static void checkRango(String k, String rango) {
        String L0 = generarTag();
        String L1 = generarTag();
    
        System.out.println("\n# Comprobacion de rango");
        System.out.println("if (" + rango + " < 0) goto " + L0 + " ;");
        System.out.println("if ($" + k + "_length < " + rango + ") goto " + L0 + " ;");
        System.out.println("if ($" + k + "_length == " + rango + ") goto " + L0 + " ;");
        
        goToLabel(L1);
        etiqueta(L0);
        error("Fallo en comprobacion de rango");
        etiqueta(L1);
        System.out.println();
    }

    public static void checkRangoMultiple(String k, String f, String c, String p) {
        String L0 = generarTag();
        String L1 = generarTag();
    
        System.out.println("\n# Comprobacion de rango para fila");
        System.out.println("if (" + f + " < 0) goto " + L0 + " ;");
        System.out.println("if ($" + k + "_fil_size < " + f + ") goto " + L0 + " ;");
        System.out.println("if ($" + k + "_fil_size == " + f + ") goto " + L0 + " ;");
        goToLabel(L1);
        etiqueta(L0);
        error("Fallo en comprobacion de rango de fila");
        etiqueta(L1);
        System.out.println();
    
        L0 = generarTag();
        L1 = generarTag();
        System.out.println("# Comprobacion de rango para columna");
        System.out.println("if (" + c + " < 0) goto " + L0 + " ;");
        System.out.println("if ($" + k + "_col_size < " + c + ") goto " + L0 + " ;");
        System.out.println("if ($" + k + "_col_size == " + c + ") goto " + L0 + " ;");
        goToLabel(L1);
        etiqueta(L0);
        error("Fallo en comprobacion de rango de columna");
        etiqueta(L1);
        System.out.println();
    
        if (!p.equals("")) {
            L0 = generarTag();
            L1 = generarTag();
            System.out.println("# Comprobacion de rango para profundidad");
            System.out.println("if (" + p + " < 0) goto " + L0 + " ;");
            System.out.println("if ($" + k + "_prof_size < " + p + ") goto " + L0 + " ;");
            System.out.println("if ($" + k + "_prof_size == " + p + ") goto " + L0 + " ;");
            goToLabel(L1);
            etiqueta(L0);
            error("Fallo en comprobacion de rango de profundidad");
            etiqueta(L1);
            System.out.println();
        }
    }
    
    public static String inicializaArray(String k, ArrayList<String> values, boolean nuevo) {
        Collections.reverse(values);
        int length = 0;
        String type = "";
    
        for (int context : variables.keySet()) {
            for (Tag t : variables.get(context)) {
                if (t.getB().equals(k)) {
                    length = t.getLength();
                    type = t.getA();
                }
            }
        }
    
        if (!nuevo && !type.equals("char")) {
            if (!checkAllTypes(type, values)) {
                error("error de tipos");
                return null;
            }
        }
    
        if (length < values.size()) {
            error("Has intentado inicializar un array de longitud " + length + " con " + values.size() + " valores");
        } else {
            String var_aux1 = newVariableAuxiliar();
            int idx = 0;
    
            for (String v : values) {
                System.out.println(var_aux1 + "[" + idx + "] = " + v + " ;");
                idx++;
            }
    
            String var_aux2 = newVariableAuxiliar();
            for (int x = 0; x < values.size(); x++) {
                System.out.println(var_aux2 + " = " + var_aux1 + "[" + x + "] ;");
                System.out.println(k + "[" + x + "] = " + var_aux2 + " ;");
            }
        }
        return k;
    }
    public static String inicializaArrayMultiple(String k, ArrayList<ArrayList<String>> val){
        Collections.reverse(val);
        ArrayList<String> values = new ArrayList<>();
        String L0 = generarTag();
        String L1 = generarTag();
        System.out.println("# Comprobacion de rango inicializacion");
        System.out.println("if ($"+ k + "_fil_size < " + val.size() + ") goto " + L0 + " ;");
        for(ArrayList<String> v : val){
            Collections.reverse(v);
            values.addAll(v);
            System.out.println("if ($"+ k + "_col_size < " + v.size() + ") goto " + L0 + " ;");
        }
        goToLabel(L1);
        etiqueta(L0);
        error("Fallo en inicializacion");
        etiqueta(L1);
        int length = 0;
        for(int context : variables.keySet()){
            for(Tag t : variables.get(context)){
                if(t.getB().equals(k)){
                    length = t.getLength();
                }
            }
        }
        String var_aux1 = newVariableAuxiliar();
        int idx = 0;
        for(String v : values){
            System.out.println(var_aux1 + "[" + idx + "] = " + v + ";");
            idx++;
        }
        String var_aux2 = newVariableAuxiliar();
        for(int x = 0; x < values.size(); x++){
            System.out.println(var_aux2 + " = " + var_aux1 + "[" + x + "] ;");
            System.out.println(k + "[" + x + "] = " + var_aux2 + " ;");
        }
        return k;
    }
    public static String inicializaArrayTriple(String k, ArrayList<ArrayList<ArrayList<String>>> values2){
        Collections.reverse(values2);
        ArrayList<String> values = new ArrayList<>();
        for(ArrayList<ArrayList<String>> v : values2){
            Collections.reverse(v);
            for(ArrayList<String> x : v){
                Collections.reverse(x);
                values.addAll(x);
            }
        }
        int length = 0;
        for(int context : variables.keySet()){
            for(Tag t : variables.get(context)){
                if(t.getB().equals(k)){
                    length = t.getLength();
                }
            }
        }
        String var_aux1 = newVariableAuxiliar();
        int idx = 0;
        for(String v : values){
            System.out.println(var_aux1 + "[" + idx + "] = " + v + ";");
            idx++;
        }
        String var_aux2 = newVariableAuxiliar();
        for(int x = 0; x < values.size(); x++){
            System.out.println(var_aux2 + " = " + var_aux1 + "[" + x + "] ;");
            System.out.println(k + "[" + x + "] = " + var_aux2 + " ;");
        }
        return k;
    }

    public static boolean checkAllTypes(String type, ArrayList<String> values){
        if(type.equalsIgnoreCase("int")){ // comprobamos si todos son int
            for(String v : values){
                if(v.matches("[0-9]*[.][0-9]+([eE][-+]?[0-9]+)?"))
                    return false;
            }
            return true;
        }else{
            for(String v : values){
                if(v.matches("0|[1-9][0-9]*"))
                    return false;
            }
            return true;
        }
    }

    public static Tag forIn(String var, String array, String tag, ArrayList<String> elem){
        if(array.equals("") && elem != null){
            array = inicializaArray(newVariableAuxiliar(), elem, true);
            System.out.println(array + "_length = " + elem.size() + " ;");
        }
        declaracion("int", "$cont", "0");
        asig(var, Compilador.operacion(array + "[0]", "", ""), "-1", false);
        etiqueta(tag);
        Tag c = condicion("MENOR", "$cont", array + "_length");
        String labelL3 = generarTag();
        Compilador.etiqueta(labelL3);
        String t2 = newVariableAuxiliar();
        String t1 = newVariableAuxiliar();
        System.out.println(t2 + "= $cont + 1 ;");
        System.out.println(t1 + "=" + array + "[" + t2 + "] ;");
        System.out.println(var + "=" + t1 + " ;");
        System.out.println("$cont = " + t2 + " ;");
        goToLabel(tag);
        etiqueta(c.getA());
        return new Tag(labelL3, c.getB());
    }
    public static String calculaMultiArray(String k, String f, String c, String p){
        String t0 = newVariableAuxiliar();
        System.out.println(t0 + " = $" + k + "_col_size * " + f + " ;");
        System.out.println(t0 + " = " + t0 + " + " + c + " ;");
        String t1 = newVariableAuxiliar();
        if(!p.equals("")){
            System.out.println(t1 + " = $" + k + "_prof_size * " + t0 + " ;");
            System.out.println(t1 + " = " + t1 + " + " + p + " ;");
            return t1;
        }
        return t0;
    }
    
    public static String puntero(int length, String k){
        String aux = "";
        String result = "*" + k;
        for(int x = 1; x <= length; x++){
            result = (x < length ? "*" : "") + Compilador.operacion(result, "", "");
        }
        return result;
    }
    
    public static String creaChar(String v){
        int ascii = 0;
        if(v.matches("\'[^\\']\'")){
            ascii = (int) v.charAt(1);
        }else if (v.contains("\\u")){
            return Integer.toString(Integer.parseInt(v.substring(3, v.length()-1), 16));
        }else{
            switch(v.charAt(2)){
                case 'b': return "8";
                case 'n': return "10";
                case 'r': return "13";
                case 't': return "9";
                case '\\': return "92";
                case '\'': return "39";
                case '\"': return "34";
            }
        }
        return Integer.toString(ascii);
    }
    
    public static String creaString(String v){
        int ascii = 0;
        if(v.matches("\'[^\\']\'")){
            ascii = (int) v.charAt(1);
        }else if (v.contains("\\u")){
            return Integer.toString(Integer.parseInt(v.substring(3, v.length()-1), 16));
        }else{
            switch(v.charAt(2)){
                case 'b': return "8";
                case 'n': return "10";
                case 'r': return "13";
                case 't': return "9";
                case '\\': return "92";
                case '\'': return "39";
                case '\"': return "34";
            }
        }
        return Integer.toString(ascii);
    }
    
    public static boolean checkChar(String k){
        return isType(k,"char");
    }
    
    public static String operaChar(String op, String k){
        String t0 = newVariableAuxiliar();
        String L0 = generarTag();
        String L1 = generarTag();
        switch(op){
            case "MONTE":
                System.out.println(t0 + " = " + k + " ;");
                System.out.println("if (" + k + " < 65) goto " + L0 + " ;");
                System.out.println("if (122 < " + k + ") goto " + L0 + " ;");
                System.out.println("if (96 < " + k + ") goto " + L1 + " ;");
                System.out.println("if (90 < " + k + ") goto " + L0 + " ;");
                System.out.println(t0 + " = " + k + " + 32 ;");
                System.out.println("goto " + L0 + ";");
                System.out.println("label " + L1 + ";");
                System.out.println(t0 + " = " + k + " - 32 ;");
                System.out.println("label " + L0 + ";");
                break;
            case "MAYUS":
                String L2 = generarTag();
                System.out.println(t0 + " = " + k + " ;");
                System.out.println("if (" + k + " < 97) goto " + L0 + " ;");
                System.out.println("if (122 < " + k + ") goto " + L0 + " ;");
                System.out.println(t0 + " = " + k + " - 32 ;");
                System.out.println("label " + L0 + ";");
                String t1 = newVariableAuxiliar();
                System.out.println(t1 + " = " + t0 + " ;");
                System.out.println("if (" + t0 + " < 65) goto " + L1 + " ;");
                System.out.println("if (122 < " + t0 + ") goto " + L1 + " ;");
                System.out.println("if (96 < " + t0 + ") goto " + L2 + " ;");
                System.out.println("if (90 < " + t0 + ") goto " + L1 + " ;");
                System.out.println(t1 + " = " + t0 + " + 32 ;");
                System.out.println("goto " + L1 + ";");
                System.out.println("label " + L2 + ";");
                System.out.println(t0 + " = " + k + " - 32 ;");
                System.out.println("label " + L1 + ";");
                t0 = t1;
                break;
            case "NEG" :
                System.out.println(t0 + " = " + k + " ;");
                System.out.println("if (" + k + " < 97) goto " + L0 + " ;");
                System.out.println("if (122 < " + k + ") goto " + L0 + " ;");
                System.out.println(t0 + " = " + k + " - 32 ;");
                System.out.println("label " + L0 + ";");
                break;
        }
        return t0;
    }  
    /*

    ">>"            {return new Symbol(sym.BITLRDERECHA);}
    "<<"            {return new Symbol(sym.BITLRIZQUIERDA);}
 
    public static String bitwiseShift(String var1, String var2, String op) {
        String aux = newVariableAuxiliar();
        String counter = newVariableAuxiliar();
        String labelStart = generarTag();
        String labelEnd = generarTag();

        System.out.println(counter + " = 1 ;");
        System.out.println(aux + " = " + var1 + " ;");
        System.out.println("label " + labelStart + " ;");
        System.out.println("if (" + var2 + " < " + counter + ") goto " + labelEnd + " ;");

        if (op.equals(">>")) {
            System.out.println(aux + " = " + aux + " / 2 ;");
        } else if (op.equals("<<")) {
            System.out.println(aux + " = " + aux + " * 2 ;");
        }

        System.out.println(counter + " = " + counter + " + 1 ;");
        System.out.println("goto " + labelStart + " ;");
        System.out.println("label " + labelEnd + " ;");

        return aux;
    }

    exp:e1 BITLRDERECHA exp:e2 {:String t1 = Compilador.operacion(e1, "", ""); String t2 = Compilador.operacion(e2, "", ""); RESULT = Compilador.bitwiseShift(t1, t2, ">>");:}
    exp:e1 BITLRIZQUIERDA exp:e2 {:String t1 = Compilador.operacion(e1, "", ""); String t2 = Compilador.operacion(e2, "", ""); RESULT = Compilador.bitwiseShift(t1, t2, "<<");:}
    */



}