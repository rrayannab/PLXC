public class Tag {

    private String a;
    private String b;
    private int length;

    public Tag(String a, String b){
        this.a = a;
        this.b = b;
        this.length = -1;
    }

    public Tag (String type, String k, int length){
        this.a = type;
        this.b = k;
        this.length = length;
    }

    public String getA(){
        return a;
    }

    public String getB(){
        return b;
    }

    public int getLength(){
        return length;
    }

    @Override
    public String toString(){
        if(length==-1){ // no tiene tipo
            return "T(" + a + "," + b + ")";
        }else{ // etiqueta array
            return "T_array(" + a + "," + b + "," + length + ")";
        }
    }
}