package stone;
//import stone.ast.ASTree;

//-----------------------------------.
// StoneExceptionクラス.
//-----------------------------------.
public class StoneException extends RuntimeException {
    public StoneException(String m) { super(m); }
    //public StoneException(String m, ASTree t) {
    //    super(m + " " + t.location());
    //}
}